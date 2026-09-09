package com.medsync.doctorservice.service;

import com.medsync.doctorservice.dto.request.CreateDoctorRequest;
import com.medsync.doctorservice.dto.request.UpdateDoctorRequest;
import com.medsync.doctorservice.dto.response.DoctorResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface DoctorService {
    Page<DoctorResponse> getAllDoctor(Pageable pageable);
    DoctorResponse getDoctorById(UUID id);
    DoctorResponse createDoctor(CreateDoctorRequest request);
    DoctorResponse updateDoctor(UUID id, UpdateDoctorRequest request);
}
