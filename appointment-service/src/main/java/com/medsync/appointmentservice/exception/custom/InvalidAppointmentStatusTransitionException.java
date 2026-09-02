package com.medsync.appointmentservice.exception.custom;

public class InvalidAppointmentStatusTransitionException extends RuntimeException {
    public InvalidAppointmentStatusTransitionException(String message) {
        super(message);
    }
}
