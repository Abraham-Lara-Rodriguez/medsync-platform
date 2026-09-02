package com.medsync.appointmentservice.service;

import com.medsync.appointmentservice.client.patient.config.PatientClient;
import com.medsync.appointmentservice.client.patient.dto.PatientResponse;
import com.medsync.appointmentservice.client.patient.enums.PatientStatus;
import com.medsync.appointmentservice.domain.entity.Appointment;
import com.medsync.appointmentservice.domain.enums.AppointmentStatus;
import com.medsync.appointmentservice.domain.validator.AppointmentScheduleValidator;
import com.medsync.appointmentservice.dto.request.CreateAppointmentRequest;
import com.medsync.appointmentservice.dto.request.UpdateAppointmentRequest;
import com.medsync.appointmentservice.dto.request.UpdateAppointmentStatusRequest;
import com.medsync.appointmentservice.dto.response.AppointmentResponse;
import com.medsync.appointmentservice.exception.custom.*;
import com.medsync.appointmentservice.mapper.AppointmentMapper;
import com.medsync.appointmentservice.repository.AppointmentRepository;
import com.medsync.commoncore.error.custom.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentScheduleValidator appointmentScheduleValidator;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper appointmentMapper;
    private final PatientClient patientClient;

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getAllAppointments(Pageable pageable) {
        return appointmentRepository
                .findAll(pageable)
                .map(appointmentMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponse getAppointmentById(UUID id) {
        Appointment appointment = findAppointmentOrThrow(id);
        return appointmentMapper.toResponse(appointment);
    }

    @Override
    @Transactional
    public AppointmentResponse createAppointment(CreateAppointmentRequest request) {

        // 1. Patient must exist and be active
        checkPatient(request.patientId());

        // 2. Validate date/time
        validateSchedule(
                request.doctorId(),
                request.appointmentDate(),
                request.startTime(),
                request.endTime(),
                null
        );

        // 3. Create appointment
        Appointment appointment = Appointment.create(
                request.patientId(),
                request.doctorId(),
                request.type(),
                request.reason(),
                request.notes()
        );

        appointment.updateSchedule(
                request.appointmentDate(),
                request.startTime(),
                request.endTime()
        );

        return appointmentMapper.toResponse(
                appointmentRepository.save(appointment)
        );
    }

    @Override
    @Transactional
    public AppointmentResponse updateAppointment(
            UpdateAppointmentRequest request,
            UUID id
    ) {
        Appointment appointment = findAppointmentOrThrow(id);

        // 1. Validate whether appointment can be modified
        validateAppointmentCanBeModified(appointment);

        // 2. Validate date/time and doctor availability
        validateSchedule(
                appointment.getDoctorId(),
                request.appointmentDate(),
                request.startTime(),
                request.endTime(),
                id
        );

        // 3. Update schedule
        appointment.updateSchedule(
                request.appointmentDate(),
                request.startTime(),
                request.endTime()
        );

        appointment.setType(request.type());
        appointment.setReason(request.reason());
        appointment.setNotes(request.notes());

        return appointmentMapper.toResponse(
                appointmentRepository.save(appointment)
        );
    }

    @Override
    @Transactional
    public AppointmentResponse updateAppointmentStatus(
            UpdateAppointmentStatusRequest request,
            UUID id
    ) {
        Appointment appointment = findAppointmentOrThrow(id);

        switch (request.status()) {
            case CONFIRMED -> appointment.confirm();
            case COMPLETED -> appointment.complete();
            case CANCELLED -> appointment.cancel();
            case NO_SHOW -> appointment.markNoShow();
            case SCHEDULED -> throw new InvalidAppointmentStatusTransitionException(
                    "Appointment cannot be manually returned to SCHEDULED"
            );
        }

        return appointmentMapper.toResponse(
                appointmentRepository.save(appointment)
        );
    }

    /* ================= HELPERS ================= */
    private Appointment findAppointmentOrThrow(UUID id) {
        return appointmentRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException(
                        "Appointment not found with id: " + id
                )
        );
    }

    private void validateAppointmentCanBeModified(Appointment appointment) {
        if (appointment.getStatus() == AppointmentStatus.COMPLETED
                || appointment.getStatus() == AppointmentStatus.CANCELLED
                || appointment.getStatus() == AppointmentStatus.NO_SHOW) {

            throw new AppointmentModificationNotAllowedException(
                    "Appointment cannot be modified in status "
                            + appointment.getStatus()
            );
        }
    }

    private PatientResponse checkPatient(UUID id) {
        PatientResponse patient = patientClient.getPatientById(id);

        if (patient == null) {
            throw new PatientNotFoundException(
                    "Patient not found with id: " + id
            );
        }

        if (patient.status() != PatientStatus.ACTIVE) {
            throw new PatientInactiveException(
                    "Patient is not active"
            );
        }

        return patient;
    }


    // TODO: validate that the doctor exists and is active once doctor-service is available
    // doctorClient.getDoctorById(doctorId); // Future Feign client
    private void validateSchedule(UUID doctorId, LocalDate appointmentDate, LocalTime startTime,
                                  LocalTime endTime, UUID appointmentId) {
        appointmentScheduleValidator.validate(
                appointmentDate,
                startTime,
                endTime
        );

        boolean overlapping = appointmentRepository.existsOverlappingAppointment(
                doctorId,
                appointmentDate,
                startTime,
                endTime,
                appointmentId
        );

        if (overlapping) {
            throw new AppointmentConflictException(
                    "Doctor already has an appointment during the requested time"
            );
        }
    }
}