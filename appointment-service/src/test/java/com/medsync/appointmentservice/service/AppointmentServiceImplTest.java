package com.medsync.appointmentservice.service;

import com.medsync.appointmentservice.client.patient.config.PatientClient;
import com.medsync.appointmentservice.client.patient.dto.PatientResponse;
import com.medsync.appointmentservice.client.patient.enums.Gender;
import com.medsync.appointmentservice.client.patient.enums.PatientStatus;
import com.medsync.appointmentservice.client.patient.enums.BloodType;
import com.medsync.appointmentservice.domain.entity.Appointment;
import com.medsync.appointmentservice.domain.enums.AppointmentStatus;
import com.medsync.appointmentservice.domain.enums.AppointmentType;
import com.medsync.appointmentservice.domain.validator.AppointmentScheduleValidator;
import com.medsync.appointmentservice.dto.request.CreateAppointmentRequest;
import com.medsync.appointmentservice.dto.request.UpdateAppointmentRequest;
import com.medsync.appointmentservice.dto.request.UpdateAppointmentStatusRequest;
import com.medsync.appointmentservice.dto.response.AppointmentResponse;
import com.medsync.appointmentservice.exception.custom.*;
import com.medsync.appointmentservice.mapper.AppointmentMapper;
import com.medsync.appointmentservice.repository.AppointmentRepository;
import com.medsync.commoncore.error.custom.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceImplTest {

    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private AppointmentMapper appointmentMapper;
    @Mock
    private AppointmentScheduleValidator appointmentScheduleValidator;
    @Mock
    private PatientClient patientClient;

    private AppointmentServiceImpl appointmentService;

    private static final UUID PATIENT_ID = UUID.randomUUID();
    private static final UUID DOCTOR_ID = UUID.randomUUID();
    private static final UUID APPOINTMENT_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        appointmentService = new AppointmentServiceImpl(
                appointmentScheduleValidator, appointmentRepository, appointmentMapper, patientClient);
    }

    private Appointment existingAppointment() {
        Appointment a = Appointment.create(PATIENT_ID, DOCTOR_ID, AppointmentType.GENERAL, "Checkup", null);
        a.updateSchedule(LocalDate.now().plusDays(1), LocalTime.of(10, 0), LocalTime.of(10, 30));
        // Use reflection to set id for testing
        return a;
    }

    private AppointmentResponse appointmentResponse(Appointment a) {
        return new AppointmentResponse(
                a.getId(), a.getPatientId(), a.getDoctorId(),
                a.getAppointmentDate(), a.getStartTime(), a.getEndTime(),
                a.getType(), a.getStatus(),
                a.getReason(), a.getNotes(),
                a.getCreatedAt(), a.getUpdatedAt()
        );
    }

    private PatientResponse patient(PatientStatus status) {
        return new PatientResponse(
                PATIENT_ID, "Juan", "Pérez", "12345678",
                Gender.MALE, LocalDate.of(1990, 1, 15), "+573001234567",
                "juan@medsync.test", "Calle 123",
                BloodType.O_POSITIVE, status
        );
    }

    private CreateAppointmentRequest createRequest() {
        return new CreateAppointmentRequest(
                PATIENT_ID, DOCTOR_ID,
                LocalDate.now().plusDays(1), LocalTime.of(10, 0), LocalTime.of(10, 30),
                AppointmentType.GENERAL, "Checkup", "Notes"
        );
    }

    private UpdateAppointmentRequest updateRequest() {
        return new UpdateAppointmentRequest(
                LocalDate.now().plusDays(2), LocalTime.of(11, 0), LocalTime.of(11, 45),
                AppointmentType.FOLLOW_UP, "Follow-up", "New notes"
        );
    }

    private void stubActivePatient() {
        when(patientClient.getPatientById(PATIENT_ID)).thenReturn(patient(PatientStatus.ACTIVE));
    }

    private void stubNoOverlap() {
        when(appointmentRepository.existsOverlappingAppointment(any(), any(), any(), any(), isNull())).thenReturn(false);
    }

    // ============================================================
    // getAllAppointments
    // ============================================================

    @Nested
    @DisplayName("getAllAppointments")
    class GetAllAppointments {

        @Test
        @DisplayName("maps the repository page through the mapper")
        void mapsRepositoryPage() {
            Pageable pageable = Pageable.unpaged();
            Appointment appointment = existingAppointment();
            AppointmentResponse response = appointmentResponse(appointment);

            when(appointmentRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(appointment)));
            when(appointmentMapper.toResponse(appointment)).thenReturn(response);

            Page<AppointmentResponse> result = appointmentService.getAllAppointments(pageable);

            assertThat(result.getContent()).containsExactly(response);
        }
    }

    // ============================================================
    // getAppointmentById
    // ============================================================

    @Nested
    @DisplayName("getAppointmentById")
    class GetAppointmentById {

        @Test
        @DisplayName("returns the mapped appointment when found")
        void returnsAppointmentWhenFound() {
            UUID id = UUID.randomUUID();
            Appointment appointment = existingAppointment();
            AppointmentResponse response = appointmentResponse(appointment);

            when(appointmentRepository.findById(id)).thenReturn(Optional.of(appointment));
            when(appointmentMapper.toResponse(appointment)).thenReturn(response);

            AppointmentResponse result = appointmentService.getAppointmentById(id);

            assertThat(result).isEqualTo(response);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when not found")
        void throwsWhenNotFound() {
            UUID id = UUID.randomUUID();
            when(appointmentRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> appointmentService.getAppointmentById(id))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(id.toString());
        }
    }

    // ============================================================
    // createAppointment
    // ============================================================

    @Nested
    @DisplayName("createAppointment")
    class CreateAppointment {

        @Test
        @DisplayName("creates and saves appointment when patient is active")
        void createsWhenPatientActive() {
            stubActivePatient();
            doNothing().when(appointmentScheduleValidator).validate(any(), any(), any());
            stubNoOverlap();

            Appointment saved = existingAppointment();
            AppointmentResponse response = appointmentResponse(saved);
            when(appointmentRepository.save(any(Appointment.class))).thenReturn(saved);
            when(appointmentMapper.toResponse(saved)).thenReturn(response);

            AppointmentResponse result = appointmentService.createAppointment(createRequest());

            assertThat(result).isEqualTo(response);

            ArgumentCaptor<Appointment> captor = ArgumentCaptor.forClass(Appointment.class);
            verify(appointmentRepository).save(captor.capture());
            assertThat(captor.getValue().getPatientId()).isEqualTo(PATIENT_ID);
            assertThat(captor.getValue().getDoctorId()).isEqualTo(DOCTOR_ID);
            assertThat(captor.getValue().getType()).isEqualTo(AppointmentType.GENERAL);
            assertThat(captor.getValue().getReason()).isEqualTo("Checkup");
            assertThat(captor.getValue().getNotes()).isEqualTo("Notes");
            assertThat(captor.getValue().getAppointmentDate()).isEqualTo(LocalDate.now().plusDays(1));
            assertThat(captor.getValue().getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
        }

        @Test
        @DisplayName("throws PatientNotFoundException when patient does not exist")
        void throwsWhenPatientNotFound() {
            when(patientClient.getPatientById(PATIENT_ID)).thenReturn(null);

            assertThatThrownBy(() -> appointmentService.createAppointment(createRequest()))
                    .isInstanceOf(PatientNotFoundException.class)
                    .hasMessageContaining(PATIENT_ID.toString());

            verify(appointmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws PatientInactiveException when patient is inactive")
        void throwsWhenPatientInactive() {
            when(patientClient.getPatientById(PATIENT_ID)).thenReturn(patient(PatientStatus.INACTIVE));

            assertThatThrownBy(() -> appointmentService.createAppointment(createRequest()))
                    .isInstanceOf(PatientInactiveException.class)
                    .hasMessageContaining("not active");

            verify(appointmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws InvalidAppointmentScheduleException when the schedule is invalid")
        void throwsWhenScheduleInvalid() {
            stubActivePatient();
            doThrow(new InvalidAppointmentScheduleException("Appointment must be scheduled in the future"))
                    .when(appointmentScheduleValidator).validate(any(), any(), any());

            assertThatThrownBy(() -> appointmentService.createAppointment(createRequest()))
                    .isInstanceOf(InvalidAppointmentScheduleException.class)
                    .hasMessageContaining("future");

            verify(appointmentRepository, never()).save(any());
            verify(appointmentRepository, never()).existsOverlappingAppointment(any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("throws AppointmentConflictException when doctor has overlapping appointment")
        void throwsWhenOverlapping() {
            stubActivePatient();
            doNothing().when(appointmentScheduleValidator).validate(any(), any(), any());
            when(appointmentRepository.existsOverlappingAppointment(any(), any(), any(), any(), isNull())).thenReturn(true);

            assertThatThrownBy(() -> appointmentService.createAppointment(createRequest()))
                    .isInstanceOf(AppointmentConflictException.class)
                    .hasMessageContaining("already has an appointment");

            verify(appointmentRepository, never()).save(any());
        }
    }

    // ============================================================
    // updateAppointment
    // ============================================================

    @Nested
    @DisplayName("updateAppointment")
    class UpdateAppointment {

        @Test
        @DisplayName("throws ResourceNotFoundException when appointment does not exist")
        void throwsWhenNotFound() {
            UUID id = UUID.randomUUID();
            when(appointmentRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> appointmentService.updateAppointment(updateRequest(), id))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(id.toString());

            verify(appointmentRepository, never()).save(any());
            verifyNoInteractions(appointmentScheduleValidator);
        }

        @Test
        @DisplayName("throws AppointmentModificationNotAllowedException when status is COMPLETED")
        void throwsWhenCompleted() {
            Appointment appointment = existingAppointment();
            appointment.confirm();
            appointment.complete();

            when(appointmentRepository.findById(APPOINTMENT_ID)).thenReturn(Optional.of(appointment));

            assertThatThrownBy(() -> appointmentService.updateAppointment(updateRequest(), APPOINTMENT_ID))
                    .isInstanceOf(AppointmentModificationNotAllowedException.class)
                    .hasMessageContaining("cannot be modified");

            verify(appointmentRepository, never()).save(any());
            verifyNoInteractions(appointmentScheduleValidator);
        }

        @Test
        @DisplayName("throws AppointmentModificationNotAllowedException when status is CANCELLED")
        void throwsWhenCancelled() {
            Appointment appointment = existingAppointment();
            appointment.cancel();

            when(appointmentRepository.findById(APPOINTMENT_ID)).thenReturn(Optional.of(appointment));

            assertThatThrownBy(() -> appointmentService.updateAppointment(updateRequest(), APPOINTMENT_ID))
                    .isInstanceOf(AppointmentModificationNotAllowedException.class)
                    .hasMessageContaining("cannot be modified");

            verify(appointmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws AppointmentModificationNotAllowedException when status is NO_SHOW")
        void throwsWhenNoShow() {
            Appointment appointment = existingAppointment();
            appointment.confirm();
            appointment.markNoShow();

            when(appointmentRepository.findById(APPOINTMENT_ID)).thenReturn(Optional.of(appointment));

            assertThatThrownBy(() -> appointmentService.updateAppointment(updateRequest(), APPOINTMENT_ID))
                    .isInstanceOf(AppointmentModificationNotAllowedException.class)
                    .hasMessageContaining("cannot be modified");

            verify(appointmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws AppointmentConflictException when new schedule overlaps another appointment")
        void throwsWhenOverlapping() {
            Appointment appointment = existingAppointment();

            when(appointmentRepository.findById(APPOINTMENT_ID)).thenReturn(Optional.of(appointment));
            doNothing().when(appointmentScheduleValidator).validate(any(), any(), any());
            when(appointmentRepository.existsOverlappingAppointment(any(), any(), any(), any(), any())).thenReturn(true);

            assertThatThrownBy(() -> appointmentService.updateAppointment(updateRequest(), APPOINTMENT_ID))
                    .isInstanceOf(AppointmentConflictException.class)
                    .hasMessageContaining("already has an appointment");

            verify(appointmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("excludes the current appointment id when checking for overlaps")
        void checksOverlapExcludingCurrentAppointment() {
            Appointment appointment = existingAppointment();

            when(appointmentRepository.findById(APPOINTMENT_ID)).thenReturn(Optional.of(appointment));
            doNothing().when(appointmentScheduleValidator).validate(any(), any(), any());
            when(appointmentRepository.existsOverlappingAppointment(any(), any(), any(), any(), any())).thenReturn(false);

            Appointment saved = appointment;
            AppointmentResponse response = appointmentResponse(saved);
            when(appointmentRepository.save(any(Appointment.class))).thenReturn(saved);
            when(appointmentMapper.toResponse(any(Appointment.class))).thenReturn(response);

            appointmentService.updateAppointment(updateRequest(), APPOINTMENT_ID);

            verify(appointmentRepository).existsOverlappingAppointment(
                    eq(appointment.getDoctorId()),
                    eq(LocalDate.now().plusDays(2)),
                    eq(LocalTime.of(11, 0)),
                    eq(LocalTime.of(11, 45)),
                    eq(APPOINTMENT_ID)
            );
        }

        @Test
        @DisplayName("updates schedule when appointment can be modified")
        void updatesWhenModifiable() {
            Appointment appointment = existingAppointment();
            AppointmentResponse response = appointmentResponse(appointment);

            when(appointmentRepository.findById(APPOINTMENT_ID)).thenReturn(Optional.of(appointment));
            doNothing().when(appointmentScheduleValidator).validate(any(), any(), any());
            when(appointmentRepository.existsOverlappingAppointment(any(), any(), any(), any(), any())).thenReturn(false);
            when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
            when(appointmentMapper.toResponse(any(Appointment.class))).thenReturn(response);

            AppointmentResponse result = appointmentService.updateAppointment(updateRequest(), APPOINTMENT_ID);

            assertThat(result).isEqualTo(response);

            ArgumentCaptor<Appointment> captor = ArgumentCaptor.forClass(Appointment.class);
            verify(appointmentRepository).save(captor.capture());
            assertThat(captor.getValue().getType()).isEqualTo(AppointmentType.FOLLOW_UP);
            assertThat(captor.getValue().getReason()).isEqualTo("Follow-up");
            assertThat(captor.getValue().getNotes()).isEqualTo("New notes");
            assertThat(captor.getValue().getAppointmentDate()).isEqualTo(LocalDate.now().plusDays(2));
            assertThat(captor.getValue().getStartTime()).isEqualTo(LocalTime.of(11, 0));
            assertThat(captor.getValue().getEndTime()).isEqualTo(LocalTime.of(11, 45));
        }

        @Test
        @DisplayName("does not modify status when updating appointment details")
        void keepsStatusUnchanged() {
            Appointment appointment = existingAppointment();
            appointment.confirm();

            when(appointmentRepository.findById(APPOINTMENT_ID)).thenReturn(Optional.of(appointment));
            doNothing().when(appointmentScheduleValidator).validate(any(), any(), any());
            when(appointmentRepository.existsOverlappingAppointment(any(), any(), any(), any(), any())).thenReturn(false);

            AppointmentResponse response = appointmentResponse(appointment);
            when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
            when(appointmentMapper.toResponse(any(Appointment.class))).thenReturn(response);

            appointmentService.updateAppointment(updateRequest(), APPOINTMENT_ID);

            ArgumentCaptor<Appointment> captor = ArgumentCaptor.forClass(Appointment.class);
            verify(appointmentRepository).save(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
        }
    }

    // ============================================================
    // updateAppointmentStatus
    // ============================================================

    @Nested
    @DisplayName("updateAppointmentStatus")
    class UpdateAppointmentStatus {

        @Test
        @DisplayName("throws ResourceNotFoundException when appointment does not exist")
        void throwsWhenNotFound() {
            UUID id = UUID.randomUUID();
            when(appointmentRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> appointmentService.updateAppointmentStatus(
                    new UpdateAppointmentStatusRequest(AppointmentStatus.CONFIRMED), id))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(id.toString());

            verify(appointmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws InvalidAppointmentStatusTransitionException when current status does not allow the transition")
        void throwsOnInvalidTransition() {
            Appointment appointment = existingAppointment();
            appointment.confirm();

            when(appointmentRepository.findById(APPOINTMENT_ID)).thenReturn(Optional.of(appointment));

            assertThatThrownBy(() -> appointmentService.updateAppointmentStatus(
                    new UpdateAppointmentStatusRequest(AppointmentStatus.CONFIRMED), APPOINTMENT_ID))
                    .isInstanceOf(InvalidAppointmentStatusTransitionException.class)
                    .hasMessageContaining("Only scheduled");

            verify(appointmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("confirms appointment")
        void confirms() {
            Appointment appointment = existingAppointment();
            AppointmentResponse response = appointmentResponse(appointment);

            when(appointmentRepository.findById(APPOINTMENT_ID)).thenReturn(Optional.of(appointment));
            when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
            when(appointmentMapper.toResponse(any(Appointment.class))).thenReturn(response);

            UpdateAppointmentStatusRequest request = new UpdateAppointmentStatusRequest(AppointmentStatus.CONFIRMED);

            AppointmentResponse result = appointmentService.updateAppointmentStatus(request, APPOINTMENT_ID);

            assertThat(result).isEqualTo(response);
            verify(appointmentRepository).save(argThat(a -> a.getStatus() == AppointmentStatus.CONFIRMED));
        }

        @Test
        @DisplayName("completes appointment")
        void completes() {
            Appointment appointment = existingAppointment();
            appointment.confirm();
            AppointmentResponse response = appointmentResponse(appointment);

            when(appointmentRepository.findById(APPOINTMENT_ID)).thenReturn(Optional.of(appointment));
            when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
            when(appointmentMapper.toResponse(any(Appointment.class))).thenReturn(response);

            UpdateAppointmentStatusRequest request = new UpdateAppointmentStatusRequest(AppointmentStatus.COMPLETED);

            AppointmentResponse result = appointmentService.updateAppointmentStatus(request, APPOINTMENT_ID);

            assertThat(result).isEqualTo(response);
            verify(appointmentRepository).save(argThat(a -> a.getStatus() == AppointmentStatus.COMPLETED));
        }

        @Test
        @DisplayName("cancels appointment")
        void cancels() {
            Appointment appointment = existingAppointment();
            AppointmentResponse response = appointmentResponse(appointment);

            when(appointmentRepository.findById(APPOINTMENT_ID)).thenReturn(Optional.of(appointment));
            when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
            when(appointmentMapper.toResponse(any(Appointment.class))).thenReturn(response);

            UpdateAppointmentStatusRequest request = new UpdateAppointmentStatusRequest(AppointmentStatus.CANCELLED);

            AppointmentResponse result = appointmentService.updateAppointmentStatus(request, APPOINTMENT_ID);

            assertThat(result).isEqualTo(response);
            verify(appointmentRepository).save(argThat(a -> a.getStatus() == AppointmentStatus.CANCELLED));
        }

        @Test
        @DisplayName("marks no-show")
        void marksNoShow() {
            Appointment appointment = existingAppointment();
            appointment.confirm();
            AppointmentResponse response = appointmentResponse(appointment);

            when(appointmentRepository.findById(APPOINTMENT_ID)).thenReturn(Optional.of(appointment));
            when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
            when(appointmentMapper.toResponse(any(Appointment.class))).thenReturn(response);

            UpdateAppointmentStatusRequest request = new UpdateAppointmentStatusRequest(AppointmentStatus.NO_SHOW);

            AppointmentResponse result = appointmentService.updateAppointmentStatus(request, APPOINTMENT_ID);

            assertThat(result).isEqualTo(response);
            verify(appointmentRepository).save(argThat(a -> a.getStatus() == AppointmentStatus.NO_SHOW));
        }

        @Test
        @DisplayName("throws when trying to set back to SCHEDULED")
        void throwsWhenSettingToScheduled() {
            Appointment appointment = existingAppointment();

            when(appointmentRepository.findById(APPOINTMENT_ID)).thenReturn(Optional.of(appointment));

            UpdateAppointmentStatusRequest request = new UpdateAppointmentStatusRequest(AppointmentStatus.SCHEDULED);

            assertThatThrownBy(() -> appointmentService.updateAppointmentStatus(request, APPOINTMENT_ID))
                    .isInstanceOf(InvalidAppointmentStatusTransitionException.class)
                    .hasMessageContaining("cannot be manually returned to SCHEDULED");
        }
    }
}