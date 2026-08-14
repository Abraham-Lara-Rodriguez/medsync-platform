package com.medsync.authservice.service.auth;

import com.medsync.authservice.config.jwt.JwtService;
import com.medsync.authservice.dto.auth.request.AuthRequest;
import com.medsync.authservice.dto.auth.response.AuthResponse;
import com.medsync.authservice.exception.custom.InvalidTokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());
        String access = jwtService.generateAccessToken(userDetails);
        String refresh = jwtService.generateRefreshToken(userDetails);
        return new AuthResponse(access, refresh);
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtService.isRefreshTokenValid(refreshToken)) {
            throw new InvalidTokenException("Invalid refresh token");
        }
        String username = jwtService.extractUsername(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        String newAccess = jwtService.generateAccessToken(userDetails);
        /*
         * NOTE: Refresh rotation is disabled. In real production scenarios it should be enabled.
         * String newRefreshToken = jwtService.generateRefreshToken(userDetails);
         * return new AuthResponse(newAccessToken, newRefreshToken);
         */
        return new AuthResponse(newAccess, refreshToken);
    }
}
