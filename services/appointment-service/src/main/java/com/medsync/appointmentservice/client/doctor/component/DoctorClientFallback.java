package com.medsync.appointmentservice.client.doctor.component;

import com.medsync.appointmentservice.client.doctor.config.DoctorClient;
import com.medsync.appointmentservice.client.doctor.dto.response.DoctorResponse;
import com.medsync.appointmentservice.exception.custom.DoctorServiceUnavailableException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DoctorClientFallback implements DoctorClient {
    @Override
    public DoctorResponse getDoctorById(UUID id) {
        throw new DoctorServiceUnavailableException(
                "Doctor service is currently unavailable"
        );
    }
}
