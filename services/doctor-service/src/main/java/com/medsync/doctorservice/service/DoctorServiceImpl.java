package com.medsync.doctorservice.service;

import com.medsync.doctorservice.domain.entity.Doctor;
import com.medsync.doctorservice.dto.request.CreateDoctorRequest;
import com.medsync.doctorservice.dto.request.UpdateDoctorRequest;
import com.medsync.doctorservice.dto.response.DoctorResponse;
import com.medsync.doctorservice.exception.custom.ResourceNotFoundException;
import com.medsync.doctorservice.mapper.DoctorMapper;
import com.medsync.doctorservice.repository.DoctorRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;
    private final DoctorMapper doctorMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<DoctorResponse> getAllDoctor(Pageable pageable) {
        return doctorRepository.findAll(pageable).map(doctorMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorResponse getDoctorById(UUID id) {
        Doctor doctor = findDoctorOrThrow(id);
        return doctorMapper.toResponse(doctor);
    }

    @Override
    @Transactional()
    public DoctorResponse createDoctor(CreateDoctorRequest request) {

        // TODO: Implement validation logic for the request if needed (e.g., check for unique email or medical license)

        Doctor doctor = Doctor.create(
                request.firstName(),
                request.lastName(),
                request.specialty(),
                request.medicalLicense(),
                request.email(),
                request.phone()
        );
        return doctorMapper.toResponse(doctorRepository.save(doctor));
    }

    @Override
    @Transactional()
    public DoctorResponse updateDoctor(UUID id, UpdateDoctorRequest request) {
        Doctor doctor = findDoctorOrThrow(id);
        doctor.changeFirstName(request.firstName());
        doctor.changeLastName(request.lastName());
        doctor.changeEmail(request.email());
        doctor.changePhone(request.phone());
        return doctorMapper.toResponse(doctorRepository.save(doctor));
    }

    /* ================= HELPERS ================= */
    protected Doctor findDoctorOrThrow(UUID id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + id));
    }
    
}
