package com.medsync.appointmentservice.client.patient.component;

import com.medsync.appointmentservice.client.patient.config.PatientClient;
import com.medsync.appointmentservice.client.patient.dto.PatientResponse;
import com.medsync.appointmentservice.exception.custom.PatientServiceUnavailableException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PatientClientFallback implements PatientClient {
    @Override
    public PatientResponse getPatientById(UUID id) {
        throw new PatientServiceUnavailableException(
                "Patient service is currently unavailable"
        );
    }
}