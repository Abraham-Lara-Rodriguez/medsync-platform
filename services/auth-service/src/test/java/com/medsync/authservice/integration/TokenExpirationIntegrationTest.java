package com.medsync.authservice.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medsync.authservice.config.jwt.JwtService;
import com.medsync.authservice.domain.entity.User;
import com.medsync.authservice.domain.enums.auth.Role;
import com.medsync.authservice.repository.auth.RefreshTokenRepository;
import com.medsync.authservice.repository.user.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.MockMvcPrint;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, properties = {
        "medsync.security.access-token-expiration=3000",
        "medsync.security.refresh-token-expiration=10000"
})
@AutoConfigureMockMvc(print = MockMvcPrint.NONE)
class TokenExpirationIntegrationTest extends AbstractIntegrationTest {
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;
    @Autowired private UserRepository users;
    @Autowired private RefreshTokenRepository tokens;
    @Autowired private PasswordEncoder encoder;
    @Autowired private JwtService jwt;

    @Test
    void accessAndRefreshReallyExpireWithoutChangingClockOrClaims() throws Exception {
        tokens.deleteAll();
        users.deleteAll();
        User user = users.save(User.create("expiration@example.invalid", encoder.encode("test-password"), Role.ADMIN));
        Map<String, Object> evidence = new LinkedHashMap<>();
        evidence.put("scope", "Isolated PostgreSQL and real Spring filters; access TTL 3s, refresh TTL 10s; actual wall-clock waiting, no mocked clock or token rewriting");
        JsonNode session = mapper.readTree(mvc.perform(post("/api/v1/auth/login").contentType("application/json")
                        .content(mapper.writeValueAsString(Map.of("email", user.getEmail(), "password", "test-password"))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        String access = session.get("accessToken").asText();
        String refresh = session.get("refreshToken").asText();
        Instant accessExpiry = jwt.parse(access).getExpiration().toInstant();
        evidence.put("accessExpiresAt", accessExpiry.toString());
        mvc.perform(get("/api/v1/users/" + user.getId()).header("Authorization", "Bearer " + access))
                .andExpect(status().isOk());
        evidence.put("accessBeforeExpiry", 200);

        awaitExpiry(accessExpiry);
        assertThatThrownBy(() -> jwt.parse(access)).isInstanceOf(ExpiredJwtException.class);
        mvc.perform(get("/api/v1/users/" + user.getId()).header("Authorization", "Bearer " + access))
                .andExpect(status().isUnauthorized());
        evidence.put("accessAfterExpiry", 401);
        assertThat(jwt.isRefreshTokenValid(refresh)).isTrue();

        JsonNode rotated = mapper.readTree(mvc.perform(post("/api/v1/auth/refresh-token").contentType("application/json")
                        .content(mapper.writeValueAsString(Map.of("refreshToken", refresh))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        evidence.put("refreshAfterAccessExpired", 200);
        String newRefresh = rotated.get("refreshToken").asText();
        Instant refreshExpiry = jwt.parse(newRefresh).getExpiration().toInstant();
        evidence.put("refreshExpiresAt", refreshExpiry.toString());
        assertThat(tokens.findByToken(newRefresh).orElseThrow().isRevoked()).isFalse();

        awaitExpiry(refreshExpiry);
        assertThatThrownBy(() -> jwt.parse(newRefresh)).isInstanceOf(ExpiredJwtException.class);
        mvc.perform(post("/api/v1/auth/refresh-token").contentType("application/json")
                        .content(mapper.writeValueAsString(Map.of("refreshToken", newRefresh))))
                .andExpect(status().isUnauthorized());
        evidence.put("refreshAfterExpiry", 401);
        // The rejection must be caused by expiration, not logout/rotation/revocation.
        assertThat(tokens.findByToken(newRefresh).orElseThrow().isRevoked()).isFalse();
        evidence.put("expiredRefreshWasNotRevoked", true);
        evidence.put("completedAt", Instant.now().toString());
        Files.writeString(Path.of("target", "token-expiration-evidence.json"),
                mapper.writerWithDefaultPrettyPrinter().writeValueAsString(evidence));
    }

    private void awaitExpiry(Instant expiry) throws InterruptedException {
        long delay = expiry.toEpochMilli() - System.currentTimeMillis() + 300;
        assertThat(delay).isLessThan(15000);
        if (delay > 0) Thread.sleep(delay);
        assertThat(Instant.now()).isAfter(expiry);
    }
}
