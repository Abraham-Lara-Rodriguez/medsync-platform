package com.medsync.appointmentservice.exception.custom;

public class PatientInactiveException extends RuntimeException {

    public PatientInactiveException(String message) {
        super(message);
    }
}