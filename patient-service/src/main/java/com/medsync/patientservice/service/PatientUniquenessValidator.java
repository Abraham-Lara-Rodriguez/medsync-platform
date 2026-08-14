package com.medsync.patientservice.service;


import com.medsync.commoncore.error.custom.DuplicateResourceException;
import com.medsync.patientservice.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PatientUniquenessValidator {

    private final PatientRepository patientRepository;

    public void validateForCreate(PatientHashService.PatientHashes hashes) {

        if (patientRepository.existsByEmailHash(hashes.emailHash())) {
            throw new DuplicateResourceException("Email already exists");
        }

        if (patientRepository.existsByDocumentNumberHash(hashes.documentHash())) {
            throw new DuplicateResourceException("Document number already exists");
        }

        if (patientRepository.existsByPhoneHash(hashes.phoneHash())) {
            throw new DuplicateResourceException("Phone already exists");
        }
    }

    public void validateForUpdate(UUID patientId, PatientHashService.PatientHashes hashes) {

        if (patientRepository.existsByEmailHashAndIdNot(hashes.emailHash(), patientId)) {
            throw new DuplicateResourceException("Email already registered");
        }
        if (patientRepository.existsByDocumentNumberHashAndIdNot(hashes.documentHash(), patientId)) {
            throw new DuplicateResourceException("Document number already registered");
        }

        if (patientRepository.existsByPhoneHashAndIdNot(hashes.phoneHash(), patientId)) {
            throw new DuplicateResourceException("Phone number already registered");
        }

    }
}