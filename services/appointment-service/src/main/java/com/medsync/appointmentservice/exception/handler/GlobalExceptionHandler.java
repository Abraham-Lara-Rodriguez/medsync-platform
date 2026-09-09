package com.medsync.appointmentservice.exception.handler;

import com.medsync.appointmentservice.exception.custom.*;
import com.medsync.commoncore.error.dto.ProblemDetails;
import com.medsync.commoncore.error.enums.ErrorCode;
import com.medsync.commoncore.error.handler.AbstractGlobalExceptionHandler;
import feign.FeignException;
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

    private static final String BASE_TYPE = "https://appointment-service/errors";

    @Override
    protected String basePath() {
        return BASE_TYPE;
    }

    @Override
    protected Logger log() {
        return log; // campo generado por @Slf4j
    }

    // =====================================================================
    // Específico de appointment-service.
    // =====================================================================

    @ExceptionHandler(PatientNotFoundException.class)
    public ResponseEntity<ProblemDetails> handlePatientNotFound(PatientNotFoundException ex, HttpServletRequest request) {
        log.warn("Patient not found at {}: {}", request.getRequestURI(), ex.getMessage());
        return problem(HttpStatus.NOT_FOUND, BASE_TYPE + "/patient-not-found", ex.getMessage(), request, ErrorCode.RESOURCE_NOT_FOUND);
    }

    @ExceptionHandler(PatientInactiveException.class)
    public ResponseEntity<ProblemDetails> handlePatientInactive(PatientInactiveException ex, HttpServletRequest request) {
        log.warn("Patient inactive at {}: {}", request.getRequestURI(), ex.getMessage());
        return problem(HttpStatus.CONFLICT, BASE_TYPE + "/patient-inactive", ex.getMessage(), request, ErrorCode.CONFLICT);
    }

    @ExceptionHandler(AppointmentConflictException.class)
    public ResponseEntity<ProblemDetails> handleAppointmentConflict(AppointmentConflictException ex, HttpServletRequest request) {
        log.warn("Appointment conflict at {}: {}", request.getRequestURI(), ex.getMessage());
        return problem(HttpStatus.CONFLICT, BASE_TYPE + "/appointment-conflict", ex.getMessage(), request, ErrorCode.CONFLICT);
    }

    @ExceptionHandler(AppointmentModificationNotAllowedException.class)
    public ResponseEntity<ProblemDetails> handleAppointmentModificationNotAllowed(AppointmentModificationNotAllowedException ex, HttpServletRequest request) {
        log.warn("Appointment modification not allowed at {}: {}", request.getRequestURI(), ex.getMessage());
        return problem(HttpStatus.CONFLICT, BASE_TYPE + "/appointment-modification-not-allowed", ex.getMessage(), request, ErrorCode.CONFLICT);
    }

    @ExceptionHandler(InvalidAppointmentScheduleException.class)
    public ResponseEntity<ProblemDetails> handleInvalidAppointmentSchedule(InvalidAppointmentScheduleException ex, HttpServletRequest request) {
        log.warn("Invalid appointment schedule at {}: {}", request.getRequestURI(), ex.getMessage());
        return problem(HttpStatus.BAD_REQUEST, BASE_TYPE + "/invalid-schedule", ex.getMessage(), request, ErrorCode.VALIDATION_ERROR);
    }

    @ExceptionHandler(InvalidAppointmentStatusTransitionException.class)
    public ResponseEntity<ProblemDetails> handleInvalidAppointmentStatusTransition(InvalidAppointmentStatusTransitionException ex, HttpServletRequest request) {
        log.warn("Invalid appointment status transition at {}: {}", request.getRequestURI(), ex.getMessage());
        return problem(HttpStatus.BAD_REQUEST, BASE_TYPE + "/invalid-status-transition", ex.getMessage(), request, ErrorCode.VALIDATION_ERROR);
    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ProblemDetails> handleBadRequest(Exception ex, HttpServletRequest request) {
        log.warn("Bad request at {}: {}", request.getRequestURI(), ex.getMessage());
        return problem(HttpStatus.BAD_REQUEST, BASE_TYPE + "/invalid-parameter", ex.getMessage(), request, ErrorCode.INVALID_PARAMETER);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetails> handleMalformedJson(HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("Malformed JSON at {}", request.getRequestURI());
        return problem(HttpStatus.BAD_REQUEST, BASE_TYPE + "/malformed-json", "Request body contains invalid JSON.", request, ErrorCode.VALIDATION_ERROR);
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ProblemDetails> handleFeignException(FeignException ex, HttpServletRequest request) {
        if (ex instanceof FeignException.NotFound) {
            log.warn("Remote resource not found at {}: {}", request.getRequestURI(), ex.getMessage());
            return problem(HttpStatus.NOT_FOUND, BASE_TYPE + "/remote-not-found",
                    "The requested resource was not found in a remote service.", request, ErrorCode.RESOURCE_NOT_FOUND);
        }
        log.error("Remote service communication error at {}: {}", request.getRequestURI(), ex.getMessage());
        return problem(HttpStatus.BAD_GATEWAY, BASE_TYPE + "/remote-service-unavailable",
                "A downstream service is unavailable or returned an unexpected error.", request, ErrorCode.INTERNAL_ERROR);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetails> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        String cause = ex.getMostSpecificCause().getMessage();
        String detail = "The operation could not be completed because it violates a database constraint.";

        if (cause != null) {
            String lower = cause.toLowerCase();
            if (lower.contains("patient_id")) {
                detail = "The referenced patient does not exist.";
            } else if (lower.contains("doctor_id")) {
                detail = "The referenced doctor does not exist.";
            } else if (lower.contains("appointment_date") || lower.contains("start_time") || lower.contains("end_time")) {
                detail = "The appointment schedule conflicts with an existing constraint.";
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

    @ExceptionHandler(PatientServiceUnavailableException.class)
    public ResponseEntity<ProblemDetails> handlePatientServiceUnavailable(PatientServiceUnavailableException ex, HttpServletRequest request) {
        log.error("Patient service unavailable at {}: {}", request.getRequestURI(), ex.getMessage());
        return problem(HttpStatus.SERVICE_UNAVAILABLE, BASE_TYPE + "/patient-service-unavailable",
                "The patient service is currently unavailable. Please try again later.", request, ErrorCode.SERVICE_UNAVAILABLE);

    }
}
