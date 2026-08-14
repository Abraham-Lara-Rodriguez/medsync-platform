package com.medsync.patientservice.dto.response;

import com.medsync.patientservice.domain.enums.BloodType;
import com.medsync.patientservice.domain.enums.Gender;
import com.medsync.patientservice.domain.enums.PatientStatus;

import java.time.LocalDate;
import java.util.UUID;

public record PatientResponse(
        UUID id,
        String firstName,
        String lastName,
        String documentNumber,
        Gender gender,
        LocalDate birthDate,
        String phone,
        String email,
        String address,
        BloodType bloodType,
        PatientStatus status
) {
}