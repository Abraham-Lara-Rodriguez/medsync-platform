package com.medsync.patientservice.dto.request;

import com.medsync.patientservice.domain.enums.BloodType;
import com.medsync.patientservice.domain.enums.Gender;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UpdatePatientRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    @DisplayName("Should Accept Valid Request")
    void shouldAcceptValidRequest() {
        UpdatePatientRequest request = new UpdatePatientRequest(
                "Juan Carlos",
                "Pérez López",
                "87654321",
                Gender.MALE,
                LocalDate.of(1990, 1, 15),
                "+573007654321",
                "juancarlos@example.com",
                "Calle 456",
                BloodType.A_POSITIVE
        );

        Set<ConstraintViolation<UpdatePatientRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Should Accept Birth Date Today")
    void shouldAcceptBirthDateToday() {
        UpdatePatientRequest request = new UpdatePatientRequest(
                "Juan Carlos",
                "Pérez López",
                "87654321",
                Gender.MALE,
                LocalDate.now(),
                "+573007654321",
                "juancarlos@example.com",
                "Calle 456",
                BloodType.A_POSITIVE
        );

        Set<ConstraintViolation<UpdatePatientRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Should Reject Invalid Request")
    void shouldRejectInvalidRequest() {
        UpdatePatientRequest request = new UpdatePatientRequest(
                "",
                "",
                "abc",
                null,
                null,
                "ABC123",
                "invalid-email",
                "a".repeat(256),
                null
        );

        Set<ConstraintViolation<UpdatePatientRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("firstName")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("lastName")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("documentNumber")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("gender")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("birthDate")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("phone")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("address")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("bloodType")));
    }

    @Test
    @DisplayName("Should Reject Invalid Document Number And Phone Format")
    void shouldRejectInvalidDocumentNumberAndPhoneFormat() {
        UpdatePatientRequest request = new UpdatePatientRequest(
                "Juan Carlos",
                "Pérez López",
                "abc",
                Gender.MALE,
                LocalDate.of(1990, 1, 15),
                "ABC123",
                "juancarlos@example.com",
                "Calle 456",
                BloodType.A_POSITIVE
        );

        Set<ConstraintViolation<UpdatePatientRequest>> violations = validator.validate(request);

        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("documentNumber")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("phone")));
    }
}

