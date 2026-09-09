package com.medsync.authservice.controller.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medsync.authservice.dto.auth.request.AuthRequest;
import com.medsync.authservice.dto.auth.request.RefreshTokenRequest;
import com.medsync.authservice.dto.auth.response.AuthResponse;
import com.medsync.authservice.exception.custom.InvalidTokenException;
import com.medsync.authservice.service.auth.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer tests for {@link AuthController}. The security filter chain is
 * disabled here (login/refresh are public endpoints); authorization is
 * exercised separately in the integration tests.
 */
@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @Nested
    class Login {

        @Test
        @DisplayName("POST /api/v1/auth/login returns 200 with the tokens on valid credentials")
        void loginReturnsTokens() throws Exception {
            AuthRequest request = new AuthRequest("user@medsync.com", "secret123");
            when(authService.login(any(AuthRequest.class))).thenReturn(new AuthResponse("access", "refresh"));

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value("access"))
                    .andExpect(jsonPath("$.refreshToken").value("refresh"));
        }

        @Test
        @DisplayName("POST /api/v1/auth/login returns 400 when email is blank")
        void loginRejectsBlankEmail() throws Exception {
            AuthRequest request = new AuthRequest("", "secret123");

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /api/v1/auth/login returns 400 when email is not well-formed")
        void loginRejectsInvalidEmail() throws Exception {
            AuthRequest request = new AuthRequest("not-an-email", "secret123");

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /api/v1/auth/login returns 400 when password is blank")
        void loginRejectsBlankPassword() throws Exception {
            AuthRequest request = new AuthRequest("user@medsync.com", "");

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /api/v1/auth/login returns 401 when credentials are invalid")
        void loginReturns401OnBadCredentials() throws Exception {
            AuthRequest request = new AuthRequest("user@medsync.com", "wrong-password");
            when(authService.login(any(AuthRequest.class))).thenThrow(new BadCredentialsException("Bad credentials"));

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class RefreshToken {

        @Test
        @DisplayName("POST /api/v1/auth/refresh-token returns 200 with new access token")
        void refreshReturnsNewAccessToken() throws Exception {
            RefreshTokenRequest request = new RefreshTokenRequest("a-valid-refresh-token");
            when(authService.refreshToken("a-valid-refresh-token"))
                    .thenReturn(new AuthResponse("new-access", "a-valid-refresh-token"));

            mockMvc.perform(post("/api/v1/auth/refresh-token")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value("new-access"))
                    .andExpect(jsonPath("$.refreshToken").value("a-valid-refresh-token"));
        }

        @Test
        @DisplayName("POST /api/v1/auth/refresh-token returns 400 when the token is blank")
        void refreshRejectsBlankToken() throws Exception {
            RefreshTokenRequest request = new RefreshTokenRequest("");

            mockMvc.perform(post("/api/v1/auth/refresh-token")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /api/v1/auth/refresh-token returns 401 when the token is invalid")
        void refreshReturns401OnInvalidToken() throws Exception {
            RefreshTokenRequest request = new RefreshTokenRequest("invalid-token");
            when(authService.refreshToken("invalid-token")).thenThrow(new InvalidTokenException("Invalid refresh token"));

            mockMvc.perform(post("/api/v1/auth/refresh-token")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }
}
