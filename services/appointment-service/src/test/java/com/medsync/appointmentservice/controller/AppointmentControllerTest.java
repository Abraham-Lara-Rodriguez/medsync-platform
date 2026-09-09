package com.medsync.appointmentservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medsync.appointmentservice.dto.request.CreateAppointmentRequest;
import com.medsync.appointmentservice.dto.request.UpdateAppointmentRequest;
import com.medsync.appointmentservice.dto.request.UpdateAppointmentStatusRequest;
import com.medsync.appointmentservice.dto.response.AppointmentResponse;
import com.medsync.appointmentservice.service.AppointmentService;
import com.medsync.commoncore.error.custom.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web-layer tests for {@link AppointmentController}. Security ({@code @PreAuthorize})
 * is disabled here to isolate request mapping, validation and serialization;
 * authorization rules are covered by the integration tests.
 */
@WebMvcTest(controllers = AppointmentController.class)
@AutoConfigureMockMvc(addFilters = false)
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AppointmentService appointmentService;

    private static AppointmentResponse sampleResponse(UUID id) {
        return new AppointmentResponse(
                id, UUID.randomUUID(), UUID.randomUUID(),
                LocalDate.now().plusDays(1), LocalTime.of(10, 0), LocalTime.of(10, 30),
                com.medsync.appointmentservice.domain.enums.AppointmentType.GENERAL,
                com.medsync.appointmentservice.domain.enums.AppointmentStatus.SCHEDULED,
                "Checkup", "Notes",
                java.time.Instant.now(), java.time.Instant.now()
        );
    }

    @Test
    @DisplayName("GET /api/v1/appointments returns 200 with a page of appointments")
    void getAllAppointmentsReturnsPage() throws Exception {
        UUID id = UUID.randomUUID();
        when(appointmentService.getAllAppointments(any())).thenReturn(new PageImpl<>(List.of(sampleResponse(id))));

        mockMvc.perform(get("/api/v1/appointments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(id.toString()));
    }

    @Test
    @DisplayName("GET /api/v1/appointments/{id} returns 200 when found")
    void getAppointmentByIdReturnsFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(appointmentService.getAppointmentById(id)).thenReturn(sampleResponse(id));

        mockMvc.perform(get("/api/v1/appointments/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    @DisplayName("GET /api/v1/appointments/{id} returns 404 when not found")
    void getAppointmentByIdReturns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(appointmentService.getAppointmentById(id)).thenThrow(new ResourceNotFoundException("Appointment not found with id: " + id));

        mockMvc.perform(get("/api/v1/appointments/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/appointments returns 201 with a Location header")
    void createsAppointment() throws Exception {
        UUID id = UUID.randomUUID();
        CreateAppointmentRequest request = new CreateAppointmentRequest(
                UUID.randomUUID(), UUID.randomUUID(),
                LocalDate.now().plusDays(1), LocalTime.of(10, 0), LocalTime.of(10, 30),
                com.medsync.appointmentservice.domain.enums.AppointmentType.GENERAL,
                "Checkup", "Notes"
        );
        when(appointmentService.createAppointment(any(CreateAppointmentRequest.class))).thenReturn(sampleResponse(id));

        mockMvc.perform(post("/api/v1/appointments")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/appointments/" + id));
    }

    @Test
    @DisplayName("POST /api/v1/appointments returns 400 when JSON is malformed")
    void rejectsMalformedJson() throws Exception {
        mockMvc.perform(post("/api/v1/appointments")
                        .contentType("application/json")
                        .content("{"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/appointments returns 400 when DTO is invalid")
    void rejectsInvalidDto() throws Exception {
        mockMvc.perform(post("/api/v1/appointments")
                        .contentType("application/json")
                        .content("""
                                {
                                  "patientId": null,
                                  "doctorId": null,
                                  "appointmentDate": null,
                                  "startTime": null,
                                  "endTime": null,
                                  "type": null,
                                  "reason": "x".repeat(501)
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Nested
    @DisplayName("PUT /api/v1/appointments/{id}")
    class UpdateAppointment {

        @Test
        @DisplayName("returns 200 with updated appointment")
        void updatesAppointment() throws Exception {
            UUID id = UUID.randomUUID();
            UpdateAppointmentRequest request = new UpdateAppointmentRequest(
                    LocalDate.now().plusDays(1), LocalTime.of(11, 0), LocalTime.of(11, 30),
                    com.medsync.appointmentservice.domain.enums.AppointmentType.FOLLOW_UP,
                    "Follow-up", "Updated notes"
            );
            when(appointmentService.updateAppointment(any(UpdateAppointmentRequest.class), any(UUID.class)))
                    .thenReturn(sampleResponse(id));

            mockMvc.perform(put("/api/v1/appointments/{id}", id)
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id.toString()));
        }

        @Test
        @DisplayName("returns 404 when appointment does not exist")
        void returns404WhenNotFound() throws Exception {
            UUID id = UUID.randomUUID();
            UpdateAppointmentRequest request = new UpdateAppointmentRequest(
                    LocalDate.now().plusDays(1), LocalTime.of(11, 0), LocalTime.of(11, 30),
                    com.medsync.appointmentservice.domain.enums.AppointmentType.FOLLOW_UP,
                    "Follow-up", "Updated notes"
            );
            when(appointmentService.updateAppointment(any(UpdateAppointmentRequest.class), any(UUID.class)))
                    .thenThrow(new ResourceNotFoundException("Appointment not found with id: " + id));

            mockMvc.perform(put("/api/v1/appointments/{id}", id)
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/appointments/status/{id}")
    class UpdateAppointmentStatus {

        @Test
        @DisplayName("returns 200 with updated status")
        void updatesStatus() throws Exception {
            UUID id = UUID.randomUUID();
            UpdateAppointmentStatusRequest request = new UpdateAppointmentStatusRequest(
                    com.medsync.appointmentservice.domain.enums.AppointmentStatus.CONFIRMED
            );
            when(appointmentService.updateAppointmentStatus(any(UpdateAppointmentStatusRequest.class), any(UUID.class)))
                    .thenReturn(sampleResponse(id));

            mockMvc.perform(patch("/api/v1/appointments/status/{id}", id)
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id.toString()));
        }

        @Test
        @DisplayName("returns 404 when appointment does not exist")
        void returns404WhenNotFound() throws Exception {
            UUID id = UUID.randomUUID();
            UpdateAppointmentStatusRequest request = new UpdateAppointmentStatusRequest(
                    com.medsync.appointmentservice.domain.enums.AppointmentStatus.CONFIRMED
            );
            when(appointmentService.updateAppointmentStatus(any(UpdateAppointmentStatusRequest.class), any(UUID.class)))
                    .thenThrow(new ResourceNotFoundException("Appointment not found with id: " + id));

            mockMvc.perform(patch("/api/v1/appointments/status/{id}", id)
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }
}