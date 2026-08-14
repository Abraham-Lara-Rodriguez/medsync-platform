package com.medsync.commoncore.error.handler;

import com.medsync.commoncore.error.custom.DuplicateResourceException;
import com.medsync.commoncore.error.custom.ResourceNotFoundException;
import com.medsync.commoncore.error.dto.ProblemDetails;
import com.medsync.commoncore.error.enums.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException; // <- el correcto (Spring Security), no java.nio.file
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.stream.Collectors;

/**
 * Shared error-handling behavior for every service's GlobalExceptionHandler.
 * Only handlers whose behavior is genuinely identical across services live here.
 * Anything with service-specific wording or logic (malformed JSON,
 * DataIntegrityViolationException per-column messages, InvalidTokenException, etc.)
 * stays in each service's own @RestControllerAdvice subclass.
 */
public abstract class AbstractGlobalExceptionHandler {

    protected abstract String basePath();

    protected abstract Logger log();

    protected ProblemDetails buildProblem(String type, HttpStatus status, String detail,
                                          HttpServletRequest request, ErrorCode code) {
        return new ProblemDetails(
                type,
                status.getReasonPhrase(),
                status.value(),
                detail,
                request != null ? request.getRequestURI() : null,
                Instant.now(),
                code
        );
    }

    protected ResponseEntity<ProblemDetails> problem(HttpStatus status, String type, String detail,
                                                     HttpServletRequest request, ErrorCode code) {
        return ResponseEntity.status(status).body(buildProblem(type, status, detail, request, code));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetails> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining("; "));
        if (detail.isBlank()) {
            detail = "Request body validation failed.";
        }
        log().warn("Validation error at {}: {}", request.getRequestURI(), detail);
        return problem(HttpStatus.BAD_REQUEST, basePath() + "/validation-error", detail, request, ErrorCode.VALIDATION_ERROR);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetails> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        String detail = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining("; "));
        if (detail.isBlank()) {
            detail = "Validation failed for one or more constraints.";
        }
        log().warn("Constraint violation at {}: {}", request.getRequestURI(), detail);
        return problem(HttpStatus.BAD_REQUEST, basePath() + "/validation-error", detail, request, ErrorCode.VALIDATION_ERROR);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ProblemDetails> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        log().warn("Invalid credentials at {}", request.getRequestURI());
        return problem(HttpStatus.UNAUTHORIZED, basePath() + "/invalid-credentials", "Invalid username or password.", request, ErrorCode.INVALID_CREDENTIALS);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetails> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        log().warn("Access denied at {}", request.getRequestURI());
        return problem(HttpStatus.FORBIDDEN, basePath() + "/forbidden", "Access denied.", request, ErrorCode.FORBIDDEN);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetails> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        log().warn("Resource not found at {}: {}", request.getRequestURI(), ex.getMessage());
        return problem(HttpStatus.NOT_FOUND, basePath() + "/resource-not-found", ex.getMessage(), request, ErrorCode.RESOURCE_NOT_FOUND);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ProblemDetails> handleDuplicateResource(DuplicateResourceException ex, HttpServletRequest request) {
        log().warn("Duplicate resource at {}: {}", request.getRequestURI(), ex.getMessage());
        return problem(HttpStatus.CONFLICT, basePath() + "/duplicate-resource", ex.getMessage(), request, ErrorCode.DUPLICATE_RESOURCE);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetails> handleUnexpected(Exception ex, HttpServletRequest request) {
        log().error("Unhandled error at {}", request.getRequestURI(), ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, basePath() + "/internal-error", "An unexpected error occurred.", request, ErrorCode.INTERNAL_ERROR);
    }
}