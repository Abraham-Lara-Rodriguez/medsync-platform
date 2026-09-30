package com.medsync.doctorservice.dto.request;

import com.medsync.doctorservice.domain.enums.Specialty;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateDoctorRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Should validate valid request successfully")
    void validRequestShouldPass() {
        CreateDoctorRequest request = new CreateDoctorRequest(
                "John",
                "Doe",
                Specialty.CARDIOLOGY,
                "MED-12345",
                "john.doe@medsync.com",
                "+1234567890"
        );
        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    @DisplayName("Should fail when required fields are blank")
    void blankFieldsShouldFail() {
        CreateDoctorRequest request = new CreateDoctorRequest(
                "", "", null, "", "invalid-email", ""
        );
        assertFalse(validator.validate(request).isEmpty());
    }
}
