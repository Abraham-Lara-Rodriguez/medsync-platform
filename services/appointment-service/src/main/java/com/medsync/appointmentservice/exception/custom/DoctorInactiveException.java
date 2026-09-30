package com.medsync.appointmentservice.exception.custom;

public class DoctorInactiveException extends RuntimeException {
    public DoctorInactiveException(String message) {
        super(message);
    }
}
