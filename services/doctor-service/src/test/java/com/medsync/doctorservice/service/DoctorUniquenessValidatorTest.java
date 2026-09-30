package com.medsync.doctorservice.service;

import com.medsync.commoncore.error.custom.DuplicateResourceException;
import com.medsync.doctorservice.repository.DoctorRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorUniquenessValidatorTest {

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private DoctorUniquenessValidator validator;

    @Test
    @DisplayName("Should pass validateForCreate when no duplicate exists")
    void validateForCreateShouldPassWhenNoDuplicate() {
        DoctorHashService.DoctorHashes hashes = new DoctorHashService.DoctorHashes("emailHash", "licenseHash", "phoneHash");

        when(doctorRepository.existsByEmailHash("emailHash")).thenReturn(false);
        when(doctorRepository.existsByMedicalLicenseHash("licenseHash")).thenReturn(false);
        when(doctorRepository.existsByPhoneHash("phoneHash")).thenReturn(false);

        assertDoesNotThrow(() -> validator.validateForCreate(hashes));
    }

    @Test
    @DisplayName("Should throw exception when email exists on create")
    void validateForCreateShouldThrowWhenEmailExists() {
        DoctorHashService.DoctorHashes hashes = new DoctorHashService.DoctorHashes("emailHash", "licenseHash", "phoneHash");

        when(doctorRepository.existsByEmailHash("emailHash")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> validator.validateForCreate(hashes));
    }

    @Test
    @DisplayName("Should throw exception when medical license exists on create")
    void validateForCreateShouldThrowWhenLicenseExists() {
        DoctorHashService.DoctorHashes hashes = new DoctorHashService.DoctorHashes("emailHash", "licenseHash", "phoneHash");

        when(doctorRepository.existsByEmailHash("emailHash")).thenReturn(false);
        when(doctorRepository.existsByMedicalLicenseHash("licenseHash")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> validator.validateForCreate(hashes));
    }

    @Test
    @DisplayName("Should throw exception when phone exists on create")
    void validateForCreateShouldThrowWhenPhoneExists() {
        DoctorHashService.DoctorHashes hashes = new DoctorHashService.DoctorHashes("emailHash", "licenseHash", "phoneHash");

        when(doctorRepository.existsByEmailHash("emailHash")).thenReturn(false);
        when(doctorRepository.existsByMedicalLicenseHash("licenseHash")).thenReturn(false);
        when(doctorRepository.existsByPhoneHash("phoneHash")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> validator.validateForCreate(hashes));
    }

    @Test
    @DisplayName("Should pass validateForUpdate when no duplicate exists")
    void validateForUpdateShouldPassWhenNoDuplicate() {
        UUID id = UUID.randomUUID();
        DoctorHashService.DoctorHashes hashes = new DoctorHashService.DoctorHashes("emailHash", null, "phoneHash");

        when(doctorRepository.existsByEmailHashAndIdNot("emailHash", id)).thenReturn(false);
        when(doctorRepository.existsByPhoneHashAndIdNot("phoneHash", id)).thenReturn(false);

        assertDoesNotThrow(() -> validator.validateForUpdate(id, hashes));
    }
}
