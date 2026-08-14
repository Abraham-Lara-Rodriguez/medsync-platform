package com.medsync.authservice.exception.handler;

import com.medsync.authservice.exception.custom.InvalidTokenException;
import com.medsync.commoncore.error.dto.ProblemDetails;
import com.medsync.commoncore.error.enums.ErrorCode;
import com.medsync.commoncore.error.handler.AbstractGlobalExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
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
    // Lo que sigue es específico de auth-service; todo lo demás
    // (validación, 404, 409 por duplicado, 401 credenciales, 403, 500)
    // ya lo resuelve AbstractGlobalExceptionHandler.
    // =====================================================================

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class, IllegalArgumentException.class})
    public ResponseEntity<ProblemDetails> handleBadRequest(Exception ex, HttpServletRequest req) {
        return problem(HttpStatus.BAD_REQUEST, BASE_TYPE + "/bad-request", ex.getMessage(), req, ErrorCode.BAD_REQUEST);
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
}