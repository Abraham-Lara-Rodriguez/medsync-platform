package com.medsync.patientservice.service;

import com.medsync.commoncore.error.custom.DuplicateResourceException;
import com.medsync.patientservice.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientUniquenessValidatorTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PatientUniquenessValidator validator;

    private PatientHashService.PatientHashes hashes;
    private UUID patientId;

    @BeforeEach
    void setUp() {
        hashes = new PatientHashService.PatientHashes("email-hash", "document-hash", "phone-hash");
        patientId = UUID.randomUUID();
    }

    @Test
    void validateForCreateShouldPassWhenNothingExists() {
        when(patientRepository.existsByEmailHash(any())).thenReturn(false);
        when(patientRepository.existsByDocumentNumberHash(any())).thenReturn(false);
        when(patientRepository.existsByPhoneHash(any())).thenReturn(false);

        assertDoesNotThrow(() -> validator.validateForCreate(hashes));
    }

    @Test
    void validateForCreateShouldFailOnDuplicateEmail() {
        when(patientRepository.existsByEmailHash(any())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> validator.validateForCreate(hashes));
    }

    @Test
    void validateForUpdateShouldFailOnDuplicateDocument() {
        when(patientRepository.existsByEmailHashAndIdNot(any(), any())).thenReturn(false);
        when(patientRepository.existsByDocumentNumberHashAndIdNot(any(), any())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> validator.validateForUpdate(patientId, hashes));
    }
}

