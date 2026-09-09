package com.medsync.appointmentservice.domain.entity;

import com.medsync.appointmentservice.domain.enums.AppointmentStatus;
import com.medsync.appointmentservice.domain.enums.AppointmentType;
import com.medsync.appointmentservice.exception.custom.InvalidAppointmentStatusTransitionException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "appointment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Appointment {

    private static final int MAX_REASON_LENGTH = 500;
    private static final int MAX_NOTES_LENGTH = 2000;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID patientId;

    @Column(nullable = false)
    private UUID doctorId;

    @Column(nullable = false)
    private LocalDate appointmentDate;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status;

    @Column(length = 500)
    private String reason;

    @Column(length = 2000)
    private String notes;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    @Version
    private Integer version;

    // Factory method público
    public static Appointment create(UUID patientId, UUID doctorId, AppointmentType type,
                                     String reason, String notes) {
        Appointment appointment = new Appointment();
        appointment.setPatientId(patientId);
        appointment.setDoctorId(doctorId);
        appointment.setType(type);
        appointment.status = AppointmentStatus.SCHEDULED;
        appointment.setReason(reason);
        appointment.setNotes(notes);
        return appointment;
    }

    //------------------//
    // domain methods //
    //---------------//
    public void setPatientId(UUID patientId) {
        if (patientId == null) {
            throw new IllegalArgumentException("Patient id cannot be null");
        }
        this.patientId = patientId;
    }

    public void setDoctorId(UUID doctorId) {
        if (doctorId == null) {
            throw new IllegalArgumentException("Doctor id cannot be null");
        }
        this.doctorId = doctorId;
    }

    public void updateSchedule(LocalDate appointmentDate, LocalTime startTime, LocalTime endTime) {
        this.appointmentDate = appointmentDate;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public void setType(AppointmentType type) {
        if (type == null) {
            throw new IllegalArgumentException("Appointment type cannot be null");
        }
        this.type = type;
    }

    // Methods for change status appointment
    public void confirm() {
        if (status != AppointmentStatus.SCHEDULED) {
            throw new InvalidAppointmentStatusTransitionException(
                    "Only scheduled appointments can be confirmed"
            );
        }
        this.status = AppointmentStatus.CONFIRMED;
    }

    public void complete() {
        if (status != AppointmentStatus.CONFIRMED) {
            throw new InvalidAppointmentStatusTransitionException(
                    "Only confirmed appointments can be completed"
            );
        }

        this.status = AppointmentStatus.COMPLETED;
    }

    public void cancel() {
        if (status != AppointmentStatus.SCHEDULED
                && status != AppointmentStatus.CONFIRMED) {
            throw new InvalidAppointmentStatusTransitionException(
                    "Appointment cannot be cancelled from status " + status
            );
        }

        this.status = AppointmentStatus.CANCELLED;
    }

    public void markNoShow() {
        if (status != AppointmentStatus.CONFIRMED) {
            throw new InvalidAppointmentStatusTransitionException(
                    "Only confirmed appointments can be marked as no-show"
            );
        }

        this.status = AppointmentStatus.NO_SHOW;
    }


    public void setReason(String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            this.reason = null;
            return;
        }
        String trimmed = reason.trim();
        if (trimmed.length() > MAX_REASON_LENGTH) {
            throw new IllegalArgumentException("Reason cannot exceed " + MAX_REASON_LENGTH + " characters");
        }
        this.reason = trimmed;
    }

    public void setNotes(String notes) {
        if (notes == null || notes.trim().isEmpty()) {
            this.notes = null;
            return;
        }
        String trimmed = notes.trim();
        if (trimmed.length() > MAX_NOTES_LENGTH) {
            throw new IllegalArgumentException("Notes cannot exceed " + MAX_NOTES_LENGTH + " characters");
        }
        this.notes = trimmed;
    }

}
