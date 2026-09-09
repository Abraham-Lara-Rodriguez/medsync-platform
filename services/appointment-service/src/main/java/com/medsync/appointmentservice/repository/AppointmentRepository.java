package com.medsync.appointmentservice.repository;

import com.medsync.appointmentservice.domain.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    @Query("""
                SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END
                FROM Appointment a
                WHERE a.doctorId = :doctorId
                  AND a.appointmentDate = :appointmentDate
                  AND a.startTime < :endTime
                  AND a.endTime > :startTime
                  AND (:appointmentId IS NULL OR a.id <> :appointmentId)
            """)
    boolean existsOverlappingAppointment(
            @Param("doctorId") UUID doctorId,
            @Param("appointmentDate") LocalDate appointmentDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("appointmentId") UUID appointmentId
    );

}
