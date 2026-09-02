package com.medsync.appointmentservice.client.patient.dto;

import com.medsync.appointmentservice.client.patient.enums.BloodType;
import com.medsync.appointmentservice.client.patient.enums.Gender;
import com.medsync.appointmentservice.client.patient.enums.PatientStatus;

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