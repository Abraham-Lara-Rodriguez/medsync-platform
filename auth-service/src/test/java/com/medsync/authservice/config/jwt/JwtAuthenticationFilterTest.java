package com.medsync.authservice.config.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link JwtAuthenticationFilter}, covering every branch of
 * {@code doFilterInternal}.
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;
    @Mock
    private UserDetailsService userDetailsService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;
    @Mock
    private Claims claims;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtService, userDetailsService);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("continues the chain without touching the security context when no Authorization header is present")
    void noAuthorizationHeader() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, userDetailsService);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("continues the chain without touching the security context when header does not start with 'Bearer '")
    void headerWithoutBearerPrefix() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic abc123");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, userDetailsService);
    }

    @Test
    @DisplayName("sets authentication in the security context for a valid access token")
    void validAccessTokenSetsAuthentication() throws Exception {
        UserDetails userDetails = new User("user@medsync.com", "pwd", List.of());

        when(request.getHeader("Authorization")).thenReturn("Bearer valid.token.value");
        when(jwtService.parse("valid.token.value")).thenReturn(claims);
        when(claims.get("type", String.class)).thenReturn("access");
        when(claims.getSubject()).thenReturn("user@medsync.com");
        when(userDetailsService.loadUserByUsername("user@medsync.com")).thenReturn(userDetails);
        when(jwtService.isTokenValid("valid.token.value", userDetails)).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(userDetails);
        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    @DisplayName("rejects with 401 and stops the chain when a refresh token is used as access token")
    void refreshTokenUsedAsAccessTokenIsRejected() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer refresh.token.value");
        when(jwtService.parse("refresh.token.value")).thenReturn(claims);
        when(claims.get("type", String.class)).thenReturn("refresh");

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(any(), any());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("does not set authentication when isTokenValid returns false, but still continues the chain")
    void tokenValidButUserDetailsMismatchDoesNotAuthenticate() throws Exception {
        UserDetails userDetails = new User("user@medsync.com", "pwd", List.of());

        when(request.getHeader("Authorization")).thenReturn("Bearer some.token.value");
        when(jwtService.parse("some.token.value")).thenReturn(claims);
        when(claims.get("type", String.class)).thenReturn("access");
        when(claims.getSubject()).thenReturn("user@medsync.com");
        when(userDetailsService.loadUserByUsername("user@medsync.com")).thenReturn(userDetails);
        when(jwtService.isTokenValid("some.token.value", userDetails)).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("does not look up user details when subject is null")
    void nullSubjectSkipsAuthentication() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer some.token.value");
        when(jwtService.parse("some.token.value")).thenReturn(claims);
        when(claims.get("type", String.class)).thenReturn("access");
        when(claims.getSubject()).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verifyNoInteractions(userDetailsService);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("does not overwrite an authentication already present in the security context")
    void existingAuthenticationIsNotOverwritten() throws Exception {
        var existingAuth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                "already-authenticated", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        when(request.getHeader("Authorization")).thenReturn("Bearer some.token.value");
        when(jwtService.parse("some.token.value")).thenReturn(claims);
        when(claims.get("type", String.class)).thenReturn("access");
        when(claims.getSubject()).thenReturn("user@medsync.com");

        filter.doFilterInternal(request, response, filterChain);

        verifyNoInteractions(userDetailsService);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(existingAuth);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("rejects with 401 and stops the chain when JwtService throws JwtException")
    void jwtExceptionResultsInUnauthorized() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer bad.token.value");
        when(jwtService.parse("bad.token.value")).thenThrow(new JwtException("invalid signature"));

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("rejects with 401 and stops the chain when JwtService throws IllegalArgumentException")
    void illegalArgumentExceptionResultsInUnauthorized() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer bad.token.value");
        when(jwtService.parse("bad.token.value")).thenThrow(new IllegalArgumentException("blank token"));

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(any(), any());
    }
}
