package com.medsync.appointmentservice.client.doctor.config;

import com.medsync.appointmentservice.client.doctor.component.DoctorClientFallback;
import com.medsync.appointmentservice.client.doctor.dto.response.DoctorResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "doctor-service", fallback = DoctorClientFallback.class)
@CircuitBreaker(name = "doctor-service", fallbackMethod = "doctorFallback")
public interface DoctorClient {

    @GetMapping("/api/v1/doctors/{id}")
    DoctorResponse getDoctorById(@PathVariable UUID id);
}
