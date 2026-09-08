package com.medsync.appointmentservice.exception.custom;

public class PatientServiceUnavailableException extends RuntimeException {
    public PatientServiceUnavailableException(String message) {
        super(message);
    }
}