package com.medsync.appointmentservice.dto.request;

import com.medsync.appointmentservice.domain.enums.AppointmentType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateAppointmentRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    @DisplayName("accepts a valid request with all required fields")
    void acceptsValidRequest() {
        UpdateAppointmentRequest request = new UpdateAppointmentRequest(
                LocalDate.now().plusDays(1), LocalTime.of(10, 0), LocalTime.of(10, 30),
                AppointmentType.GENERAL, "Checkup", "Notes"
        );

        Set<ConstraintViolation<UpdateAppointmentRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("accepts a valid request with null optional fields")
    void acceptsRequestWithNullOptionalFields() {
        UpdateAppointmentRequest request = new UpdateAppointmentRequest(
                LocalDate.now().plusDays(1), LocalTime.of(10, 0), LocalTime.of(10, 30),
                AppointmentType.GENERAL, null, null
        );

        Set<ConstraintViolation<UpdateAppointmentRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("rejects null appointmentDate")
    void rejectsNullAppointmentDate() {
        UpdateAppointmentRequest request = new UpdateAppointmentRequest(
                null, LocalTime.of(10, 0), LocalTime.of(10, 30),
                AppointmentType.GENERAL, "Checkup", "Notes"
        );

        Set<ConstraintViolation<UpdateAppointmentRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("appointmentDate"));
    }

    @Test
    @DisplayName("rejects null startTime")
    void rejectsNullStartTime() {
        UpdateAppointmentRequest request = new UpdateAppointmentRequest(
                LocalDate.now().plusDays(1), null, LocalTime.of(10, 30),
                AppointmentType.GENERAL, "Checkup", "Notes"
        );

        Set<ConstraintViolation<UpdateAppointmentRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("startTime"));
    }

    @Test
    @DisplayName("rejects null endTime")
    void rejectsNullEndTime() {
        UpdateAppointmentRequest request = new UpdateAppointmentRequest(
                LocalDate.now().plusDays(1), LocalTime.of(10, 0), null,
                AppointmentType.GENERAL, "Checkup", "Notes"
        );

        Set<ConstraintViolation<UpdateAppointmentRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("endTime"));
    }

    @Test
    @DisplayName("rejects null type")
    void rejectsNullType() {
        UpdateAppointmentRequest request = new UpdateAppointmentRequest(
                LocalDate.now().plusDays(1), LocalTime.of(10, 0), LocalTime.of(10, 30),
                null, "Checkup", "Notes"
        );

        Set<ConstraintViolation<UpdateAppointmentRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("type"));
    }

    @Test
    @DisplayName("rejects reason exceeding max length")
    void rejectsReasonTooLong() {
        UpdateAppointmentRequest request = new UpdateAppointmentRequest(
                LocalDate.now().plusDays(1), LocalTime.of(10, 0), LocalTime.of(10, 30),
                AppointmentType.GENERAL, "x".repeat(501), "Notes"
        );

        Set<ConstraintViolation<UpdateAppointmentRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("reason"));
    }

    @Test
    @DisplayName("rejects notes exceeding max length")
    void rejectsNotesTooLong() {
        UpdateAppointmentRequest request = new UpdateAppointmentRequest(
                LocalDate.now().plusDays(1), LocalTime.of(10, 0), LocalTime.of(10, 30),
                AppointmentType.GENERAL, "Checkup", "x".repeat(2001)
        );

        Set<ConstraintViolation<UpdateAppointmentRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("notes"));
    }

    @Test
    @DisplayName("rejects all required fields when null")
    void rejectsAllRequiredFieldsWhenNull() {
        UpdateAppointmentRequest request = new UpdateAppointmentRequest(
                null, null, null, null, null, null
        );

        Set<ConstraintViolation<UpdateAppointmentRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("appointmentDate"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("startTime"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("endTime"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("type"));
    }
}
