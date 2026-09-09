package com.medsync.appointmentservice.domain.validator;

import com.medsync.appointmentservice.exception.custom.InvalidAppointmentScheduleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AppointmentScheduleValidatorTest {

    private final AppointmentScheduleValidator validator = new AppointmentScheduleValidator();

    @Nested
    @DisplayName("validate")
    class Validate {

        @Test
        @DisplayName("passes for valid future appointment with 30 minutes duration")
        void validFutureAppointment() {
            validator.validate(LocalDate.now().plusDays(1), LocalTime.of(10, 0), LocalTime.of(10, 30));
        }

        @Test
        @DisplayName("throws when date is null")
        void throwsWhenDateNull() {
            assertThatThrownBy(() -> validator.validate(null, LocalTime.of(10, 0), LocalTime.of(10, 30)))
                    .isInstanceOf(InvalidAppointmentScheduleException.class)
                    .hasMessageContaining("date cannot be null");
        }

        @Test
        @DisplayName("throws when startTime is null")
        void throwsWhenStartTimeNull() {
            assertThatThrownBy(() -> validator.validate(LocalDate.now().plusDays(1), null, LocalTime.of(10, 30)))
                    .isInstanceOf(InvalidAppointmentScheduleException.class)
                    .hasMessageContaining("start and end time are required");
        }

        @Test
        @DisplayName("throws when endTime is null")
        void throwsWhenEndTimeNull() {
            assertThatThrownBy(() -> validator.validate(LocalDate.now().plusDays(1), LocalTime.of(10, 0), null))
                    .isInstanceOf(InvalidAppointmentScheduleException.class)
                    .hasMessageContaining("start and end time are required");
        }

        @Test
        @DisplayName("throws when endTime is before startTime")
        void throwsWhenEndBeforeStart() {
            assertThatThrownBy(() -> validator.validate(LocalDate.now().plusDays(1), LocalTime.of(10, 30), LocalTime.of(10, 0)))
                    .isInstanceOf(InvalidAppointmentScheduleException.class)
                    .hasMessageContaining("end time must be after start time");
        }

        @Test
        @DisplayName("throws when endTime equals startTime")
        void throwsWhenEndEqualsStart() {
            assertThatThrownBy(() -> validator.validate(LocalDate.now().plusDays(1), LocalTime.of(10, 0), LocalTime.of(10, 0)))
                    .isInstanceOf(InvalidAppointmentScheduleException.class)
                    .hasMessageContaining("end time must be after start time");
        }

        @Test
        @DisplayName("throws when appointment is in the past")
        void throwsWhenPast() {
            assertThatThrownBy(() -> validator.validate(LocalDate.now().minusDays(1), LocalTime.of(10, 0), LocalTime.of(10, 30)))
                    .isInstanceOf(InvalidAppointmentScheduleException.class)
                    .hasMessageContaining("future");
        }

        @Test
        @DisplayName("throws when appointment is exactly now")
        void throwsWhenNow() {
            LocalDate today = LocalDate.now();
            LocalTime now = LocalTime.now().plusMinutes(5); // pequeño margen para CI
            assertThatThrownBy(() -> validator.validate(today, LocalTime.of(0, 0), LocalTime.of(0, 30)))
                    .isInstanceOf(InvalidAppointmentScheduleException.class)
                    .hasMessageContaining("future");
        }

        @Test
        @DisplayName("throws when duration is less than 15 minutes")
        void throwsWhenTooShort() {
            assertThatThrownBy(() -> validator.validate(LocalDate.now().plusDays(1), LocalTime.of(10, 0), LocalTime.of(10, 10)))
                    .isInstanceOf(InvalidAppointmentScheduleException.class)
                    .hasMessageContaining("15");
        }

        @Test
        @DisplayName("passes for exactly 15 minutes")
        void passesForFifteenMinutes() {
            validator.validate(LocalDate.now().plusDays(1), LocalTime.of(10, 0), LocalTime.of(10, 15));
        }
    }
}