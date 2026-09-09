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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CreateAppointmentRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    @DisplayName("accepts a valid request with all required fields")
    void acceptsValidRequest() {
        CreateAppointmentRequest request = new CreateAppointmentRequest(
                UUID.randomUUID(), UUID.randomUUID(),
                LocalDate.now().plusDays(1), LocalTime.of(10, 0), LocalTime.of(10, 30),
                AppointmentType.GENERAL, "Checkup", "Notes"
        );

        Set<ConstraintViolation<CreateAppointmentRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("accepts a valid request with null optional fields")
    void acceptsRequestWithNullOptionalFields() {
        CreateAppointmentRequest request = new CreateAppointmentRequest(
                UUID.randomUUID(), UUID.randomUUID(),
                LocalDate.now().plusDays(1), LocalTime.of(10, 0), LocalTime.of(10, 30),
                AppointmentType.GENERAL, null, null
        );

        Set<ConstraintViolation<CreateAppointmentRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("rejects null patientId")
    void rejectsNullPatientId() {
        CreateAppointmentRequest request = new CreateAppointmentRequest(
                null, UUID.randomUUID(),
                LocalDate.now().plusDays(1), LocalTime.of(10, 0), LocalTime.of(10, 30),
                AppointmentType.GENERAL, "Checkup", "Notes"
        );

        Set<ConstraintViolation<CreateAppointmentRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("patientId"));
    }

    @Test
    @DisplayName("rejects null doctorId")
    void rejectsNullDoctorId() {
        CreateAppointmentRequest request = new CreateAppointmentRequest(
                UUID.randomUUID(), null,
                LocalDate.now().plusDays(1), LocalTime.of(10, 0), LocalTime.of(10, 30),
                AppointmentType.GENERAL, "Checkup", "Notes"
        );

        Set<ConstraintViolation<CreateAppointmentRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("doctorId"));
    }

    @Test
    @DisplayName("rejects null appointmentDate")
    void rejectsNullAppointmentDate() {
        CreateAppointmentRequest request = new CreateAppointmentRequest(
                UUID.randomUUID(), UUID.randomUUID(),
                null, LocalTime.of(10, 0), LocalTime.of(10, 30),
                AppointmentType.GENERAL, "Checkup", "Notes"
        );

        Set<ConstraintViolation<CreateAppointmentRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("appointmentDate"));
    }

    @Test
    @DisplayName("rejects appointmentDate in the past")
    void rejectsPastAppointmentDate() {
        CreateAppointmentRequest request = new CreateAppointmentRequest(
                UUID.randomUUID(), UUID.randomUUID(),
                LocalDate.now().minusDays(1), LocalTime.of(10, 0), LocalTime.of(10, 30),
                AppointmentType.GENERAL, "Checkup", "Notes"
        );

        Set<ConstraintViolation<CreateAppointmentRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("appointmentDate"));
    }

    @Test
    @DisplayName("rejects null startTime")
    void rejectsNullStartTime() {
        CreateAppointmentRequest request = new CreateAppointmentRequest(
                UUID.randomUUID(), UUID.randomUUID(),
                LocalDate.now().plusDays(1), null, LocalTime.of(10, 30),
                AppointmentType.GENERAL, "Checkup", "Notes"
        );

        Set<ConstraintViolation<CreateAppointmentRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("startTime"));
    }

    @Test
    @DisplayName("rejects null endTime")
    void rejectsNullEndTime() {
        CreateAppointmentRequest request = new CreateAppointmentRequest(
                UUID.randomUUID(), UUID.randomUUID(),
                LocalDate.now().plusDays(1), LocalTime.of(10, 0), null,
                AppointmentType.GENERAL, "Checkup", "Notes"
        );

        Set<ConstraintViolation<CreateAppointmentRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("endTime"));
    }

    @Test
    @DisplayName("rejects null type")
    void rejectsNullType() {
        CreateAppointmentRequest request = new CreateAppointmentRequest(
                UUID.randomUUID(), UUID.randomUUID(),
                LocalDate.now().plusDays(1), LocalTime.of(10, 0), LocalTime.of(10, 30),
                null, "Checkup", "Notes"
        );

        Set<ConstraintViolation<CreateAppointmentRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("type"));
    }

    @Test
    @DisplayName("rejects reason exceeding max length")
    void rejectsReasonTooLong() {
        CreateAppointmentRequest request = new CreateAppointmentRequest(
                UUID.randomUUID(), UUID.randomUUID(),
                LocalDate.now().plusDays(1), LocalTime.of(10, 0), LocalTime.of(10, 30),
                AppointmentType.GENERAL, "x".repeat(501), "Notes"
        );

        Set<ConstraintViolation<CreateAppointmentRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("reason"));
    }

    @Test
    @DisplayName("rejects notes exceeding max length")
    void rejectsNotesTooLong() {
        CreateAppointmentRequest request = new CreateAppointmentRequest(
                UUID.randomUUID(), UUID.randomUUID(),
                LocalDate.now().plusDays(1), LocalTime.of(10, 0), LocalTime.of(10, 30),
                AppointmentType.GENERAL, "Checkup", "x".repeat(2001)
        );

        Set<ConstraintViolation<CreateAppointmentRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("notes"));
    }

    @Test
    @DisplayName("rejects all required fields when null")
    void rejectsAllRequiredFieldsWhenNull() {
        CreateAppointmentRequest request = new CreateAppointmentRequest(
                null, null, null, null, null, null, null, null
        );

        Set<ConstraintViolation<CreateAppointmentRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("patientId"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("doctorId"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("appointmentDate"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("startTime"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("endTime"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("type"));
    }
}
