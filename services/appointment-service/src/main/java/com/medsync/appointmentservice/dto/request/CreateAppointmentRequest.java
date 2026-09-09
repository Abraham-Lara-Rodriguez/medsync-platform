package com.medsync.appointmentservice.dto.request;

import com.medsync.appointmentservice.domain.enums.AppointmentType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record CreateAppointmentRequest(
        @NotNull
        UUID patientId,

        @NotNull
        UUID doctorId,

        @NotNull
        @Future
        LocalDate appointmentDate,

        @NotNull
        LocalTime startTime,

        @NotNull
        LocalTime endTime,

        @NotNull
        AppointmentType type,

        @Size(max = 500)
        String reason,

        @Size(max = 2000)
        String notes
) {
}
