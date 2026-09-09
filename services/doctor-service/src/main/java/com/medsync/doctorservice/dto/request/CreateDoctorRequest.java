package com.medsync.doctorservice.dto.request;

import com.medsync.doctorservice.domain.enums.Specialty;

public record CreateDoctorRequest(
        String firstName, String lastName, Specialty specialty,
        String medicalLicense, String email, String phone) {
}
