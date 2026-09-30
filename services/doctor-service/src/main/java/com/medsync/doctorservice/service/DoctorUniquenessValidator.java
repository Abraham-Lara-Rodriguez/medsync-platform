package com.medsync.doctorservice.service;

import com.medsync.commoncore.error.custom.DuplicateResourceException;
import com.medsync.doctorservice.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DoctorUniquenessValidator {

    private final DoctorRepository doctorRepository;

    public void validateForCreate(DoctorHashService.DoctorHashes hashes) {
        if (doctorRepository.existsByEmailHash(hashes.emailHash())) {
            throw new DuplicateResourceException("A doctor with the provided email already exists.");
        }

        if (doctorRepository.existsByMedicalLicenseHash(hashes.medicalLicenseHash())) {
            throw new DuplicateResourceException("A doctor with the provided medical license already exists.");
        }

        if (doctorRepository.existsByPhoneHash(hashes.phoneHash())) {
            throw new DuplicateResourceException("A doctor with the provided phone already exists.");
        }
    }

    public void validateForUpdate(UUID doctorId, DoctorHashService.DoctorHashes hashes) {
        if (doctorRepository.existsByEmailHashAndIdNot(hashes.emailHash(), doctorId)) {
            throw new DuplicateResourceException("A doctor with the provided email already exists.");
        }

        if (doctorRepository.existsByPhoneHashAndIdNot(hashes.phoneHash(), doctorId)) {
            throw new DuplicateResourceException("A doctor with the provided phone already exists.");
        }
    }
}
