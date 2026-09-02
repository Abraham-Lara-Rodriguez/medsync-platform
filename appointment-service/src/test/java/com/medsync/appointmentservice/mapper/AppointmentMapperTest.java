package com.medsync.appointmentservice.mapper;

import com.medsync.appointmentservice.domain.entity.Appointment;
import com.medsync.appointmentservice.domain.enums.AppointmentStatus;
import com.medsync.appointmentservice.domain.enums.AppointmentType;
import com.medsync.appointmentservice.dto.response.AppointmentResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AppointmentMapperTest {

    private final AppointmentMapper mapper = Mappers.getMapper(AppointmentMapper.class);

    @Test
    @DisplayName("maps all fields from Appointment to AppointmentResponse")
    void mapsAllFields() {
        Appointment appointment = Appointment.create(
                UUID.randomUUID(), UUID.randomUUID(), AppointmentType.GENERAL, "Checkup", "Notes");
        appointment.updateSchedule(LocalDate.now().plusDays(1), LocalTime.of(10, 0), LocalTime.of(10, 30));

        // Use reflection to set id/timestamps since they're protected by JPA
        // In real usage MapStruct uses the getters which work fine with JPA

        AppointmentResponse response = mapper.toResponse(appointment);

        assertThat(response.id()).isEqualTo(appointment.getId());
        assertThat(response.patientId()).isEqualTo(appointment.getPatientId());
        assertThat(response.doctorId()).isEqualTo(appointment.getDoctorId());
        assertThat(response.appointmentDate()).isEqualTo(appointment.getAppointmentDate());
        assertThat(response.startTime()).isEqualTo(appointment.getStartTime());
        assertThat(response.endTime()).isEqualTo(appointment.getEndTime());
        assertThat(response.type()).isEqualTo(appointment.getType());
        assertThat(response.status()).isEqualTo(appointment.getStatus());
        assertThat(response.reason()).isEqualTo(appointment.getReason());
        assertThat(response.notes()).isEqualTo(appointment.getNotes());
        assertThat(response.createdAt()).isEqualTo(appointment.getCreatedAt());
        assertThat(response.updatedAt()).isEqualTo(appointment.getUpdatedAt());
    }

    @Test
    @DisplayName("handles null reason and notes")
    void handlesNullFields() {
        Appointment appointment = Appointment.create(
                UUID.randomUUID(), UUID.randomUUID(), AppointmentType.FOLLOW_UP, null, null);
        appointment.updateSchedule(LocalDate.now().plusDays(1), LocalTime.of(10, 0), LocalTime.of(10, 30));

        AppointmentResponse response = mapper.toResponse(appointment);

        assertThat(response.reason()).isNull();
        assertThat(response.notes()).isNull();
    }
}