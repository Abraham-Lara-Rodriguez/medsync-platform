package com.medsync.appointmentservice.exception.custom;

public class AppointmentModificationNotAllowedException extends RuntimeException {
    public AppointmentModificationNotAllowedException(String message) {
        super(message);
    }
}
