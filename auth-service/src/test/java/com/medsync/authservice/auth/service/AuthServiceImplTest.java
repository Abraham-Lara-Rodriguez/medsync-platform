package com.medsync.authservice.auth.service;


import com.medsync.authservice.config.jwt.JwtService;
import com.medsync.authservice.dto.auth.request.AuthRequest;
import com.medsync.authservice.dto.auth.response.AuthResponse;
import com.medsync.authservice.exception.custom.InvalidTokenException;
import com.medsync.authservice.service.auth.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

class AuthServiceImplTest {

    private final AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
    private final JwtService jwtService = mock(JwtService.class);
    private final UserDetailsService userDetailsService = mock(UserDetailsService.class);

    private final AuthServiceImpl authService =
            new AuthServiceImpl(authenticationManager, jwtService, userDetailsService);

    @Test
    void login_ReturnsTokensSuccessfully() {
        AuthRequest request = new AuthRequest("user", "password");
        UserDetails userDetails = mock(UserDetails.class);

        when(userDetailsService.loadUserByUsername("user")).thenReturn(userDetails);
        when(jwtService.generateAccessToken(userDetails)).thenReturn("ACCESS");
        when(jwtService.generateRefreshToken(userDetails)).thenReturn("REFRESH");

        AuthResponse response = authService.login(request);

        assertThat(response.accessToken()).isEqualTo("ACCESS");
        assertThat(response.refreshToken()).isEqualTo("REFRESH");

        verify(authenticationManager).authenticate(
                ArgumentMatchers.any(UsernamePasswordAuthenticationToken.class)
        );
    }

    @Test
    void refreshToken_ThrowsException_WhenTokenInvalid() {
        when(jwtService.isRefreshTokenValid("BAD")).thenReturn(false);

        assertThatThrownBy(() -> authService.refreshToken("BAD"))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void refreshToken_ReturnsNewAccessToken() {
        UserDetails userDetails = mock(UserDetails.class);

        when(jwtService.isRefreshTokenValid("REFRESH")).thenReturn(true);
        when(jwtService.extractUsername("REFRESH")).thenReturn("user");
        when(userDetailsService.loadUserByUsername("user")).thenReturn(userDetails);
        when(jwtService.generateAccessToken(userDetails)).thenReturn("NEW_ACCESS");

        AuthResponse response = authService.refreshToken("REFRESH");

        assertThat(response.accessToken()).isEqualTo("NEW_ACCESS");
        assertThat(response.refreshToken()).isEqualTo("REFRESH");
    }
}