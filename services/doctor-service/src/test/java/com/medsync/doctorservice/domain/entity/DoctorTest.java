package com.medsync.doctorservice.domain.entity;

import com.medsync.doctorservice.domain.converter.DeterministicHasher;
import com.medsync.doctorservice.domain.enums.DoctorStatus;
import com.medsync.doctorservice.domain.enums.Specialty;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DoctorTest {

    @BeforeAll
    static void initHasher() {
        DeterministicHasher.initialize("test-secret-key-for-jwt-tests-must-be-long-enough");
    }

    @Test
    @DisplayName("Should create doctor successfully with valid parameters")
    void createDoctorSuccessfully() {
        Doctor doctor = Doctor.create(
                "Jane",
                "Smith",
                Specialty.PEDIATRICS,
                "LIC-99999",
                "jane.smith@medsync.com",
                "+9876543210"
        );

        assertNotNull(doctor);
        assertEquals("Jane", doctor.getFirstName());
        assertEquals("Smith", doctor.getLastName());
        assertEquals(Specialty.PEDIATRICS, doctor.getSpecialty());
        assertEquals(DoctorStatus.ACTIVE, doctor.getStatus());
        assertNotNull(doctor.getEmailHash());
        assertNotNull(doctor.getPhoneHash());
        assertNotNull(doctor.getMedicalLicenseHash());
    }

    @Test
    @DisplayName("Should throw exception on invalid email format")
    void invalidEmailShouldThrowException() {
        Doctor doctor = Doctor.create(
                "Jane", "Smith", Specialty.PEDIATRICS, "LIC-99999",
                "jane.smith@medsync.com", "+9876543210"
        );

        assertThrows(IllegalArgumentException.class, () -> doctor.changeEmail("invalid-email"));
    }
}
