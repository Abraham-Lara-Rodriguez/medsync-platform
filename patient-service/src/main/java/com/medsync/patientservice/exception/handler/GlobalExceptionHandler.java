package com.medsync.patientservice.exception.handler;

import com.medsync.commoncore.error.dto.ProblemDetails;
import com.medsync.commoncore.error.enums.ErrorCode;
import com.medsync.commoncore.error.handler.AbstractGlobalExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends AbstractGlobalExceptionHandler {

    private static final String BASE_TYPE = "https://patient-service/errors";

    @Override
    protected String basePath() {
        return BASE_TYPE;
    }

    @Override
    protected Logger log() {
        return log; // campo generado por @Slf4j
    }

    // =====================================================================
    // Específico de patient-service.
    // =====================================================================

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetails> handleMalformedJson(HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("Malformed JSON at {}", request.getRequestURI());
        return problem(HttpStatus.BAD_REQUEST, BASE_TYPE + "/malformed-json", "Request body contains invalid JSON.", request, ErrorCode.VALIDATION_ERROR);
    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ProblemDetails> handleBadRequest(Exception ex, HttpServletRequest request) {
        log.warn("Bad request at {}: {}", request.getRequestURI(), ex.getMessage());
        return problem(HttpStatus.BAD_REQUEST, BASE_TYPE + "/invalid-parameter", ex.getMessage(), request, ErrorCode.INVALID_PARAMETER);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ProblemDetails> handleInvalidToken(InvalidTokenException ex, HttpServletRequest request) {
        log.warn("Invalid token at {}", request.getRequestURI());
        return problem(HttpStatus.UNAUTHORIZED, BASE_TYPE + "/invalid-token", "Invalid or expired token.", request, ErrorCode.UNAUTHORIZED);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetails> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        String cause = ex.getMostSpecificCause().getMessage();
        String detail = "The operation could not be completed because it violates a database constraint.";

        if (cause != null) {
            String lower = cause.toLowerCase();
            if (lower.contains("email_hash")) {
                detail = "A patient with the same email already exists.";
            } else if (lower.contains("document_number_hash")) {
                detail = "A patient with the same document number already exists.";
            } else if (lower.contains("phone_hash")) {
                detail = "A patient with the same phone number already exists.";
            }
        }

        log.warn("Database integrity violation at {}: {}", request.getRequestURI(), cause);
        return problem(HttpStatus.CONFLICT, BASE_TYPE + "/database-constraint", detail, request, ErrorCode.DUPLICATE_RESOURCE);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ProblemDetails> handleOptimisticLockingFailure(OptimisticLockingFailureException ex, HttpServletRequest request) {
        log.warn("Optimistic locking failure at {}", request.getRequestURI());
        return problem(HttpStatus.CONFLICT, BASE_TYPE + "/concurrent-update", "Concurrent update detected. Please reload the resource and try again.", request, ErrorCode.CONFLICT);
    }
}