package com.medsync.appointmentservice.client.patient.config;

import com.medsync.appointmentservice.client.patient.dto.PatientResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "patient-service")
public interface PatientClient {

    @GetMapping("/api/v1/patients/{id}")
    PatientResponse getPatientById(@PathVariable UUID id);

}