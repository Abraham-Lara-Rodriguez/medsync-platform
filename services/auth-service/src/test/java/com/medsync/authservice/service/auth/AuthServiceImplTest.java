package com.medsync.authservice.service.auth;

import com.medsync.authservice.config.jwt.JwtService;
import com.medsync.authservice.dto.auth.request.AuthRequest;
import com.medsync.authservice.dto.auth.response.AuthResponse;
import com.medsync.authservice.exception.custom.InvalidTokenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;
    @Mock
    private UserDetailsService userDetailsService;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(authenticationManager, jwtService, userDetailsService);
    }

    @Nested
    @DisplayName("login")
    class Login {

        @Test
        @DisplayName("authenticates credentials and returns access + refresh tokens")
        void loginSuccess() {
            AuthRequest request = new AuthRequest("user@medsync.com", "secret");
            UserDetails userDetails = new User("user@medsync.com", "encoded", List.of());

            when(userDetailsService.loadUserByUsername("user@medsync.com")).thenReturn(userDetails);
            when(jwtService.generateAccessToken(userDetails)).thenReturn("access-token");
            when(jwtService.generateRefreshToken(userDetails)).thenReturn("refresh-token");

            AuthResponse response = authService.login(request);

            assertThat(response.accessToken()).isEqualTo("access-token");
            assertThat(response.refreshToken()).isEqualTo("refresh-token");

            ArgumentCaptor<UsernamePasswordAuthenticationToken> captor =
                    ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
            verify(authenticationManager).authenticate(captor.capture());
            assertThat(captor.getValue().getPrincipal()).isEqualTo("user@medsync.com");
            assertThat(captor.getValue().getCredentials()).isEqualTo("secret");
        }

        @Test
        @DisplayName("propagates BadCredentialsException without generating tokens")
        void loginFailsWithBadCredentials() {
            AuthRequest request = new AuthRequest("user@medsync.com", "wrong-password");
            doThrow(new BadCredentialsException("Bad credentials"))
                    .when(authenticationManager).authenticate(any());

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BadCredentialsException.class);

            verifyNoInteractions(jwtService);
            verify(userDetailsService, never()).loadUserByUsername(anyString());
        }
    }

    @Nested
    @DisplayName("refreshToken")
    class RefreshTokenTests {

        @Test
        @DisplayName("issues a new access token and keeps the same refresh token when valid")
        void refreshSuccess() {
            UserDetails userDetails = new User("user@medsync.com", "encoded", List.of());

            when(jwtService.isRefreshTokenValid("valid-refresh")).thenReturn(true);
            when(jwtService.extractUsername("valid-refresh")).thenReturn("user@medsync.com");
            when(userDetailsService.loadUserByUsername("user@medsync.com")).thenReturn(userDetails);
            when(jwtService.generateAccessToken(userDetails)).thenReturn("new-access-token");

            AuthResponse response = authService.refreshToken("valid-refresh");

            assertThat(response.accessToken()).isEqualTo("new-access-token");
            assertThat(response.refreshToken()).isEqualTo("valid-refresh");
            verify(jwtService, never()).generateRefreshToken(any());
        }

        @Test
        @DisplayName("throws InvalidTokenException when the refresh token is not valid")
        void refreshFailsWithInvalidToken() {
            when(jwtService.isRefreshTokenValid("bad-token")).thenReturn(false);

            assertThatThrownBy(() -> authService.refreshToken("bad-token"))
                    .isInstanceOf(InvalidTokenException.class)
                    .hasMessage("Invalid refresh token");

            verify(jwtService, never()).extractUsername(any());
            verifyNoInteractions(userDetailsService);
        }
    }
}
