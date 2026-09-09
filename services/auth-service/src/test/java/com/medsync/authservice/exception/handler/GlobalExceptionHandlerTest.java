package com.medsync.authservice.exception.handler;


import com.medsync.authservice.exception.custom.InvalidTokenException;
import com.medsync.commoncore.error.dto.ProblemDetails;
import com.medsync.commoncore.error.enums.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        when(request.getRequestURI()).thenReturn("/api/v1/auth/login");
    }

    static Stream<Exception> badRequestExceptions() {
        return Stream.of(
                new HttpMessageNotReadableException("Malformed JSON", null),
                new IllegalArgumentException("Illegal argument")
        );
    }

    @ParameterizedTest
    @MethodSource("badRequestExceptions")
    @DisplayName("handleBadRequest returns 400 with BAD_REQUEST code")
    void handlesBadRequest(Exception ex) {
        ResponseEntity<ProblemDetails> response = handler.handleBadRequest(ex, request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(ErrorCode.BAD_REQUEST);
        assertThat(response.getBody().detail()).isEqualTo(ex.getMessage());
        assertThat(response.getBody().type()).endsWith("/bad-request");
    }

    @Test
    @DisplayName("handleDataIntegrity returns 409 CONFLICT with DUPLICATE_RESOURCE code")
    void handlesDataIntegrityViolation() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("unique constraint");

        ResponseEntity<ProblemDetails> response = handler.handleDataIntegrity(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(ErrorCode.DUPLICATE_RESOURCE);
        assertThat(response.getBody().detail()).isEqualTo("Unique constraint violated");
    }

    @Test
    @DisplayName("handleInvalidToken returns 401 UNAUTHORIZED with UNAUTHORIZED code")
    void handlesInvalidToken() {
        InvalidTokenException ex = new InvalidTokenException("Invalid refresh token");

        ResponseEntity<ProblemDetails> response = handler.handleInvalidToken(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(ErrorCode.UNAUTHORIZED);
        assertThat(response.getBody().detail()).isEqualTo("Invalid or expired token");
        assertThat(response.getBody().type()).endsWith("/invalid-token");
    }
}
