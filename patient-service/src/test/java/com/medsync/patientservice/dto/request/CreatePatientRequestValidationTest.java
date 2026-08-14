package com.medsync.patientservice.dto.request;

import com.medsync.patientservice.domain.enums.BloodType;
import com.medsync.patientservice.domain.enums.Gender;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreatePatientRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void shouldAcceptValidRequest() {
        CreatePatientRequest request = new CreatePatientRequest(
                "Juan",
                "Pérez",
                "12345678",
                Gender.MALE,
                LocalDate.of(1990, 1, 15),
                "+573001234567",
                "juan@example.com",
                "Calle 123",
                BloodType.O_POSITIVE
        );

        Set<ConstraintViolation<CreatePatientRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldRejectInvalidRequest() {
        CreatePatientRequest request = new CreatePatientRequest(
                "",
                "",
                "",
                null,
                null,
                "",
                "invalid-email",
                "a".repeat(256),
                null
        );

        Set<ConstraintViolation<CreatePatientRequest>> violations = validator.validate(request);

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
}


