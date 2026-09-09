package com.medsync.appointmentservice.integration;

import com.medsync.appointmentservice.client.patient.config.PatientClient;
import com.medsync.appointmentservice.client.patient.dto.PatientResponse;
import com.medsync.appointmentservice.client.patient.enums.BloodType;
import com.medsync.appointmentservice.client.patient.enums.Gender;
import com.medsync.appointmentservice.client.patient.enums.PatientStatus;
import com.medsync.appointmentservice.domain.entity.Appointment;
import com.medsync.appointmentservice.domain.enums.AppointmentStatus;
import com.medsync.appointmentservice.domain.enums.AppointmentType;
import com.medsync.appointmentservice.repository.AppointmentRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class AppointmentControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String USER_EMAIL = "user@medsync.test";
    private static final String ADMIN_EMAIL = "admin@medsync.test";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @MockitoBean
    private PatientClient patientClient;

    private UUID patientId;
    private UUID doctorId;
    private UUID appointmentId;

    @BeforeEach
    void setUp() {
        appointmentRepository.deleteAll();

        patientId = UUID.randomUUID();
        doctorId = UUID.randomUUID();

        // The real patient-service is not running in tests; stub the Feign client.
        PatientResponse patient = new PatientResponse(
                patientId, "Juan", "Pérez", "12345678",
                Gender.MALE, LocalDate.of(1990, 1, 15), "+573001234567",
                "juan@medsync.test", "Calle 123",
                BloodType.O_POSITIVE, PatientStatus.ACTIVE
        );
        when(patientClient.getPatientById(patientId)).thenReturn(patient);

        Appointment appointment = Appointment.create(
                patientId, doctorId, AppointmentType.GENERAL, "Checkup", "Notes");
        appointment.updateSchedule(LocalDate.now().plusDays(1), LocalTime.of(10, 0), LocalTime.of(10, 30));
        appointmentId = appointmentRepository.saveAndFlush(appointment).getId();
    }

    /* ================= GET ALL ================= */

    @Test
    @DisplayName("GET /appointments returns persisted appointments with APPOINTMENT_READ")
    void getAllAppointmentsAllowed() throws Exception {
        mockMvc.perform(get("/api/v1/appointments")
                        .header("Authorization", bearerToken(USER_EMAIL, List.of("ROLE_USER", "APPOINTMENT_READ")))
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(appointmentId.toString()))
                .andExpect(jsonPath("$.content[0].patientId").value(patientId.toString()))
                .andExpect(jsonPath("$.content[0].status").value("SCHEDULED"));
    }

    @Test
    @DisplayName("GET /appointments without APPOINTMENT_READ returns 403")
    void getAllAppointmentsForbiddenWithoutAuthority() throws Exception {
        mockMvc.perform(get("/api/v1/appointments")
                        .header("Authorization", bearerToken(USER_EMAIL, List.of("ROLE_USER"))))
                .andExpect(status().isForbidden());
    }

    /* ================= GET BY ID ================= */

    @Test
    @DisplayName("GET /appointments/{id} returns appointment with APPOINTMENT_READ")
    void getAppointmentById() throws Exception {
        mockMvc.perform(get("/api/v1/appointments/{id}", appointmentId)
                        .header("Authorization", bearerToken(USER_EMAIL, List.of("ROLE_USER", "APPOINTMENT_READ"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(appointmentId.toString()))
                .andExpect(jsonPath("$.patientId").value(patientId.toString()))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }

    @Test
    @DisplayName("GET /appointments/{id} returns 404 when appointment does not exist")
    void getAppointmentByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/appointments/{id}", UUID.randomUUID())
                        .header("Authorization", bearerToken(USER_EMAIL, List.of("ROLE_USER", "APPOINTMENT_READ"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.type").value("https://appointment-service/errors/resource-not-found"));
    }

    /* ================= CREATE ================= */

    @Test
    @DisplayName("POST /appointments creates an appointment with APPOINTMENT_CREATE")
    void createAppointment() throws Exception {
        String body = createAppointmentJson(patientId, doctorId);

        mockMvc.perform(post("/api/v1/appointments")
                        .header("Authorization", bearerToken(USER_EMAIL, List.of("ROLE_USER", "APPOINTMENT_CREATE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/appointments/")))
                .andExpect(jsonPath("$.patientId").value(patientId.toString()))
                .andExpect(jsonPath("$.doctorId").value(doctorId.toString()))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }

    @Test
    @DisplayName("POST /appointments rejects malformed JSON")
    void createAppointmentMalformedJson() throws Exception {
        mockMvc.perform(post("/api/v1/appointments")
                        .header("Authorization", bearerToken(USER_EMAIL, List.of("ROLE_USER", "APPOINTMENT_CREATE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("https://appointment-service/errors/malformed-json"))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("POST /appointments rejects invalid DTO")
    void createAppointmentValidationError() throws Exception {
        mockMvc.perform(post("/api/v1/appointments")
                        .header("Authorization", bearerToken(USER_EMAIL, List.of("ROLE_USER", "APPOINTMENT_CREATE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidCreateAppointmentJson()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("https://appointment-service/errors/validation-error"))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.detail").isNotEmpty());
    }

    /* ================= UPDATE ================= */

    @Test
    @DisplayName("PUT /appointments/{id} updates appointment with APPOINTMENT_UPDATE")
    void updateAppointment() throws Exception {
        mockMvc.perform(put("/api/v1/appointments/{id}", appointmentId)
                        .header("Authorization", bearerToken(USER_EMAIL, List.of("ROLE_USER", "APPOINTMENT_UPDATE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateAppointmentJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(appointmentId.toString()))
                .andExpect(jsonPath("$.type").value("FOLLOW_UP"))
                .andExpect(jsonPath("$.reason").value("Follow-up visit"))
                .andExpect(jsonPath("$.notes").value("Updated notes"));

        Appointment updated = appointmentRepository.findById(appointmentId).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(AppointmentType.FOLLOW_UP, updated.getType());
        org.junit.jupiter.api.Assertions.assertEquals("Follow-up visit", updated.getReason());
    }

    @Test
    @DisplayName("PUT /appointments/{id} returns 400 when schedule is invalid")
    void updateAppointmentInvalidSchedule() throws Exception {
        mockMvc.perform(put("/api/v1/appointments/{id}", appointmentId)
                        .header("Authorization", bearerToken(USER_EMAIL, List.of("ROLE_USER", "APPOINTMENT_UPDATE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateAppointmentJson(LocalDate.now().minusDays(1))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("https://appointment-service/errors/invalid-schedule"));
    }

    /* ================= UPDATE STATUS ================= */

    @Test
    @DisplayName("PATCH /appointments/status/{id} updates status with APPOINTMENT_CANCEL")
    void updateAppointmentStatus() throws Exception {
        mockMvc.perform(patch("/api/v1/appointments/status/{id}", appointmentId)
                        .header("Authorization", bearerToken(USER_EMAIL, List.of("ROLE_USER", "APPOINTMENT_CANCEL")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusJson("CONFIRMED")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    @DisplayName("PATCH /appointments/status/{id} without APPOINTMENT_CANCEL returns 403")
    void updateAppointmentStatusForbidden() throws Exception {
        mockMvc.perform(patch("/api/v1/appointments/status/{id}", appointmentId)
                        .header("Authorization", bearerToken(USER_EMAIL, List.of("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusJson("CONFIRMED")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /appointments/status/{id} rejects invalid state transition")
    void updateAppointmentStatusInvalidTransition() throws Exception {
        mockMvc.perform(patch("/api/v1/appointments/status/{id}", appointmentId)
                        .header("Authorization", bearerToken(USER_EMAIL, List.of("ROLE_USER", "APPOINTMENT_CANCEL")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusJson("COMPLETED")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("https://appointment-service/errors/invalid-status-transition"));
    }

    /* ================= SECURITY ================= */

    @Test
    @DisplayName("Protected endpoint without token returns 401")
    void protectedEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/appointments"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Protected endpoint with malformed token returns 401")
    void protectedEndpointWithMalformedToken() throws Exception {
        mockMvc.perform(get("/api/v1/appointments")
                        .header("Authorization", "Bearer not-a-valid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Refresh token cannot be used as access token")
    void refreshTokenIsRejected() throws Exception {
        mockMvc.perform(get("/api/v1/appointments")
                        .header("Authorization", bearerToken(USER_EMAIL, List.of("ROLE_USER", "APPOINTMENT_READ"), "refresh")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Actuator health is public")
    void healthIsPublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    /* ================= HELPERS ================= */

    private String bearerToken(String subject, List<String> authorities) {
        return bearerToken(subject, authorities, "access");
    }

    private String bearerToken(String subject, List<String> authorities, String type) {
        Instant now = Instant.now();
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(JWT_SECRET_BASE64));

        String token = Jwts.builder()
                .issuer("medsync-auth-service")
                .audience().add("medsync-platform").and()
                .subject(subject)
                .issuedAt(Date.from(now))
                .notBefore(Date.from(now))
                .expiration(Date.from(now.plusSeconds(900)))
                .id(UUID.randomUUID().toString())
                .claim("type", type)
                .claim("roles", authorities)
                .signWith(key)
                .compact();

        return "Bearer " + token;
    }

    /* ================= ADDITIONAL TESTS ================= */

    @Test
    @DisplayName("POST /appointments returns 409 when patient is inactive")
    void createAppointmentInactivePatient() throws Exception {
        UUID inactivePatientId = UUID.randomUUID();
        PatientResponse inactivePatient = new PatientResponse(
                inactivePatientId, "Ana", "García", "87654321",
                Gender.FEMALE, LocalDate.of(1985, 5, 20), "+573009876543",
                "ana@medsync.test", "Avenida 456",
                BloodType.A_NEGATIVE, PatientStatus.INACTIVE
        );
        when(patientClient.getPatientById(inactivePatientId)).thenReturn(inactivePatient);

        String body = createAppointmentJson(inactivePatientId, doctorId);

        mockMvc.perform(post("/api/v1/appointments")
                        .header("Authorization", bearerToken(USER_EMAIL, List.of("ROLE_USER", "APPOINTMENT_CREATE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value("https://appointment-service/errors/patient-inactive"))
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    @Test
    @DisplayName("POST /appointments returns 409 when patient is not found")
    void createAppointmentPatientNotFound() throws Exception {
        UUID nonExistentPatientId = UUID.randomUUID();
        when(patientClient.getPatientById(nonExistentPatientId)).thenReturn(null);

        String body = createAppointmentJson(nonExistentPatientId, doctorId);

        mockMvc.perform(post("/api/v1/appointments")
                        .header("Authorization", bearerToken(USER_EMAIL, List.of("ROLE_USER", "APPOINTMENT_CREATE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("https://appointment-service/errors/patient-not-found"))
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    @DisplayName("POST /appointments returns 409 when doctor has overlapping appointment")
    void createAppointmentDoctorOverlap() throws Exception {
        // Create an existing appointment for the same doctor at same date and time
        // createAppointmentJson uses LocalDate.now().plusDays(2), 14:00 - 14:45
        LocalDate overlapDate = LocalDate.now().plusDays(2);
        Appointment existing = Appointment.create(
                patientId, doctorId, AppointmentType.GENERAL, "Existing", null);
        existing.updateSchedule(overlapDate, LocalTime.of(14, 0), LocalTime.of(14, 45));
        appointmentRepository.saveAndFlush(existing);

        String body = createAppointmentJson(patientId, doctorId);

        mockMvc.perform(post("/api/v1/appointments")
                        .header("Authorization", bearerToken(USER_EMAIL, List.of("ROLE_USER", "APPOINTMENT_CREATE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value("https://appointment-service/errors/appointment-conflict"))
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    @Nested
    @DisplayName("PUT /appointments/{id} - additional cases")
    class UpdateAppointmentAdditional {

        @Test
        @DisplayName("returns 409 when appointment status is CANCELLED")
        void updateCancelledAppointmentForbidden() throws Exception {
            // Create a CANCELLED appointment
            Appointment cancelled = Appointment.create(patientId, doctorId, AppointmentType.GENERAL, "Checkup", null);
            cancelled.updateSchedule(LocalDate.now().plusDays(2), LocalTime.of(10, 0), LocalTime.of(10, 30));
            cancelled.confirm();
            cancelled.cancel();
            UUID cancelledId = appointmentRepository.saveAndFlush(cancelled).getId();

            mockMvc.perform(put("/api/v1/appointments/{id}", cancelledId)
                            .header("Authorization", bearerToken(USER_EMAIL, List.of("ROLE_USER", "APPOINTMENT_UPDATE")))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updateAppointmentJson()))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.type").value("https://appointment-service/errors/appointment-modification-not-allowed"))
                    .andExpect(jsonPath("$.code").value("CONFLICT"));
        }

        @Test
        @DisplayName("returns 409 when appointment status is NO_SHOW")
        void updateNoShowAppointmentForbidden() throws Exception {
            Appointment noShow = Appointment.create(patientId, doctorId, AppointmentType.GENERAL, "Checkup", null);
            noShow.updateSchedule(LocalDate.now().plusDays(2), LocalTime.of(10, 0), LocalTime.of(10, 30));
            noShow.confirm();
            noShow.markNoShow();
            UUID noShowId = appointmentRepository.saveAndFlush(noShow).getId();

            mockMvc.perform(put("/api/v1/appointments/{id}", noShowId)
                            .header("Authorization", bearerToken(USER_EMAIL, List.of("ROLE_USER", "APPOINTMENT_UPDATE")))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updateAppointmentJson()))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.type").value("https://appointment-service/errors/appointment-modification-not-allowed"))
                    .andExpect(jsonPath("$.code").value("CONFLICT"));
        }

        @Test
        @DisplayName("returns 409 when new schedule overlaps existing appointment")
        void updateWithOverlapForbidden() throws Exception {
            // Create another appointment for same doctor at overlapping time
            LocalDate overlapDate = LocalDate.now().plusDays(3);
            Appointment overlap = Appointment.create(patientId, doctorId, AppointmentType.GENERAL, "Other", null);
            overlap.updateSchedule(overlapDate, LocalTime.of(9, 0), LocalTime.of(9, 30));
            appointmentRepository.saveAndFlush(overlap);

            // Try to update our appointment to overlap
            mockMvc.perform(put("/api/v1/appointments/{id}", appointmentId)
                            .header("Authorization", bearerToken(USER_EMAIL, List.of("ROLE_USER", "APPOINTMENT_UPDATE")))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updateAppointmentJson(overlapDate)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.type").value("https://appointment-service/errors/appointment-conflict"))
                    .andExpect(jsonPath("$.code").value("CONFLICT"));
        }
    }

    @Nested
    @DisplayName("PATCH /appointments/status/{id} - additional cases")
    class UpdateAppointmentStatusAdditional {

        @Test
        @DisplayName("returns 400 when transitioning from SCHEDULED to COMPLETED")
        void completeFromScheduledInvalid() throws Exception {
            mockMvc.perform(patch("/api/v1/appointments/status/{id}", appointmentId)
                            .header("Authorization", bearerToken(USER_EMAIL, List.of("ROLE_USER", "APPOINTMENT_CANCEL")))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(statusJson("COMPLETED")))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.type").value("https://appointment-service/errors/invalid-status-transition"))
                    .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("returns 400 when trying to set status to SCHEDULED")
        void setToScheduledInvalid() throws Exception {
            mockMvc.perform(patch("/api/v1/appointments/status/{id}", appointmentId)
                            .header("Authorization", bearerToken(USER_EMAIL, List.of("ROLE_USER", "APPOINTMENT_CANCEL")))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(statusJson("SCHEDULED")))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.type").value("https://appointment-service/errors/invalid-status-transition"))
                    .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("completes appointment after confirming it")
        void completeAfterConfirm() throws Exception {
            // First confirm
            mockMvc.perform(patch("/api/v1/appointments/status/{id}", appointmentId)
                            .header("Authorization", bearerToken(USER_EMAIL, List.of("ROLE_USER", "APPOINTMENT_CANCEL")))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(statusJson("CONFIRMED")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("CONFIRMED"));

            // Then complete
            mockMvc.perform(patch("/api/v1/appointments/status/{id}", appointmentId)
                            .header("Authorization", bearerToken(USER_EMAIL, List.of("ROLE_USER", "APPOINTMENT_CANCEL")))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(statusJson("COMPLETED")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("COMPLETED"));
        }

        @Test
        @DisplayName("cancels appointment from CONFIRMED status")
        void cancelFromConfirmed() throws Exception {
            // First confirm
            mockMvc.perform(patch("/api/v1/appointments/status/{id}", appointmentId)
                            .header("Authorization", bearerToken(USER_EMAIL, List.of("ROLE_USER", "APPOINTMENT_CANCEL")))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(statusJson("CONFIRMED")))
                    .andExpect(status().isOk());

            // Then cancel
            mockMvc.perform(patch("/api/v1/appointments/status/{id}", appointmentId)
                            .header("Authorization", bearerToken(USER_EMAIL, List.of("ROLE_USER", "APPOINTMENT_CANCEL")))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(statusJson("CANCELLED")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("CANCELLED"));
        }

        @Test
        @DisplayName("marks no-show from CONFIRMED status")
        void markNoShowFromConfirmed() throws Exception {
            // First confirm
            mockMvc.perform(patch("/api/v1/appointments/status/{id}", appointmentId)
                            .header("Authorization", bearerToken(USER_EMAIL, List.of("ROLE_USER", "APPOINTMENT_CANCEL")))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(statusJson("CONFIRMED")))
                    .andExpect(status().isOk());

            // Then mark no-show
            mockMvc.perform(patch("/api/v1/appointments/status/{id}", appointmentId)
                            .header("Authorization", bearerToken(USER_EMAIL, List.of("ROLE_USER", "APPOINTMENT_CANCEL")))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(statusJson("NO_SHOW")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("NO_SHOW"));
        }
    }

    private String createAppointmentJson(UUID patientId, UUID doctorId) {
        return """
                {
                  "patientId": "%s",
                  "doctorId": "%s",
                  "appointmentDate": "%s",
                  "startTime": "14:00:00",
                  "endTime": "14:45:00",
                  "type": "SPECIALIST",
                  "reason": "Specialist consult",
                  "notes": "Initial notes"
                }
                """.formatted(patientId, doctorId, LocalDate.now().plusDays(2));
    }

    private String invalidCreateAppointmentJson() {
        return """
                {
                  "patientId": null,
                  "doctorId": null,
                  "appointmentDate": null,
                  "startTime": null,
                  "endTime": null,
                  "type": null,
                  "reason": "%s"
                }
                """.formatted("x".repeat(501));
    }

    private String updateAppointmentJson() {
        return updateAppointmentJson(LocalDate.now().plusDays(3));
    }

    private String updateAppointmentJson(LocalDate date) {
        return """
                {
                  "appointmentDate": "%s",
                  "startTime": "09:00:00",
                  "endTime": "09:30:00",
                  "type": "FOLLOW_UP",
                  "reason": "Follow-up visit",
                  "notes": "Updated notes"
                }
                """.formatted(date);
    }

    private String statusJson(String status) {
        return """
                {
                  "status": "%s"
                }
                """.formatted(status);
    }
}
