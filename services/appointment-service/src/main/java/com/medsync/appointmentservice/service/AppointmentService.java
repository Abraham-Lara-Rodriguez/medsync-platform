package com.medsync.appointmentservice.service;

import com.medsync.appointmentservice.dto.request.CreateAppointmentRequest;
import com.medsync.appointmentservice.dto.request.UpdateAppointmentRequest;
import com.medsync.appointmentservice.dto.request.UpdateAppointmentStatusRequest;
import com.medsync.appointmentservice.dto.response.AppointmentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AppointmentService {

    Page<AppointmentResponse> getAllAppointments(Pageable pageable);

    AppointmentResponse getAppointmentById(UUID id);

    AppointmentResponse createAppointment(CreateAppointmentRequest request);

    AppointmentResponse updateAppointment(UpdateAppointmentRequest request, UUID id);

    AppointmentResponse updateAppointmentStatus(UpdateAppointmentStatusRequest status, UUID id);


}
