package com.medsync.patientservice.service;

import com.medsync.patientservice.dto.request.CreatePatientRequest;
import com.medsync.patientservice.dto.request.UpdatePatientRequest;
import com.medsync.patientservice.dto.response.PatientResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PatientService {

    Page<PatientResponse> getAllPatients(Pageable pageable);

    PatientResponse getPatientById(UUID id);

    PatientResponse createPatient(CreatePatientRequest createPatientRequest);

    PatientResponse updatePatient(UUID id, UpdatePatientRequest updatePatientRequest);

    void deactivatePatient(UUID id);

}