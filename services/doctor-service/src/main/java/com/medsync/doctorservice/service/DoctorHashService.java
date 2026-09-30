package com.medsync.doctorservice.service;

import com.medsync.doctorservice.domain.converter.DeterministicHasher;
import com.medsync.doctorservice.dto.request.CreateDoctorRequest;
import com.medsync.doctorservice.dto.request.UpdateDoctorRequest;
import org.springframework.stereotype.Service;

@Service
public class DoctorHashService {

    public DoctorHashes fromCreateRequest(CreateDoctorRequest request) {
        return new DoctorHashes(
                DeterministicHasher.hash(request.email()),
                DeterministicHasher.hash(request.medicalLicense()),
                DeterministicHasher.hash(request.phone())
        );
    }

    public DoctorHashes fromUpdateRequest(UpdateDoctorRequest request) {
        return new DoctorHashes(
                DeterministicHasher.hash(request.email()),
                null,
                DeterministicHasher.hash(request.phone())
        );
    }

    public record DoctorHashes(
            String emailHash,
            String medicalLicenseHash,
            String phoneHash
    ) {
    }
}
