package com.medsync.appointmentservice.client.patient.config;

import com.medsync.appointmentservice.client.patient.component.PatientClientFallback;
import com.medsync.appointmentservice.client.patient.dto.PatientResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "patient-service", fallback = PatientClientFallback.class)
@CircuitBreaker(name = "patient-service", fallbackMethod = "patientFallback")
public interface PatientClient {

    @GetMapping("/api/v1/patients/{id}")
    PatientResponse getPatientById(@PathVariable UUID id);

}