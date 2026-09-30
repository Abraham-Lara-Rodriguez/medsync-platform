package com.medsync.doctorservice.dto.request;

import com.medsync.doctorservice.domain.enums.DoctorStatus;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UpdateDoctorRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Should validate valid update request successfully")
    void validRequestShouldPass() {
        UpdateDoctorRequest request = new UpdateDoctorRequest(
                "John",
                "Doe",
                "john.doe@medsync.com",
                "+1234567890",
                DoctorStatus.ACTIVE
        );
        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    @DisplayName("Should fail when update fields are blank or null")
    void blankFieldsShouldFail() {
        UpdateDoctorRequest request = new UpdateDoctorRequest(
                "", "", "invalid-email", "", null
        );
        assertFalse(validator.validate(request).isEmpty());
    }
}
