package com.medsync.authservice.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medsync.authservice.domain.entity.User;
import com.medsync.authservice.domain.enums.Role;
import com.medsync.authservice.domain.enums.UserStatus;
import com.medsync.authservice.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end integration test covering the full authentication flow against a
 * real PostgreSQL database and the real Spring Security filter chain:
 * login -> use access token on a protected endpoint -> refresh -> reject bad input.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class AuthFlowIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void seedUser() {
        userRepository.deleteAll();
        User admin = User.create("admin@medsync.com", passwordEncoder.encode("Sup3rSecret!"), Role.ADMIN);
        userRepository.save(admin);
        User inactive = User.create("inactive@medsync.com", passwordEncoder.encode("Sup3rSecret!"), Role.USER);
        inactive.changeStatus(UserStatus.INACTIVE);
        userRepository.save(inactive);
    }

    @Test
    @DisplayName("full flow: login -> access protected resource -> refresh")
    void fullAuthFlow() throws Exception {
        // 1. Login with valid credentials
        String loginBody = """
                {"email":"admin@medsync.com","password":"Sup3rSecret!"}
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andReturn();

        JsonNode tokens = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String accessToken = tokens.get("accessToken").asText();
        String refreshToken = tokens.get("refreshToken").asText();

        // 2. Access a protected, ADMIN_READ-only endpoint with the access token
        mockMvc.perform(get("/api/v1/users").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        // 3. Refresh the session
        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .contentType("application/json")
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    @DisplayName("login fails with 401 for wrong password")
    void loginFailsWithWrongPassword() throws Exception {
        String body = """
                {"email":"admin@medsync.com","password":"wrong-password"}
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("login fails with 401 for an unknown email")
    void loginFailsForUnknownUser() throws Exception {
        String body = """
                {"email":"unknown@medsync.com","password":"whatever"}
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("login fails with 401 for an INACTIVE (disabled) user")
    void loginFailsForInactiveUser() throws Exception {
        String body = """
                {"email":"inactive@medsync.com","password":"Sup3rSecret!"}
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("accessing a protected endpoint without a token returns 401")
    void protectedEndpointWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("accessing a protected endpoint with a malformed token returns 401")
    void protectedEndpointWithMalformedTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/users").header("Authorization", "Bearer not-a-real-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("refresh fails with 401 when using an access token instead of a refresh token")
    void refreshFailsWhenUsingAccessToken() throws Exception {
        String loginBody = """
                {"email":"admin@medsync.com","password":"Sup3rSecret!"}
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(loginBody))
                .andExpect(status().isOk())
                .andReturn();

        String accessToken = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("accessToken").asText();

        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .contentType("application/json")
                        .content("{\"refreshToken\":\"" + accessToken + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("public endpoints are reachable without authentication")
    void healthEndpointIsPublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }
}
