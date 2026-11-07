package com.medsync.authservice.service.auth;

import com.medsync.authservice.config.jwt.JwtService;
import com.medsync.authservice.domain.entity.RefreshToken;
import com.medsync.authservice.domain.entity.User;
import com.medsync.authservice.domain.enums.auth.Role;
import com.medsync.authservice.dto.auth.request.*;
import com.medsync.authservice.exception.custom.InvalidTokenException;
import com.medsync.authservice.repository.auth.RefreshTokenRepository;
import com.medsync.authservice.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import java.time.Instant;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {
    private static final String EMAIL = "user@medsync.com";
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtService jwtService;
    @Mock private UserDetailsService userDetailsService;
    @Mock private RefreshTokenRepository tokens;
    @Mock private UserRepository users;
    private AuthServiceImpl service;
    private User user;

    @BeforeEach
    void setUp() {
        service = new AuthServiceImpl(authenticationManager, jwtService, userDetailsService, tokens, users);
        user = User.create(EMAIL, "encoded", Role.PATIENT);
    }

    @Test
    void loginPersistsRefreshToken() {
        UserDetails details = details("active");
        Instant expiry = Instant.now().plusSeconds(3600);
        when(users.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(userDetailsService.loadUserByUsername(EMAIL)).thenReturn(details);
        when(jwtService.generateAccessToken(details)).thenReturn("access");
        when(jwtService.generateRefreshToken(details)).thenReturn("refresh");
        when(jwtService.getRefreshTokenExpiryDate()).thenReturn(expiry);
        var result = service.login(new AuthRequest(EMAIL, "password"));
        assertThat(result.accessToken()).isEqualTo("access");
        assertThat(result.refreshToken()).isEqualTo("refresh");
        ArgumentCaptor<RefreshToken> saved = ArgumentCaptor.forClass(RefreshToken.class);
        verify(tokens).save(saved.capture());
        assertThat(saved.getValue().getToken()).isEqualTo("refresh");
        assertThat(saved.getValue().getUser()).isSameAs(user);
        assertThat(saved.getValue().getExpiresAt()).isEqualTo(expiry);
        assertThat(saved.getValue().isRevoked()).isFalse();
        verify(authenticationManager).authenticate(any());
    }

    @Test
    void wrongPasswordDoesNotIssueOrPersistTokens() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Invalid"));
        assertThatThrownBy(() -> service.login(new AuthRequest(EMAIL, "wrong")))
                .isInstanceOf(BadCredentialsException.class);
        verifyNoInteractions(jwtService, tokens, userDetailsService, users);
    }

    @Test
    void disabledAccountCannotLoginOrReachTokenPersistence() {
        when(authenticationManager.authenticate(any())).thenThrow(new DisabledException("Disabled"));
        assertThatThrownBy(() -> service.login(new AuthRequest(EMAIL, "password")))
                .isInstanceOf(DisabledException.class);
        verifyNoInteractions(jwtService, tokens, userDetailsService, users);
    }

    @Test
    void refreshRotatesAndRejectsReuse() {
        RefreshToken stored = stored(false, false);
        stubStored(stored);
        UserDetails details = details("active");
        when(jwtService.extractUsername("old")).thenReturn(EMAIL);
        when(userDetailsService.loadUserByUsername(EMAIL)).thenReturn(details);
        when(jwtService.generateAccessToken(details)).thenReturn("new-access");
        when(jwtService.generateRefreshToken(details)).thenReturn("new-refresh");
        Instant expiry = Instant.now().plusSeconds(7200);
        when(jwtService.getRefreshTokenExpiryDate()).thenReturn(expiry);
        var result = service.refreshToken(new RefreshTokenRequest("old"));
        assertThat(result.accessToken()).isEqualTo("new-access");
        assertThat(result.refreshToken()).isEqualTo("new-refresh");
        assertThat(stored.isRevoked()).isTrue();
        ArgumentCaptor<RefreshToken> saved = ArgumentCaptor.forClass(RefreshToken.class);
        verify(tokens, times(2)).save(saved.capture());
        RefreshToken replacement = saved.getAllValues().get(1);
        assertThat(replacement.getToken()).isEqualTo("new-refresh");
        assertThat(replacement.getUser()).isSameAs(user);
        assertThat(replacement.getExpiresAt()).isEqualTo(expiry);
        assertThat(replacement.isRevoked()).isFalse();
        assertThatThrownBy(() -> service.refreshToken(new RefreshTokenRequest("old")))
                .isInstanceOf(InvalidTokenException.class);
        verify(jwtService, times(1)).generateRefreshToken(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"disabled", "locked", "expired", "credentialsExpired"})
    void restrictedAccountCannotRefresh(String state) {
        RefreshToken stored = stored(false, false);
        stubStored(stored);
        when(jwtService.extractUsername("old")).thenReturn(EMAIL);
        when(userDetailsService.loadUserByUsername(EMAIL)).thenReturn(details(state));
        assertThatThrownBy(() -> service.refreshToken(new RefreshTokenRequest("old")))
                .isInstanceOf(AccountStatusException.class);
        assertThat(stored.isRevoked()).isFalse();
        verify(tokens, never()).save(any());
        verify(jwtService, never()).generateAccessToken(any());
        verify(jwtService, never()).generateRefreshToken(any());
    }

    @Test
    void invalidJwtDoesNotReachDatabase() {
        when(jwtService.isRefreshTokenValid("bad")).thenReturn(false);
        assertThatThrownBy(() -> service.refreshToken(new RefreshTokenRequest("bad")))
                .isInstanceOf(InvalidTokenException.class);
        verifyNoInteractions(tokens, userDetailsService);
    }

    @Test
    void unknownTokenIsRejected() {
        when(jwtService.isRefreshTokenValid("old")).thenReturn(true);
        when(tokens.findByToken("old")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.refreshToken(new RefreshTokenRequest("old")))
                .isInstanceOf(InvalidTokenException.class);
        verify(jwtService, never()).generateAccessToken(any());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void revokedOrExpiredTokenIsRejected(boolean revoked) {
        stubStored(stored(revoked, !revoked));
        assertThatThrownBy(() -> service.refreshToken(new RefreshTokenRequest("old")))
                .isInstanceOf(InvalidTokenException.class);
        verify(jwtService, never()).generateAccessToken(any());
        verify(jwtService, never()).generateRefreshToken(any());
    }

    @Test
    void logoutIsIdempotentAndBlocksRefresh() {
        RefreshToken stored = stored(false, false);
        stubStored(stored);
        service.logout(new LogoutRequest("old"));
        service.logout(new LogoutRequest("old"));
        assertThat(stored.isRevoked()).isTrue();
        verify(tokens, times(1)).save(stored);
        assertThatThrownBy(() -> service.refreshToken(new RefreshTokenRequest("old")))
                .isInstanceOf(InvalidTokenException.class);
        verify(jwtService, never()).generateRefreshToken(any());
    }

    @Test
    void logoutOfUnknownTokenDoesNothing() {
        when(tokens.findByToken("missing")).thenReturn(Optional.empty());
        assertThatCode(() -> service.logout(new LogoutRequest("missing"))).doesNotThrowAnyException();
        verify(tokens, never()).save(any());
    }

    private RefreshToken stored(boolean revoked, boolean expired) {
        return RefreshToken.create(user, "old", revoked, Instant.now().plusSeconds(expired ? -60 : 3600));
    }
    private void stubStored(RefreshToken token) {
        when(jwtService.isRefreshTokenValid("old")).thenReturn(true);
        when(tokens.findByToken("old")).thenReturn(Optional.of(token));
    }
    private UserDetails details(String state) {
        return org.springframework.security.core.userdetails.User.withUsername(EMAIL).password("encoded")
                .authorities("ROLE_PATIENT").disabled(state.equals("disabled"))
                .accountLocked(state.equals("locked")).accountExpired(state.equals("expired"))
                .credentialsExpired(state.equals("credentialsExpired")).build();
    }
}
