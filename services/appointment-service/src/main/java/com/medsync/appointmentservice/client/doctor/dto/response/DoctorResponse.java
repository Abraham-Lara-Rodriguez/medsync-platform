package com.medsync.appointmentservice.client.doctor.dto.response;

import com.medsync.appointmentservice.client.doctor.enums.DoctorStatus;
import com.medsync.appointmentservice.client.doctor.enums.Specialty;

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
