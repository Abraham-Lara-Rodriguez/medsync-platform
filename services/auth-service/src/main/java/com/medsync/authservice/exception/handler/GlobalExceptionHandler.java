package com.medsync.authservice.exception.handler;

import com.medsync.authservice.exception.custom.InvalidTokenException;
import com.medsync.commoncore.error.dto.ProblemDetails;
import com.medsync.commoncore.error.enums.ErrorCode;
import com.medsync.commoncore.error.handler.AbstractGlobalExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler extends AbstractGlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String BASE_TYPE = "https://auth-service/errors";

    @Override
    protected String basePath() {
        return BASE_TYPE;
    }

    @Override
    protected Logger log() {
        return log;
    }

// =====================================================================
// The handlers below are specific to auth-service.
// All other common cases (validation, 404, duplicate resources,
// 401 authentication failures, 403 authorization errors, and 500
// internal errors) are handled by AbstractGlobalExceptionHandler.
// =====================================================================

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class, IllegalArgumentException.class})
    public ResponseEntity<ProblemDetails> handleBadRequest(Exception ex, HttpServletRequest req) {
        return problem(HttpStatus.BAD_REQUEST, BASE_TYPE + "/bad-request", "Invalid request body or parameter.", req, ErrorCode.BAD_REQUEST);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ProblemDetails> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        HttpHeaders headers = new HttpHeaders();
        if (ex.getSupportedHttpMethods() != null) {
            headers.setAllow(ex.getSupportedHttpMethods());
        }
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).headers(headers)
                .body(buildProblem(BASE_TYPE + "/method-not-allowed", HttpStatus.METHOD_NOT_ALLOWED,
                        "HTTP method is not supported for this endpoint.", req, ErrorCode.BAD_REQUEST));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ProblemDetails> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex, HttpServletRequest req) {
        return problem(HttpStatus.UNSUPPORTED_MEDIA_TYPE, BASE_TYPE + "/unsupported-media-type",
                "Content-Type is not supported for this endpoint.", req, ErrorCode.BAD_REQUEST);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ProblemDetails> handleConcurrentModification(OptimisticLockingFailureException ex, HttpServletRequest req) {
        return problem(HttpStatus.CONFLICT, BASE_TYPE + "/concurrent-modification",
                "The resource was modified by another request. Reload its state before continuing.", req, ErrorCode.CONFLICT);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetails> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest req) {
        log.warn("Conflict: {}", ex.getMessage());
        return problem(HttpStatus.CONFLICT, BASE_TYPE + "/conflict", "Unique constraint violated", req, ErrorCode.DUPLICATE_RESOURCE);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ProblemDetails> handleInvalidToken(InvalidTokenException ex, HttpServletRequest req) {
        log.warn("invalid_token: {}", ex.getMessage());
        return problem(HttpStatus.UNAUTHORIZED, BASE_TYPE + "/invalid-token", "Invalid or expired token", req, ErrorCode.UNAUTHORIZED);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ProblemDetails> handleAuthenticationException(AuthenticationException ex, HttpServletRequest req) {
        return handleBadCredentials(new BadCredentialsException("Invalid username or password."), req);
    }

}
