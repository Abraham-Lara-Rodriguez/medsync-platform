package com.medsync.patientservice.service;

import com.medsync.commoncore.error.custom.DuplicateResourceException;
import com.medsync.patientservice.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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
        hashes = new PatientHashService.PatientHashes(
                "email-hash", "document-hash", "phone-hash"
        );
        patientId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should validate For Create Should Pass When All Values Are Unique")
    void validateForCreateShouldPassWhenAllValuesAreUnique() {
        when(patientRepository.existsByEmailHash(hashes.emailHash())).thenReturn(false);
        when(patientRepository.existsByDocumentNumberHash(hashes.documentHash())).thenReturn(false);
        when(patientRepository.existsByPhoneHash(hashes.phoneHash())).thenReturn(false);

        assertDoesNotThrow(() -> validator.validateForCreate(hashes));

        verify(patientRepository).existsByEmailHash(hashes.emailHash());
        verify(patientRepository).existsByDocumentNumberHash(hashes.documentHash());
        verify(patientRepository).existsByPhoneHash(hashes.phoneHash());
    }

    @Test
    @DisplayName("Should validate For Create Should Stop At Duplicate Email")
    void validateForCreateShouldStopAtDuplicateEmail() {
        when(patientRepository.existsByEmailHash(hashes.emailHash())).thenReturn(true);

        DuplicateResourceException ex = assertThrows(
                DuplicateResourceException.class,
                () -> validator.validateForCreate(hashes)
        );

        assertEquals("Email already exists", ex.getMessage());
        verify(patientRepository, never()).existsByDocumentNumberHash(any());
        verify(patientRepository, never()).existsByPhoneHash(any());
    }

    @Test
    @DisplayName("Should validate For Create Should Stop At Duplicate Document")
    void validateForCreateShouldStopAtDuplicateDocument() {
        when(patientRepository.existsByEmailHash(hashes.emailHash())).thenReturn(false);
        when(patientRepository.existsByDocumentNumberHash(hashes.documentHash())).thenReturn(true);

        DuplicateResourceException ex = assertThrows(
                DuplicateResourceException.class,
                () -> validator.validateForCreate(hashes)
        );

        assertEquals("Document number already exists", ex.getMessage());
        verify(patientRepository, never()).existsByPhoneHash(any());
    }

    @Test
    @DisplayName("Should validate For Create Should Stop At Duplicate Phone")
    void validateForCreateShouldStopAtDuplicatePhone() {
        when(patientRepository.existsByEmailHash(hashes.emailHash())).thenReturn(false);
        when(patientRepository.existsByDocumentNumberHash(hashes.documentHash())).thenReturn(false);
        when(patientRepository.existsByPhoneHash(hashes.phoneHash())).thenReturn(true);

        DuplicateResourceException ex = assertThrows(
                DuplicateResourceException.class,
                () -> validator.validateForCreate(hashes)
        );

        assertEquals("Phone already exists", ex.getMessage());
    }

    @Test
    @DisplayName("Should validate For Update Should Pass When All Values Are Unique For Other Patients")
    void validateForUpdateShouldPassWhenAllValuesAreUniqueForOtherPatients() {
        when(patientRepository.existsByEmailHashAndIdNot(hashes.emailHash(), patientId)).thenReturn(false);
        when(patientRepository.existsByDocumentNumberHashAndIdNot(hashes.documentHash(), patientId)).thenReturn(false);
        when(patientRepository.existsByPhoneHashAndIdNot(hashes.phoneHash(), patientId)).thenReturn(false);

        assertDoesNotThrow(() -> validator.validateForUpdate(patientId, hashes));
    }

    @Test
    @DisplayName("Should validate For Update Should Reject Duplicate Email")
    void validateForUpdateShouldRejectDuplicateEmail() {
        when(patientRepository.existsByEmailHashAndIdNot(hashes.emailHash(), patientId)).thenReturn(true);

        DuplicateResourceException ex = assertThrows(
                DuplicateResourceException.class,
                () -> validator.validateForUpdate(patientId, hashes)
        );

        assertEquals("Email already registered", ex.getMessage());
        verify(patientRepository, never()).existsByDocumentNumberHashAndIdNot(any(), eq(patientId));
        verify(patientRepository, never()).existsByPhoneHashAndIdNot(any(), eq(patientId));
    }

    @Test
    @DisplayName("Should validate For Update Should Reject Duplicate Document")
    void validateForUpdateShouldRejectDuplicateDocument() {
        when(patientRepository.existsByEmailHashAndIdNot(hashes.emailHash(), patientId)).thenReturn(false);
        when(patientRepository.existsByDocumentNumberHashAndIdNot(hashes.documentHash(), patientId)).thenReturn(true);

        DuplicateResourceException ex = assertThrows(
                DuplicateResourceException.class,
                () -> validator.validateForUpdate(patientId, hashes)
        );

        assertEquals("Document number already registered", ex.getMessage());
        verify(patientRepository, never()).existsByPhoneHashAndIdNot(any(), eq(patientId));
    }

    @Test
    @DisplayName("Should validate For Update Should Reject Duplicate Phone")
    void validateForUpdateShouldRejectDuplicatePhone() {
        when(patientRepository.existsByEmailHashAndIdNot(hashes.emailHash(), patientId)).thenReturn(false);
        when(patientRepository.existsByDocumentNumberHashAndIdNot(hashes.documentHash(), patientId)).thenReturn(false);
        when(patientRepository.existsByPhoneHashAndIdNot(hashes.phoneHash(), patientId)).thenReturn(true);

        DuplicateResourceException ex = assertThrows(
                DuplicateResourceException.class,
                () -> validator.validateForUpdate(patientId, hashes)
        );

        assertEquals("Phone number already registered", ex.getMessage());
    }
}
