package com.medsync.appointmentservice.repository;

import com.medsync.appointmentservice.domain.entity.Appointment;
import com.medsync.appointmentservice.domain.enums.AppointmentType;
import com.medsync.appointmentservice.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AppointmentRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private AppointmentRepository appointmentRepository;

    private static final UUID PATIENT_ID = UUID.randomUUID();
    private static final UUID DOCTOR_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        appointmentRepository.deleteAll();
    }

    private Appointment createAndSaveAppointment(LocalDate date, LocalTime start, LocalTime end) {
        Appointment appointment = Appointment.create(
                PATIENT_ID, DOCTOR_ID, AppointmentType.GENERAL, "Checkup", null);
        appointment.updateSchedule(date, start, end);
        return appointmentRepository.saveAndFlush(appointment);
    }

    // ============================================================
    // existsOverlappingAppointment
    // ============================================================

    @Nested
    @DisplayName("existsOverlappingAppointment")
    class ExistsOverlappingAppointmentTest {

        @Test
        @DisplayName("returns false when no appointments exist for the doctor")
        void returnsFalseWhenNoAppointments() {
            boolean exists = appointmentRepository.existsOverlappingAppointment(
                    DOCTOR_ID, LocalDate.now().plusDays(1),
                    LocalTime.of(10, 0), LocalTime.of(10, 30),
                    null
            );

            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("returns true when a same-time slot overlap exists")
        void returnsTrueForSameTimeOverlap() {
            LocalDate date = LocalDate.now().plusDays(1);
            createAndSaveAppointment(date, LocalTime.of(10, 0), LocalTime.of(11, 0));

            boolean exists = appointmentRepository.existsOverlappingAppointment(
                    DOCTOR_ID, date,
                    LocalTime.of(10, 0), LocalTime.of(11, 0),
                    null
            );

            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("returns true when new slot is fully contained in existing")
        void returnsTrueWhenContained() {
            LocalDate date = LocalDate.now().plusDays(1);
            createAndSaveAppointment(date, LocalTime.of(9, 0), LocalTime.of(12, 0));

            boolean exists = appointmentRepository.existsOverlappingAppointment(
                    DOCTOR_ID, date,
                    LocalTime.of(10, 0), LocalTime.of(10, 30),
                    null
            );

            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("returns true when new slot starts before and ends within existing")
        void returnsTrueForPartialOverlapStart() {
            LocalDate date = LocalDate.now().plusDays(1);
            createAndSaveAppointment(date, LocalTime.of(10, 30), LocalTime.of(11, 30));

            boolean exists = appointmentRepository.existsOverlappingAppointment(
                    DOCTOR_ID, date,
                    LocalTime.of(10, 0), LocalTime.of(11, 0),
                    null
            );

            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("returns true when new slot starts within and ends after existing")
        void returnsTrueForPartialOverlapEnd() {
            LocalDate date = LocalDate.now().plusDays(1);
            createAndSaveAppointment(date, LocalTime.of(9, 30), LocalTime.of(10, 30));

            boolean exists = appointmentRepository.existsOverlappingAppointment(
                    DOCTOR_ID, date,
                    LocalTime.of(10, 0), LocalTime.of(11, 0),
                    null
            );

            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("returns false when new slot is before existing")
        void returnsFalseWhenBefore() {
            LocalDate date = LocalDate.now().plusDays(1);
            createAndSaveAppointment(date, LocalTime.of(10, 0), LocalTime.of(11, 0));

            boolean exists = appointmentRepository.existsOverlappingAppointment(
                    DOCTOR_ID, date,
                    LocalTime.of(8, 0), LocalTime.of(9, 0),
                    null
            );

            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("returns false when new slot is after existing")
        void returnsFalseWhenAfter() {
            LocalDate date = LocalDate.now().plusDays(1);
            createAndSaveAppointment(date, LocalTime.of(10, 0), LocalTime.of(11, 0));

            boolean exists = appointmentRepository.existsOverlappingAppointment(
                    DOCTOR_ID, date,
                    LocalTime.of(11, 0), LocalTime.of(12, 0),
                    null
            );

            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("returns false when new slot ends exactly at existing start")
        void returnsFalseWhenEndsAtStart() {
            LocalDate date = LocalDate.now().plusDays(1);
            createAndSaveAppointment(date, LocalTime.of(10, 0), LocalTime.of(11, 0));

            boolean exists = appointmentRepository.existsOverlappingAppointment(
                    DOCTOR_ID, date,
                    LocalTime.of(9, 0), LocalTime.of(10, 0),
                    null
            );

            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("returns false when new slot starts exactly at existing end")
        void returnsFalseWhenStartsAtEnd() {
            LocalDate date = LocalDate.now().plusDays(1);
            createAndSaveAppointment(date, LocalTime.of(10, 0), LocalTime.of(11, 0));

            boolean exists = appointmentRepository.existsOverlappingAppointment(
                    DOCTOR_ID, date,
                    LocalTime.of(11, 0), LocalTime.of(12, 0),
                    null
            );

            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("excludes a given appointment id from overlap check")
        void excludesGivenAppointment() {
            LocalDate date = LocalDate.now().plusDays(1);
            Appointment existing = createAndSaveAppointment(date, LocalTime.of(10, 0), LocalTime.of(11, 0));

            boolean exists = appointmentRepository.existsOverlappingAppointment(
                    DOCTOR_ID, date,
                    LocalTime.of(10, 0), LocalTime.of(11, 0),
                    existing.getId()
            );

            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("returns false for different doctor on same date and time")
        void returnsFalseForDifferentDoctor() {
            LocalDate date = LocalDate.now().plusDays(1);
            createAndSaveAppointment(date, LocalTime.of(10, 0), LocalTime.of(11, 0));

            boolean exists = appointmentRepository.existsOverlappingAppointment(
                    UUID.randomUUID(), date,
                    LocalTime.of(10, 0), LocalTime.of(11, 0),
                    null
            );

            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("returns false for same doctor on different date")
        void returnsFalseForDifferentDate() {
            LocalDate date = LocalDate.now().plusDays(1);
            createAndSaveAppointment(date, LocalTime.of(10, 0), LocalTime.of(11, 0));

            boolean exists = appointmentRepository.existsOverlappingAppointment(
                    DOCTOR_ID, date.plusDays(1),
                    LocalTime.of(10, 0), LocalTime.of(11, 0),
                    null
            );

            assertThat(exists).isFalse();
        }
    }

    // ============================================================
    // Standard JPA methods
    // ============================================================

    @Nested
    @DisplayName("Standard JPA methods")
    class StandardJpaTest {

        @Test
        @DisplayName("saves and retrieves an appointment by id")
        void saveAndFindById() {
            Appointment appointment = createAndSaveAppointment(
                    LocalDate.now().plusDays(1), LocalTime.of(10, 0), LocalTime.of(10, 30));

            Optional<Appointment> found = appointmentRepository.findById(appointment.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getPatientId()).isEqualTo(PATIENT_ID);
            assertThat(found.get().getDoctorId()).isEqualTo(DOCTOR_ID);
            assertThat(found.get().getAppointmentDate()).isEqualTo(LocalDate.now().plusDays(1));
            assertThat(found.get().getStartTime()).isEqualTo(LocalTime.of(10, 0));
            assertThat(found.get().getEndTime()).isEqualTo(LocalTime.of(10, 30));
            assertThat(found.get().getType()).isEqualTo(AppointmentType.GENERAL);
            assertThat(found.get().getReason()).isEqualTo("Checkup");
        }

        @Test
        @DisplayName("returns empty Optional for nonexistent id")
        void returnsEmptyForNonexistentId() {
            Optional<Appointment> found = appointmentRepository.findById(UUID.randomUUID());

            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("saves all fields correctly including timestamps")
        void savesTimestamps() {
            Appointment saved = createAndSaveAppointment(
                    LocalDate.now().plusDays(1), LocalTime.of(10, 0), LocalTime.of(10, 30));

            assertThat(saved.getCreatedAt()).isNotNull();
            assertThat(saved.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("returns correct count after saves")
        void countAfterSaves() {
            createAndSaveAppointment(LocalDate.now().plusDays(1), LocalTime.of(10, 0), LocalTime.of(10, 30));
            createAndSaveAppointment(LocalDate.now().plusDays(1), LocalTime.of(11, 0), LocalTime.of(11, 30));

            assertThat(appointmentRepository.count()).isEqualTo(2);
        }
    }
}
