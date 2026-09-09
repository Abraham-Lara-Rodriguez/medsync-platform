package com.medsync.doctorservice.dto.response;

import com.medsync.doctorservice.domain.enums.DoctorStatus;
import com.medsync.doctorservice.domain.enums.Specialty;

import java.util.UUID;

public record DoctorResponse(
        UUID id,
        String firstName,
        String lastName,
        Specialty specialty,
        String medicalLicense,
        String email,
        String phone,
        DoctorStatus status
) {}
