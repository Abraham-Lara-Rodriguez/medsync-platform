package com.medsync.appointmentservice.client.doctor.config;

import com.medsync.appointmentservice.client.doctor.component.DoctorClientFallback;
import com.medsync.appointmentservice.client.doctor.dto.response.DoctorResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "doctor-service", fallback = DoctorClientFallback.class)
public interface DoctorClient {

    @GetMapping("/api/v1/doctors/{id}")
    DoctorResponse getDoctorById(@PathVariable UUID id);
}
