package com.medsync.doctorservice.dto.request;

// TODO: evaluate whether the fields (specialty, medicalLicense) need to be updated (modified).
public record UpdateDoctorRequest(
        String firstName,
        String lastName,
        String email,
        String phone
        ) {
}