package com.medsync.appointmentservice.exception.custom;

public class InvalidAppointmentScheduleException extends RuntimeException {
    public InvalidAppointmentScheduleException(String message) {
        super(message);
    }
}
