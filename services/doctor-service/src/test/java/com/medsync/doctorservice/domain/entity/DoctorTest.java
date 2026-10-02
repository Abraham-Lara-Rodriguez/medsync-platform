package com.medsync.doctorservice.domain.entity;

import com.medsync.doctorservice.domain.converter.DeterministicHasher;
import com.medsync.doctorservice.domain.constants.DoctorConstraints;
import com.medsync.doctorservice.domain.enums.DoctorStatus;
import com.medsync.doctorservice.domain.enums.Specialty;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DoctorTest {

    @BeforeAll
    static void initHasher() {
        DeterministicHasher.initialize("test-secret-key-for-jwt-tests-must-be-long-enough");
    }

    @Test
    void create_shouldCreateDoctorWithActiveStatus() {
        Doctor doctor = Doctor.create("John", "Doe", Specialty.GENERAL_MEDICINE,
                "LIC-001", "john@medsync.com", "+555123");
        assertNull(doctor.getId());
        assertEquals("John", doctor.getFirstName());
        assertEquals("Doe", doctor.getLastName());
        assertEquals(Specialty.GENERAL_MEDICINE, doctor.getSpecialty());
        assertEquals(DoctorStatus.ACTIVE, doctor.getStatus());
    }

    @Test
    void create_shouldHashMedicalLicense() {
        Doctor doctor = Doctor.create("John", "Doe", Specialty.GENERAL_MEDICINE,
                "LIC-001", "john@medsync.com", "+555123");
        assertNotNull(doctor.getMedicalLicenseHash());
        assertEquals(64, doctor.getMedicalLicenseHash().length());
    }

    @Test
    void create_shouldHashEmail() {
        Doctor doctor = Doctor.create("John", "Doe", Specialty.GENERAL_MEDICINE,
                "LIC-001", "john@medsync.com", "+555123");
        assertNotNull(doctor.getEmailHash());
        assertEquals(64, doctor.getEmailHash().length());
    }

    @Test
    void create_shouldHashPhone() {
        Doctor doctor = Doctor.create("John", "Doe", Specialty.GENERAL_MEDICINE,
                "LIC-001", "john@medsync.com", "+555123");
        assertNotNull(doctor.getPhoneHash());
        assertEquals(64, doctor.getPhoneHash().length());
    }

    @Test
    void changeFirstName_shouldThrowWhenNull() {
        Doctor doctor = Doctor.create("John", "Doe", Specialty.GENERAL_MEDICINE,
                "LIC-001", "john@medsync.com", "+555123");
        assertThrows(IllegalArgumentException.class, () -> doctor.changeFirstName(null));
    }

    @Test
    void changeFirstName_shouldThrowWhenEmpty() {
        Doctor doctor = Doctor.create("John", "Doe", Specialty.GENERAL_MEDICINE,
                "LIC-001", "john@medsync.com", "+555123");
        assertThrows(IllegalArgumentException.class, () -> doctor.changeFirstName(""));
    }

    @Test
    void changeFirstName_shouldThrowWhenTooLong() {
        Doctor doctor = Doctor.create("John", "Doe", Specialty.GENERAL_MEDICINE,
                "LIC-001", "john@medsync.com", "+555123");
        String longName = "a".repeat(DoctorConstraints.MAX_NAME_LENGTH + 1);
        assertThrows(IllegalArgumentException.class, () -> doctor.changeFirstName(longName));
    }

    @Test
    void changeFirstName_shouldNormalizeName() {
        Doctor doctor = Doctor.create("John", "Doe", Specialty.GENERAL_MEDICINE,
                "LIC-001", "john@medsync.com", "+555123");
        doctor.changeFirstName("  JOHN  ");
        assertNotEquals("John", doctor.getFirstName());
    }

    @Test
    void changeFirstName_shouldAcceptValidName() {
        Doctor doctor = Doctor.create("John", "Doe", Specialty.GENERAL_MEDICINE,
                "LIC-001", "john@medsync.com", "+555123");
        doctor.changeFirstName("Jane");
        assertEquals("Jane", doctor.getFirstName());
    }

    @Test
    void changeLastName_shouldThrowWhenNull() {
        Doctor doctor = Doctor.create("John", "Doe", Specialty.GENERAL_MEDICINE,
                "LIC-001", "john@medsync.com", "+555123");
        assertThrows(IllegalArgumentException.class, () -> doctor.changeLastName(null));
    }

    @Test
    void changeLastName_shouldThrowWhenEmpty() {
        Doctor doctor = Doctor.create("John", "Doe", Specialty.GENERAL_MEDICINE,
                "LIC-001", "john@medsync.com", "+555123");
        assertThrows(IllegalArgumentException.class, () -> doctor.changeLastName(""));
    }

    @Test
    void changeLastName_shouldThrowWhenTooLong() {
        Doctor doctor = Doctor.create("John", "Doe", Specialty.GENERAL_MEDICINE,
                "LIC-001", "john@medsync.com", "+555123");
        String longName = "a".repeat(DoctorConstraints.MAX_NAME_LENGTH + 1);
        assertThrows(IllegalArgumentException.class, () -> doctor.changeLastName(longName));
    }

    @Test
    void changeLastName_shouldNormalizeName() {
        Doctor doctor = Doctor.create("John", "Doe", Specialty.GENERAL_MEDICINE,
                "LIC-001", "john@medsync.com", "+555123");
        doctor.changeLastName("  DOE  ");
        assertNotEquals("Doe", doctor.getLastName());
    }

    @Test
    void changeLastName_shouldAcceptValidName() {
        Doctor doctor = Doctor.create("John", "Doe", Specialty.GENERAL_MEDICINE,
                "LIC-001", "john@medsync.com", "+555123");
        doctor.changeLastName("Smith");
        assertEquals("Smith", doctor.getLastName());
    }

    @Test
    void changeSpecialty_shouldThrowWhenNull() {
        Doctor doctor = Doctor.create("John", "Doe", Specialty.GENERAL_MEDICINE,
                "LIC-001", "john@medsync.com", "+555123");
        assertThrows(IllegalArgumentException.class, () -> doctor.changeSpecialty(null));
    }

    @Test
    void changeSpecialty_shouldAcceptValidSpecialty() {
        Doctor doctor = Doctor.create("John", "Doe", Specialty.GENERAL_MEDICINE,
                "LIC-001", "john@medsync.com", "+555123");
        doctor.changeSpecialty(Specialty.CARDIOLOGY);
        assertEquals(Specialty.CARDIOLOGY, doctor.getSpecialty());
    }

    @Test
    void changeMedicalLicense_shouldThrowWhenNull() {
        Doctor doctor = Doctor.create("John", "Doe", Specialty.GENERAL_MEDICINE,
                "LIC-001", "john@medsync.com", "+555123");
        assertThrows(IllegalArgumentException.class, () -> doctor.changeMedicalLicense(null));
    }

    @Test
    void changeMedicalLicense_shouldThrowWhenEmpty() {
        Doctor doctor = Doctor.create("John", "Doe", Specialty.GENERAL_MEDICINE,
                "LIC-001", "john@medsync.com", "+555123");
        assertThrows(IllegalArgumentException.class, () -> doctor.changeMedicalLicense(""));
    }

    @Test
    void changeMedicalLicense_shouldThrowWhenTooLong() {
        Doctor doctor = Doctor.create("John", "Doe", Specialty.GENERAL_MEDICINE,
                "LIC-001", "john@medsync.com", "+555123");
        String longLicense = "a".repeat(DoctorConstraints.MAX_MEDICAL_LICENSE_LENGTH + 1);
        assertThrows(IllegalArgumentException.class, () -> doctor.changeMedicalLicense(longLicense));
    }

    @Test
    void changeMedicalLicense_shouldThrowWhenInvalidCharacters() {
        Doctor doctor = Doctor.create("John", "Doe", Specialty.GENERAL_MEDICINE,
                "LIC-001", "john@medsync.com", "+555123");
        assertThrows(IllegalArgumentException.class, () -> doctor.changeMedicalLicense("LIC/001"));
    }

    @Test
    void changeMedicalLicense_shouldNormalizeAndRehash() {
        Doctor doctor = Doctor.create("John", "Doe", Specialty.GENERAL_MEDICINE,
                "LIC-001", "john@medsync.com", "+555123");
        String oldHash = doctor.getMedicalLicenseHash();
        doctor.changeMedicalLicense("LIC-999");
        assertNotEquals(oldHash, doctor.getMedicalLicenseHash());
        assertNotNull(doctor.getMedicalLicenseHash());
    }

    @Test
    void changeEmail_shouldThrowWhenNull() {
        Doctor doctor = Doctor.create("John", "Doe", Specialty.GENERAL_MEDICINE,
                "LIC-001", "john@medsync.com", "+555123");
        assertThrows(IllegalArgumentException.class, () -> doctor.changeEmail(null));
    }

    @Test
    void changeEmail_shouldThrowWhenEmpty() {
        Doctor doctor = Doctor.create("John", "Doe", Specialty.GENERAL_MEDICINE,
                "LIC-001", "john@medsync.com", "+555123");
        assertThrows(IllegalArgumentException.class, () -> doctor.changeEmail(""));
    }

    @Test
    void changeEmail_shouldThrowWhenTooLong() {
        Doctor doctor = Doctor.create("John", "Doe", Specialty.GENERAL_MEDICINE,
                "LIC-001", "john@medsync.com", "+555123");
        String longEmail = "a".repeat(DoctorConstraints.MAX_EMAIL_LENGTH + 1) + "@medsync.com";
        assertThrows(IllegalArgumentException.class, () -> doctor.changeEmail(longEmail));
    }

    @Test
    void changeEmail_shouldThrowWhenInvalidFormat() {
        Doctor doctor = Doctor.create("John", "Doe", Specialty.GENERAL_MEDICINE,
                "LIC-001", "john@medsync.com", "+555123");
        assertThrows(IllegalArgumentException.class, () -> doctor.changeEmail("invalid-email"));
    }

    @Test
    void changeEmail_shouldNormalizeAndRehash() {
        Doctor doctor = Doctor.create("John", "Doe", Specialty.GENERAL_MEDICINE,
                "LIC-001", "john@medsync.com", "+555123");
        String oldHash = doctor.getEmailHash();
        doctor.changeEmail("new@medsync.com");
        assertNotEquals(oldHash, doctor.getEmailHash());
        assertNotNull(doctor.getEmailHash());
    }

    @Test
    void changeEmail_shouldNormalizeEmail() {
        Doctor doctor = Doctor.create("John", "Doe", Specialty.GENERAL_MEDICINE,
                "LIC-001", "john@medsync.com", "+555123");
        doctor.changeEmail("  John@Test.Com  ");
        assertEquals("john@test.com", doctor.getEmail());
    }

    @Test
    void changePhone_shouldThrowWhenNull() {
        Doctor doctor = Doctor.create("John", "Doe", Specialty.GENERAL_MEDICINE,
                "LIC-001", "john@medsync.com", "+555123");
        assertThrows(IllegalArgumentException.class, () -> doctor.changePhone(null));
    }

    @Test
    void changePhone_shouldThrowWhenEmpty() {
        Doctor doctor = Doctor.create("John", "Doe", Specialty.GENERAL_MEDICINE,
                "LIC-001", "john@medsync.com", "+555123");
        assertThrows(IllegalArgumentException.class, () -> doctor.changePhone(""));
    }

    @Test
    void changePhone_shouldThrowWhenTooLong() {
        Doctor doctor = Doctor.create("John", "Doe", Specialty.GENERAL_MEDICINE,
                "LIC-001", "john@medsync.com", "+555123");
        String longPhone = "a".repeat(DoctorConstraints.MAX_PHONE_LENGTH + 1);
        assertThrows(IllegalArgumentException.class, () -> doctor.changePhone(longPhone));
    }

    @Test
    void changePhone_shouldThrowWhenInvalidCharacters() {
        Doctor doctor = Doctor.create("John", "Doe", Specialty.GENERAL_MEDICINE,
                "LIC-001", "john@medsync.com", "+555123");
        assertThrows(IllegalArgumentException.class, () -> doctor.changePhone("+5551234567x"));
    }

    @Test
    void changePhone_shouldNormalizeAndRehash() {
        Doctor doctor = Doctor.create("John", "Doe", Specialty.GENERAL_MEDICINE,
                "LIC-001", "john@medsync.com", "+555123");
        String oldHash = doctor.getPhoneHash();
        doctor.changePhone("+555999");
        assertNotEquals(oldHash, doctor.getPhoneHash());
        assertNotNull(doctor.getPhoneHash());
    }

    @Test
    void changePhone_shouldNormalizePhone() {
        Doctor doctor = Doctor.create("John", "Doe", Specialty.GENERAL_MEDICINE,
                "LIC-001", "john@medsync.com", "+555123");
        doctor.changePhone(" 555-123  ");
        assertNotNull(doctor.getPhone());
    }

    @Test
    void changeStatus_shouldThrowWhenNull() {
        Doctor doctor = Doctor.create("John", "Doe", Specialty.GENERAL_MEDICINE,
                "LIC-001", "john@medsync.com", "+555123");
        assertThrows(IllegalArgumentException.class, () -> doctor.changeStatus(null));
    }

    @Test
    void changeStatus_shouldAcceptActive() {
        Doctor doctor = Doctor.create("John", "Doe", Specialty.GENERAL_MEDICINE,
                "LIC-001", "john@medsync.com", "+555123");
        doctor.changeStatus(DoctorStatus.ACTIVE);
        assertEquals(DoctorStatus.ACTIVE, doctor.getStatus());
    }

    @Test
    void changeStatus_shouldAcceptInactive() {
        Doctor doctor = Doctor.create("John", "Doe", Specialty.GENERAL_MEDICINE,
                "LIC-001", "john@medsync.com", "+555123");
        doctor.changeStatus(DoctorStatus.INACTIVE);
        assertEquals(DoctorStatus.INACTIVE, doctor.getStatus());
    }

    @Test
    void equals_shouldReturnTrueForSameId() {
        Doctor doctor1 = Doctor.create("John", "Doe", Specialty.GENERAL_MEDICINE,
                "LIC-001", "john@medsync.com", "+555123");
        assertTrue(doctor1.equals(doctor1));
    }

    @Test
    void equals_shouldReturnFalseForDifferentObjects() {
        Doctor doctor1 = Doctor.create("John", "Doe", Specialty.GENERAL_MEDICINE,
                "LIC-001", "john@medsync.com", "+555123");
        Doctor doctor2 = Doctor.create("Jane", "Doe", Specialty.GENERAL_MEDICINE,
                "LIC-002", "jane@medsync.com", "+555124");
        assertNotEquals(doctor1, doctor2);
    }

    @Test
    void equals_shouldReturnFalseForNull() {
        Doctor doctor = Doctor.create("John", "Doe", Specialty.GENERAL_MEDICINE,
                "LIC-001", "john@medsync.com", "+555123");
        assertFalse(doctor.equals(null));
    }

    @Test
    void equals_shouldReturnFalseForDifferentClass() {
        Doctor doctor = Doctor.create("John", "Doe", Specialty.GENERAL_MEDICINE,
                "LIC-001", "john@medsync.com", "+555123");
        assertFalse(doctor.equals("not a doctor"));
    }

    @Test
    void hashCode_shouldBeConsistent() {
        Doctor doctor = Doctor.create("John", "Doe", Specialty.GENERAL_MEDICINE,
                "LIC-001", "john@medsync.com", "+555123");
        int hash1 = doctor.hashCode();
        int hash2 = doctor.hashCode();
        assertEquals(hash1, hash2);
    }

    @Test
    void hashCode_shouldBeEqualForEqualsObjects() {
        Doctor doctor1 = Doctor.create("John", "Doe", Specialty.GENERAL_MEDICINE,
                "LIC-001", "john@medsync.com", "+555123");
        Doctor doctor2 = Doctor.create("John", "Doe", Specialty.GENERAL_MEDICINE,
                "LIC-001", "john@medsync.com", "+555123");
        assertEquals(doctor1.hashCode(), doctor2.hashCode());
    }
}