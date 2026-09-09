package com.medsync.appointmentservice.mapper;

import com.medsync.appointmentservice.domain.entity.Appointment;
import com.medsync.appointmentservice.dto.response.AppointmentResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AppointmentMapper {
    AppointmentResponse toResponse(Appointment appointment);
}
