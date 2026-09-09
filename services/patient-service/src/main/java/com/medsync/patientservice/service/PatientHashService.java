package com.medsync.patientservice.service;

import com.medsync.patientservice.domain.converter.DeterministicHasher;
import com.medsync.patientservice.dto.request.CreatePatientRequest;
import com.medsync.patientservice.dto.request.UpdatePatientRequest;
import org.springframework.stereotype.Service;

@Service
public class PatientHashService {

    public PatientHashes fromCreateRequest(CreatePatientRequest request) {
        return new PatientHashes(
                DeterministicHasher.hash(request.email()),
                DeterministicHasher.hash(request.documentNumber()),
                DeterministicHasher.hash(request.phone())
        );
    }

    public PatientHashes fromUpdateRequest(UpdatePatientRequest request) {
        return new PatientHashes(
                DeterministicHasher.hash(request.email()),
                DeterministicHasher.hash(request.documentNumber()),
                DeterministicHasher.hash(request.phone())
        );
    }

    public record PatientHashes(
            String emailHash,
            String documentHash,
            String phoneHash
    ) {
    }
}