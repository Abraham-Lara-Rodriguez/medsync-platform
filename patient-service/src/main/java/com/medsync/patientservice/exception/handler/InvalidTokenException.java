package com.medsync.patientservice.exception.handler;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a provided token is invalid or cannot be used.
 */
@Getter
public class InvalidTokenException extends RuntimeException {

    private final HttpStatus status;

    public InvalidTokenException(String message) {
        super(message);
        this.status = HttpStatus.UNAUTHORIZED; // 401 by default
    }

    public InvalidTokenException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

}
