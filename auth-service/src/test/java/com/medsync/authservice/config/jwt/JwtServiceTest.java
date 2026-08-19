package com.medsync.authservice.config.jwt;

import com.medsync.authservice.config.properties.AuthSecurityProperties;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link JwtService}. No Spring context is loaded; the
 * {@link AuthSecurityProperties} dependency is built manually.
 */
class JwtServiceTest {

    private static final String SECRET = Base64.getEncoder()
            .encodeToString("this-is-a-test-secret-key-with-enough-length".getBytes());

    private JwtService jwtService;
    private AuthSecurityProperties properties;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        properties = new AuthSecurityProperties();
        properties.setSecretKey(SECRET);
        properties.setAccessTokenExpiration(60_000L);
        properties.setRefreshTokenExpiration(120_000L);
        properties.setClientOrigin("http://localhost:3000");

        jwtService = new JwtService(properties);

        userDetails = new User(
                "user@medsync.com",
                "encoded-password",
                List.of(
                        new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"),
                        new org.springframework.security.core.authority.SimpleGrantedAuthority("ADMIN_READ")
                )
        );
    }

    @Nested
    @DisplayName("generateAccessToken")
    class GenerateAccessToken {

        @Test
        @DisplayName("includes subject, type=access and roles claim")
        void includesExpectedClaims() {
            String token = jwtService.generateAccessToken(userDetails);

            Claims claims = jwtService.parse(token);

            assertThat(claims.getSubject()).isEqualTo("user@medsync.com");
            assertThat(claims.get("type", String.class)).isEqualTo("access");
            assertThat(claims.get("roles", List.class)).containsExactlyInAnyOrder("ROLE_ADMIN", "ADMIN_READ");
            assertThat(claims.getIssuer()).isEqualTo("medsync-auth-service");
        }

        @Test
        @DisplayName("is recognized as valid by isTokenValid for the same user")
        void isValidForSameUser() {
            String token = jwtService.generateAccessToken(userDetails);

            assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
        }
    }

    @Nested
    @DisplayName("generateRefreshToken")
    class GenerateRefreshToken {

        @Test
        @DisplayName("includes subject, type=refresh and NO roles claim")
        void includesExpectedClaims() {
            String token = jwtService.generateRefreshToken(userDetails);

            Claims claims = jwtService.parse(token);

            assertThat(claims.getSubject()).isEqualTo("user@medsync.com");
            assertThat(claims.get("type", String.class)).isEqualTo("refresh");
            assertThat(claims.get("roles")).isNull();
        }

        @Test
        @DisplayName("is recognized as a valid refresh token")
        void isRecognizedAsRefresh() {
            String token = jwtService.generateRefreshToken(userDetails);

            assertThat(jwtService.isRefreshTokenValid(token)).isTrue();
        }
    }

    @Nested
    @DisplayName("isTokenValid")
    class IsTokenValid {

        @Test
        @DisplayName("returns false when subject does not match user's username")
        void returnsFalseWhenSubjectMismatch() {
            String token = jwtService.generateAccessToken(userDetails);
            UserDetails otherUser = new User("other@medsync.com", "pwd", List.of());

            assertThat(jwtService.isTokenValid(token, otherUser)).isFalse();
        }

        @Test
        @DisplayName("returns false for a malformed token")
        void returnsFalseForMalformedToken() {
            assertThat(jwtService.isTokenValid("not-a-jwt", userDetails)).isFalse();
        }

        @Test
        @DisplayName("returns false for an expired token")
        void returnsFalseForExpiredToken() {
            properties.setAccessTokenExpiration(-10_000L); // already expired
            String expiredToken = jwtService.generateAccessToken(userDetails);

            assertThat(jwtService.isTokenValid(expiredToken, userDetails)).isFalse();
        }

        @Test
        @DisplayName("returns false when signed with a different key")
        void returnsFalseForDifferentSigningKey() {
            AuthSecurityProperties otherProps = new AuthSecurityProperties();
            otherProps.setSecretKey(Base64.getEncoder().encodeToString("another-completely-different-secret-key".getBytes()));
            otherProps.setAccessTokenExpiration(60_000L);
            otherProps.setRefreshTokenExpiration(60_000L);
            otherProps.setClientOrigin("http://localhost:3000");
            JwtService otherService = new JwtService(otherProps);

            String token = otherService.generateAccessToken(userDetails);

            assertThat(jwtService.isTokenValid(token, userDetails)).isFalse();
        }
    }

    @Nested
    @DisplayName("isRefreshTokenValid")
    class IsRefreshTokenValid {

        @Test
        @DisplayName("returns false when given an access token")
        void returnsFalseForAccessToken() {
            String accessToken = jwtService.generateAccessToken(userDetails);

            assertThat(jwtService.isRefreshTokenValid(accessToken)).isFalse();
        }

        @Test
        @DisplayName("returns false for a malformed token")
        void returnsFalseForMalformedToken() {
            assertThat(jwtService.isRefreshTokenValid("garbage")).isFalse();
        }

        @Test
        @DisplayName("returns false for an expired refresh token")
        void returnsFalseForExpiredToken() {
            properties.setRefreshTokenExpiration(-5_000L);
            String expired = jwtService.generateRefreshToken(userDetails);

            assertThat(jwtService.isRefreshTokenValid(expired)).isFalse();
        }
    }

    @Nested
    @DisplayName("parse / extractUsername")
    class ParseAndExtract {

        @Test
        @DisplayName("extractUsername returns the token subject")
        void extractsUsername() {
            String token = jwtService.generateAccessToken(userDetails);

            assertThat(jwtService.extractUsername(token)).isEqualTo("user@medsync.com");
        }

        @Test
        @DisplayName("parse throws JwtException for a tampered token")
        void throwsForTamperedToken() {
            String token = jwtService.generateAccessToken(userDetails);
            String tampered = token.substring(0, token.length() - 2) + "xx";

            org.junit.jupiter.api.Assertions.assertThrows(
                    io.jsonwebtoken.JwtException.class,
                    () -> jwtService.parse(tampered)
            );
        }
    }
}
