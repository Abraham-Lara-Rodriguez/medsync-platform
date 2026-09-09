package com.medsync.appointmentservice.domain.entity;

import com.medsync.appointmentservice.domain.enums.AppointmentStatus;
import com.medsync.appointmentservice.domain.enums.AppointmentType;
import com.medsync.appointmentservice.exception.custom.InvalidAppointmentStatusTransitionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AppointmentTest {

    private static final UUID PATIENT_ID = UUID.randomUUID();
    private static final UUID DOCTOR_ID = UUID.randomUUID();

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("creates an appointment with SCHEDULED status")
        void createsScheduled() {
            Appointment appointment = Appointment.create(
                    PATIENT_ID, DOCTOR_ID, AppointmentType.GENERAL, "Checkup", null);

            assertThat(appointment.getPatientId()).isEqualTo(PATIENT_ID);
            assertThat(appointment.getDoctorId()).isEqualTo(DOCTOR_ID);
            assertThat(appointment.getType()).isEqualTo(AppointmentType.GENERAL);
            assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
            assertThat(appointment.getReason()).isEqualTo("Checkup");
            assertThat(appointment.getNotes()).isNull();
        }

        @Test
        @DisplayName("throws when patientId is null")
        void throwsOnNullPatientId() {
            assertThatThrownBy(() -> Appointment.create(
                    null, DOCTOR_ID, AppointmentType.GENERAL, null, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("throws when doctorId is null")
        void throwsOnNullDoctorId() {
            assertThatThrownBy(() -> Appointment.create(
                    PATIENT_ID, null, AppointmentType.GENERAL, null, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("throws when type is null")
        void throwsOnNullType() {
            assertThatThrownBy(() -> Appointment.create(
                    PATIENT_ID, DOCTOR_ID, null, null, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("status transitions")
    class StatusTransitions {

        @Test
        @DisplayName("confirm from SCHEDULED → CONFIRMED")
        void confirmFromScheduled() {
            Appointment appointment = Appointment.create(
                    PATIENT_ID, DOCTOR_ID, AppointmentType.GENERAL, null, null);

            appointment.confirm();

            assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
        }

        @Test
        @DisplayName("confirm from CONFIRMED throws")
        void confirmFromConfirmedThrows() {
            Appointment appointment = Appointment.create(
                    PATIENT_ID, DOCTOR_ID, AppointmentType.GENERAL, null, null);
            appointment.confirm();

            assertThatThrownBy(appointment::confirm)
                    .isInstanceOf(InvalidAppointmentStatusTransitionException.class)
                    .hasMessageContaining("Only scheduled");
        }

        @Test
        @DisplayName("complete from CONFIRMED → COMPLETED")
        void completeFromConfirmed() {
            Appointment appointment = Appointment.create(
                    PATIENT_ID, DOCTOR_ID, AppointmentType.GENERAL, null, null);
            appointment.confirm();

            appointment.complete();

            assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.COMPLETED);
        }

        @Test
        @DisplayName("complete from SCHEDULED throws")
        void completeFromScheduledThrows() {
            Appointment appointment = Appointment.create(
                    PATIENT_ID, DOCTOR_ID, AppointmentType.GENERAL, null, null);

            assertThatThrownBy(appointment::complete)
                    .isInstanceOf(InvalidAppointmentStatusTransitionException.class)
                    .hasMessageContaining("Only confirmed");
        }

        @Test
        @DisplayName("cancel from SCHEDULED → CANCELLED")
        void cancelFromScheduled() {
            Appointment appointment = Appointment.create(
                    PATIENT_ID, DOCTOR_ID, AppointmentType.GENERAL, null, null);

            appointment.cancel();

            assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
        }

        @Test
        @DisplayName("cancel from CONFIRMED → CANCELLED")
        void cancelFromConfirmed() {
            Appointment appointment = Appointment.create(
                    PATIENT_ID, DOCTOR_ID, AppointmentType.GENERAL, null, null);
            appointment.confirm();

            appointment.cancel();

            assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
        }

        @Test
        @DisplayName("cancel from COMPLETED throws")
        void cancelFromCompletedThrows() {
            Appointment appointment = Appointment.create(
                    PATIENT_ID, DOCTOR_ID, AppointmentType.GENERAL, null, null);
            appointment.confirm();
            appointment.complete();

            assertThatThrownBy(appointment::cancel)
                    .isInstanceOf(InvalidAppointmentStatusTransitionException.class)
                    .hasMessageContaining("cannot be cancelled");
        }

        @Test
        @DisplayName("cancel from NO_SHOW throws")
        void cancelFromNoShowThrows() {
            Appointment appointment = Appointment.create(
                    PATIENT_ID, DOCTOR_ID, AppointmentType.GENERAL, null, null);
            appointment.confirm();
            appointment.markNoShow();

            assertThatThrownBy(appointment::cancel)
                    .isInstanceOf(InvalidAppointmentStatusTransitionException.class);
        }

        @Test
        @DisplayName("markNoShow from CONFIRMED → NO_SHOW")
        void markNoShowFromConfirmed() {
            Appointment appointment = Appointment.create(
                    PATIENT_ID, DOCTOR_ID, AppointmentType.GENERAL, null, null);
            appointment.confirm();

            appointment.markNoShow();

            assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.NO_SHOW);
        }

        @Test
        @DisplayName("markNoShow from SCHEDULED throws")
        void markNoShowFromScheduledThrows() {
            Appointment appointment = Appointment.create(
                    PATIENT_ID, DOCTOR_ID, AppointmentType.GENERAL, null, null);

            assertThatThrownBy(appointment::markNoShow)
                    .isInstanceOf(InvalidAppointmentStatusTransitionException.class)
                    .hasMessageContaining("Only confirmed");
        }
    }

    @Nested
    @DisplayName("setReason / setNotes")
    class TextFields {

        @Test
        @DisplayName("trims and stores a valid reason")
        void trimsReason() {
            Appointment appointment = Appointment.create(
                    PATIENT_ID, DOCTOR_ID, AppointmentType.GENERAL, null, null);

            appointment.setReason("  Headache  ");

            assertThat(appointment.getReason()).isEqualTo("Headache");
        }

        @Test
        @DisplayName("sets null when reason is blank")
        void blankReasonBecomesNull() {
            Appointment appointment = Appointment.create(
                    PATIENT_ID, DOCTOR_ID, AppointmentType.GENERAL, null, null);

            appointment.setReason("   ");

            assertThat(appointment.getReason()).isNull();
        }

        @Test
        @DisplayName("throws when reason exceeds max length")
        void throwsWhenReasonTooLong() {
            Appointment appointment = Appointment.create(
                    PATIENT_ID, DOCTOR_ID, AppointmentType.GENERAL, null, null);

            assertThatThrownBy(() -> appointment.setReason("x".repeat(501)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("500");
        }

        @Test
        @DisplayName("throws when notes exceeds max length")
        void throwsWhenNotesTooLong() {
            Appointment appointment = Appointment.create(
                    PATIENT_ID, DOCTOR_ID, AppointmentType.GENERAL, null, null);

            assertThatThrownBy(() -> appointment.setNotes("x".repeat(2001)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("2000");
        }
    }
}
