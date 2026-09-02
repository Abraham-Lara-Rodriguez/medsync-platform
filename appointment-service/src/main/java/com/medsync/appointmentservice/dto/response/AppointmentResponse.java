package com.medsync.appointmentservice.dto.response;

import com.medsync.appointmentservice.domain.enums.AppointmentStatus;
import com.medsync.appointmentservice.domain.enums.AppointmentType;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record AppointmentResponse(
        UUID id,
        UUID patientId,
        UUID doctorId,

        LocalDate appointmentDate,
        LocalTime startTime,
        LocalTime endTime,

        AppointmentType type,
        AppointmentStatus status,

        String reason,
        String notes,

        Instant createdAt,
        Instant updatedAt
) {
}
