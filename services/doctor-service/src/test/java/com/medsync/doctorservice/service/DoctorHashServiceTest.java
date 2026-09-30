package com.medsync.doctorservice.service;

import com.medsync.doctorservice.domain.converter.DeterministicHasher;
import com.medsync.doctorservice.domain.enums.DoctorStatus;
import com.medsync.doctorservice.domain.enums.Specialty;
import com.medsync.doctorservice.dto.request.CreateDoctorRequest;
import com.medsync.doctorservice.dto.request.UpdateDoctorRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DoctorHashServiceTest {

    private final DoctorHashService hashService = new DoctorHashService();

    @BeforeAll
    static void initHasher() {
        DeterministicHasher.initialize("test-secret-key-for-jwt-tests-must-be-long-enough");
    }

    @Test
    @DisplayName("Should generate hashes from CreateDoctorRequest")
    void fromCreateRequestShouldGenerateHashes() {
        CreateDoctorRequest request = new CreateDoctorRequest(
                "John", "Doe", Specialty.CARDIOLOGY, "MED-12345",
                "john.doe@medsync.com", "+1234567890"
        );

        DoctorHashService.DoctorHashes hashes = hashService.fromCreateRequest(request);

        assertNotNull(hashes);
        assertNotNull(hashes.emailHash());
        assertNotNull(hashes.medicalLicenseHash());
        assertNotNull(hashes.phoneHash());
    }

    @Test
    @DisplayName("Should generate hashes from UpdateDoctorRequest")
    void fromUpdateRequestShouldGenerateHashes() {
        UpdateDoctorRequest request = new UpdateDoctorRequest(
                "John", "Doe", "john.doe@medsync.com", "+1234567890", DoctorStatus.ACTIVE
        );

        DoctorHashService.DoctorHashes hashes = hashService.fromUpdateRequest(request);

        assertNotNull(hashes);
        assertNotNull(hashes.emailHash());
        assertNull(hashes.medicalLicenseHash());
        assertNotNull(hashes.phoneHash());
    }
}
