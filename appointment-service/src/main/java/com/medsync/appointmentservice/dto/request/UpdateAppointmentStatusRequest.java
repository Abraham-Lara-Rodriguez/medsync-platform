package com.medsync.appointmentservice.dto.request;

import com.medsync.appointmentservice.domain.enums.AppointmentStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateAppointmentStatusRequest(

        @NotNull
        AppointmentStatus status
) {
}