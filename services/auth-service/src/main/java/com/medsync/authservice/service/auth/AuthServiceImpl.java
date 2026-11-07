package com.medsync.authservice.service.auth;

import com.medsync.authservice.config.jwt.JwtService;
import com.medsync.authservice.domain.entity.RefreshToken;
import com.medsync.authservice.domain.entity.User;
import com.medsync.authservice.dto.auth.request.AuthRequest;
import com.medsync.authservice.dto.auth.request.LogoutRequest;
import com.medsync.authservice.dto.auth.request.RefreshTokenRequest;
import com.medsync.authservice.dto.auth.response.AuthResponse;
import com.medsync.authservice.exception.custom.InvalidTokenException;
import com.medsync.authservice.repository.auth.RefreshTokenRepository;
import com.medsync.authservice.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password."));
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());

        String access = jwtService.generateAccessToken(userDetails);
        String refresh = jwtService.generateRefreshToken(userDetails);

        // Persistent fresh token
        RefreshToken refreshTokenEntity = RefreshToken.create(
                user,
                refresh,
                false,
                jwtService.getRefreshTokenExpiryDate());
        refreshTokenRepository.save(refreshTokenEntity);
        return new AuthResponse(access, refresh);
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest refreshToken) {
        String incomingToken = refreshToken.refreshToken();

        if (!jwtService.isRefreshTokenValid(incomingToken)) {
            throw new InvalidTokenException("Invalid refresh token type or format.");
        }

        RefreshToken storedToken = refreshTokenRepository.findByToken(incomingToken)
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found in database."));

        if (storedToken.isRevoked()) {
            throw new InvalidTokenException("Refresh token has been revoked.");
        }

        if (storedToken.getExpiresAt().isBefore(Instant.now())) {
            storedToken.setRevoked(true);
            refreshTokenRepository.save(storedToken);
            throw new InvalidTokenException("Refresh token has expired.");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(jwtService.extractUsername(incomingToken));
        new AccountStatusUserDetailsChecker().check(userDetails);
        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        String newAccess = jwtService.generateAccessToken(userDetails);
        String newRefresh = jwtService.generateRefreshToken(userDetails);


        RefreshToken NewrefreshTokenEntity = RefreshToken.create(
                storedToken.getUser(),
                newRefresh,
                false,
                jwtService.getRefreshTokenExpiryDate());
        refreshTokenRepository.save(NewrefreshTokenEntity);
        return new AuthResponse(newAccess, newRefresh);
    }

    @Override
    @Transactional
    public void logout(LogoutRequest request) {
        String token = request.refreshToken();
        refreshTokenRepository.findByToken(token).ifPresent(storedToken -> {
            if (!storedToken.isRevoked()) {
                storedToken.setRevoked(true);
                refreshTokenRepository.save(storedToken);
            }
        });
    }
}
