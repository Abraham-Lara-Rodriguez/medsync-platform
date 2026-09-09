package com.medsync.appointmentservice.exception.handler;

import com.medsync.appointmentservice.exception.custom.*;
import com.medsync.commoncore.error.dto.ProblemDetails;
import com.medsync.commoncore.error.enums.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        when(request.getRequestURI()).thenReturn("/api/v1/appointments");
    }

    @Test
    @DisplayName("handlePatientNotFound returns 404 with RESOURCE_NOT_FOUND code")
    void handlePatientNotFound() {
        PatientNotFoundException ex = new PatientNotFoundException("Patient not found: " + UUID.randomUUID());

        ResponseEntity<ProblemDetails> response = handler.handlePatientNotFound(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
        assertThat(response.getBody().detail()).isEqualTo(ex.getMessage());
        assertThat(response.getBody().type()).endsWith("/patient-not-found");
    }

    @Test
    @DisplayName("handlePatientInactive returns 409 with CONFLICT code")
    void handlePatientInactive() {
        PatientInactiveException ex = new PatientInactiveException("Patient is not active");

        ResponseEntity<ProblemDetails> response = handler.handlePatientInactive(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(ErrorCode.CONFLICT);
        assertThat(response.getBody().detail()).isEqualTo(ex.getMessage());
        assertThat(response.getBody().type()).endsWith("/patient-inactive");
    }

    @Test
    @DisplayName("handleAppointmentConflict returns 409 with CONFLICT code")
    void handleAppointmentConflict() {
        AppointmentConflictException ex = new AppointmentConflictException("Doctor already has an appointment");

        ResponseEntity<ProblemDetails> response = handler.handleAppointmentConflict(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(ErrorCode.CONFLICT);
        assertThat(response.getBody().detail()).isEqualTo(ex.getMessage());
        assertThat(response.getBody().type()).endsWith("/appointment-conflict");
    }

    @Test
    @DisplayName("handleAppointmentModificationNotAllowed returns 409 with CONFLICT code")
    void handleAppointmentModificationNotAllowed() {
        AppointmentModificationNotAllowedException ex = new AppointmentModificationNotAllowedException(
                "Appointment cannot be modified in status COMPLETED");

        ResponseEntity<ProblemDetails> response = handler.handleAppointmentModificationNotAllowed(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(ErrorCode.CONFLICT);
        assertThat(response.getBody().detail()).isEqualTo(ex.getMessage());
        assertThat(response.getBody().type()).endsWith("/appointment-modification-not-allowed");
    }

    @Test
    @DisplayName("handleInvalidAppointmentSchedule returns 400 with VALIDATION_ERROR code")
    void handleInvalidAppointmentSchedule() {
        InvalidAppointmentScheduleException ex = new InvalidAppointmentScheduleException(
                "Appointment must be scheduled in the future");

        ResponseEntity<ProblemDetails> response = handler.handleInvalidAppointmentSchedule(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(ErrorCode.VALIDATION_ERROR);
        assertThat(response.getBody().detail()).isEqualTo(ex.getMessage());
        assertThat(response.getBody().type()).endsWith("/invalid-schedule");
    }

    @Test
    @DisplayName("handleInvalidAppointmentStatusTransition returns 400 with VALIDATION_ERROR code")
    void handleInvalidAppointmentStatusTransition() {
        InvalidAppointmentStatusTransitionException ex = new InvalidAppointmentStatusTransitionException(
                "Only scheduled appointments can be confirmed");

        ResponseEntity<ProblemDetails> response = handler.handleInvalidAppointmentStatusTransition(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(ErrorCode.VALIDATION_ERROR);
        assertThat(response.getBody().detail()).isEqualTo(ex.getMessage());
        assertThat(response.getBody().type()).endsWith("/invalid-status-transition");
    }

    @Test
    @DisplayName("handleBadRequest returns 400 with INVALID_PARAMETER code")
    void handleBadRequest() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid parameter");

        ResponseEntity<ProblemDetails> response = handler.handleBadRequest(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(ErrorCode.INVALID_PARAMETER);
        assertThat(response.getBody().detail()).isEqualTo(ex.getMessage());
        assertThat(response.getBody().type()).endsWith("/invalid-parameter");
    }

    @Test
    @DisplayName("handleMalformedJson returns 400 with VALIDATION_ERROR code")
    void handleMalformedJson() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("Malformed JSON", null);

        ResponseEntity<ProblemDetails> response = handler.handleMalformedJson(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(ErrorCode.VALIDATION_ERROR);
        assertThat(response.getBody().detail()).isEqualTo("Request body contains invalid JSON.");
        assertThat(response.getBody().type()).endsWith("/malformed-json");
    }

    @Test
    @DisplayName("handleDataIntegrityViolation returns 409 with DUPLICATE_RESOURCE code")
    void handleDataIntegrityViolation() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("unique constraint");

        ResponseEntity<ProblemDetails> response = handler.handleDataIntegrityViolation(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(ErrorCode.DUPLICATE_RESOURCE);
        assertThat(response.getBody().detail()).isEqualTo("The operation could not be completed because it violates a database constraint.");
        assertThat(response.getBody().type()).endsWith("/database-constraint");
    }

    @Test
    @DisplayName("handleOptimisticLockingFailure returns 409 with CONFLICT code")
    void handleOptimisticLockingFailure() {
        OptimisticLockingFailureException ex = new OptimisticLockingFailureException("version conflict");

        ResponseEntity<ProblemDetails> response = handler.handleOptimisticLockingFailure(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(ErrorCode.CONFLICT);
        assertThat(response.getBody().detail()).isEqualTo("Concurrent update detected. Please reload the resource and try again.");
        assertThat(response.getBody().type()).endsWith("/concurrent-update");
    }
}