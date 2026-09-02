package com.medsync.appointmentservice.domain.validator;

import com.medsync.appointmentservice.exception.custom.InvalidAppointmentScheduleException;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
public class AppointmentScheduleValidator {

    public void validate(LocalDate date, LocalTime startTime, LocalTime endTime) {
        if (date == null) {
            throw new InvalidAppointmentScheduleException(
                    "Appointment date cannot be null"
            );
        }

        if (startTime == null || endTime == null) {
            throw new InvalidAppointmentScheduleException(
                    "Appointment start and end time are required"
            );
        }

        if (!endTime.isAfter(startTime)) {
            throw new InvalidAppointmentScheduleException(
                    "Appointment end time must be after start time"
            );
        }

        validateFuture(date, startTime);
        validateDuration(startTime, endTime);
    }

    private void validateFuture(LocalDate date, LocalTime startTime) {
        LocalDateTime appointmentDateTime = LocalDateTime.of(date, startTime);
        if (!appointmentDateTime.isAfter(LocalDateTime.now())) {
            throw new InvalidAppointmentScheduleException(
                    "Appointment must be scheduled in the future"
            );
        }
    }

    private void validateDuration(LocalTime startTime, LocalTime endTime) {
        long minutes = Duration.between(startTime, endTime).toMinutes();
        if (minutes < 15) {
            throw new InvalidAppointmentScheduleException(
                    "Appointment must last at least 15 minutes"
            );
        }
    }
}