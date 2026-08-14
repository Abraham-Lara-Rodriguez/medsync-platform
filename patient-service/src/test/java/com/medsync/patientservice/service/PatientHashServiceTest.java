package com.medsync.patientservice.service;

import com.medsync.patientservice.domain.converter.DeterministicHasher;
import com.medsync.patientservice.domain.enums.BloodType;
import com.medsync.patientservice.domain.enums.Gender;
import com.medsync.patientservice.dto.request.CreatePatientRequest;
import com.medsync.patientservice.dto.request.UpdatePatientRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PatientHashServiceTest {

    private final PatientHashService hashService = new PatientHashService();

    @BeforeEach
    void setUp() {
        DeterministicHasher.initialize("test-hash-key-1234567890");
    }

    @Test
    void fromCreateRequestShouldHashSensitiveFieldsInStableOrder() {
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

        PatientHashService.PatientHashes hashes = hashService.fromCreateRequest(request);

        assertAll(
                () -> assertNotNull(hashes.emailHash()),
                () -> assertNotNull(hashes.documentHash()),
                () -> assertNotNull(hashes.phoneHash()),
                () -> assertEquals(64, hashes.emailHash().length()),
                () -> assertEquals(64, hashes.documentHash().length()),
                () -> assertEquals(64, hashes.phoneHash().length())
        );
    }

    @Test
    void fromUpdateRequestShouldProduceSameHashesForSameInputs() {
        UpdatePatientRequest request = new UpdatePatientRequest(
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

        PatientHashService.PatientHashes first = hashService.fromUpdateRequest(request);
        PatientHashService.PatientHashes second = hashService.fromUpdateRequest(request);

        assertAll(
                () -> assertEquals(first.emailHash(), second.emailHash()),
                () -> assertEquals(first.documentHash(), second.documentHash()),
                () -> assertEquals(first.phoneHash(), second.phoneHash())
        );
    }
}

