package com.medsync.patientservice.service;

import com.medsync.commoncore.error.custom.ResourceNotFoundException;
import com.medsync.patientservice.domain.entity.Patient;
import com.medsync.patientservice.domain.enums.PatientStatus;
import com.medsync.patientservice.dto.request.CreatePatientRequest;
import com.medsync.patientservice.dto.request.UpdatePatientRequest;
import com.medsync.patientservice.dto.response.PatientResponse;
import com.medsync.patientservice.mapper.PatientMapper;
import com.medsync.patientservice.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private static final int MAX_PAGE_SIZE = 100;
    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;
    private final PatientHashService patientHashService;
    private final PatientUniquenessValidator patientUniquenessValidator;

    @Override
    @Transactional(readOnly = true)
    public Page<PatientResponse> getAllPatients(Pageable pageable) {
        validatePageSize(pageable);
        return patientRepository.findAll(pageable).map(patientMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PatientResponse getPatientById(UUID id) {
        return patientMapper.toResponse(findPatientOrThrow(id));
    }

    @Override
    @Transactional
    public PatientResponse createPatient(CreatePatientRequest createPatientRequest) {

        PatientHashService.PatientHashes hashes = patientHashService.fromCreateRequest(createPatientRequest);
        patientUniquenessValidator.validateForCreate(hashes);

        Patient patient = Patient.create(
                createPatientRequest.firstName(),
                createPatientRequest.lastName(),
                createPatientRequest.documentNumber(),
                createPatientRequest.gender(),
                createPatientRequest.birthDate(),
                createPatientRequest.phone(),
                createPatientRequest.email(),
                createPatientRequest.address(),
                createPatientRequest.bloodType()
        );
        return patientMapper.toResponse(patientRepository.save(patient));
    }

    @Override
    @Transactional
    public PatientResponse updatePatient(UUID id, UpdatePatientRequest updatePatientRequest) {
        Patient patient = findPatientOrThrow(id);

        PatientHashService.PatientHashes hashes = patientHashService.fromUpdateRequest(updatePatientRequest);
        patientUniquenessValidator.validateForUpdate(id, hashes);

        patient.changeFirstName(updatePatientRequest.firstName());
        patient.changeLastName(updatePatientRequest.lastName());
        patient.changeDocumentNumber(updatePatientRequest.documentNumber());
        patient.changeGender(updatePatientRequest.gender());
        patient.changeBirthDate(updatePatientRequest.birthDate());
        patient.changeEmail(updatePatientRequest.email());
        patient.changePhone(updatePatientRequest.phone());
        patient.changeAddress(updatePatientRequest.address());
        patient.changeBloodType(updatePatientRequest.bloodType());
        return patientMapper.toResponse(patientRepository.save(patient));
    }

    @Override
    @Transactional
    public void deactivatePatient(UUID id) {
        Patient patient = findPatientOrThrow(id);
        patient.changeStatus(PatientStatus.INACTIVE);
        patientRepository.save(patient);
    }

    /* ================= HELPERS ================= */
    protected Patient findPatientOrThrow(UUID id) {
        return patientRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));
    }

    private void validatePageSize(Pageable pageable) {
        if (pageable.getPageSize() > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("Page size must not exceed " + MAX_PAGE_SIZE);
        }
    }
}
