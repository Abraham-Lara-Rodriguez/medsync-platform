package com.medsync.appointmentservice.dto.request;

import com.medsync.appointmentservice.domain.enums.AppointmentStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateAppointmentStatusRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    @DisplayName("accepts a valid status")
    void acceptsValidStatus() {
        UpdateAppointmentStatusRequest request = new UpdateAppointmentStatusRequest(AppointmentStatus.CONFIRMED);

        Set<ConstraintViolation<UpdateAppointmentStatusRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("rejects null status")
    void rejectsNullStatus() {
        UpdateAppointmentStatusRequest request = new UpdateAppointmentStatusRequest(null);

        Set<ConstraintViolation<UpdateAppointmentStatusRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("status"));
    }

    @Test
    @DisplayName("accepts all valid status values")
    void acceptsAllValidStatusValues() {
        for (AppointmentStatus status : AppointmentStatus.values()) {
            UpdateAppointmentStatusRequest request = new UpdateAppointmentStatusRequest(status);

            Set<ConstraintViolation<UpdateAppointmentStatusRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }
    }
}
