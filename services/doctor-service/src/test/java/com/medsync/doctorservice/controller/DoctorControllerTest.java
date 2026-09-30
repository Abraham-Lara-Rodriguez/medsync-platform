package com.medsync.doctorservice.controller;

import com.medsync.commoncore.error.custom.ResourceNotFoundException;
import com.medsync.doctorservice.domain.enums.DoctorStatus;
import com.medsync.doctorservice.domain.enums.Specialty;
import com.medsync.doctorservice.dto.request.CreateDoctorRequest;
import com.medsync.doctorservice.dto.request.UpdateDoctorRequest;
import com.medsync.doctorservice.dto.response.DoctorResponse;
import com.medsync.doctorservice.exception.handler.GlobalExceptionHandler;
import com.medsync.doctorservice.service.DoctorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DoctorControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DoctorService doctorService;

    @InjectMocks
    private DoctorController doctorController;

    private UUID doctorId;
    private DoctorResponse doctorResponse;

    @BeforeEach
    void setUp() {
        doctorId = UUID.randomUUID();

        mockMvc = MockMvcBuilders
                .standaloneSetup(doctorController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        doctorResponse = sampleDoctorResponse(doctorId);
    }

    /* ================= GET ALL ================= */

    @Test
    @DisplayName("Should get all doctors")
    void getAllDoctor() throws Exception {
        Page<DoctorResponse> page = new PageImpl<>(List.of(doctorResponse), PageRequest.of(0, 10), 1);
        when(doctorService.getAllDoctor(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/doctors")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(doctorId.toString()))
                .andExpect(jsonPath("$.content[0].firstName").value("Gregory"));

        verify(doctorService).getAllDoctor(any(Pageable.class));
    }

    @Test
    @DisplayName("Should get all doctors returning empty page")
    void getAllDoctorShouldReturnEmptyPage() throws Exception {
        Page<DoctorResponse> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);
        when(doctorService.getAllDoctor(any(Pageable.class))).thenReturn(emptyPage);

        mockMvc.perform(get("/api/v1/doctors")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));

        verify(doctorService).getAllDoctor(any(Pageable.class));
    }

    /* ================= GET BY ID ================= */

    @Test
    @DisplayName("Should get doctor by id")
    void getDoctorById() throws Exception {
        when(doctorService.getDoctorById(doctorId)).thenReturn(doctorResponse);

        mockMvc.perform(get("/api/v1/doctors/{id}", doctorId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(doctorId.toString()))
                .andExpect(jsonPath("$.firstName").value("Gregory"))
                .andExpect(jsonPath("$.email").value("house@medsync.com"));

        verify(doctorService).getDoctorById(doctorId);
    }

    @Test
    @DisplayName("Should return not found when doctor missing")
    void getDoctorByIdShouldReturnNotFoundWhenMissing() throws Exception {
        when(doctorService.getDoctorById(doctorId))
                .thenThrow(new ResourceNotFoundException("Doctor not found"));

        mockMvc.perform(get("/api/v1/doctors/{id}", doctorId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(doctorService).getDoctorById(doctorId);
    }

    /* ================= CREATE ================= */

    @Test
    @DisplayName("Should create doctor")
    void createDoctor() throws Exception {
        when(doctorService.createDoctor(any(CreateDoctorRequest.class))).thenReturn(doctorResponse);

        mockMvc.perform(post("/api/v1/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createDoctorJson()))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(doctorId.toString()))
                .andExpect(jsonPath("$.firstName").value("Gregory"));

        verify(doctorService).createDoctor(any(CreateDoctorRequest.class));
    }

    @Test
    @DisplayName("Should fail when create json is invalid")
    void createDoctorShouldFailWhenInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidCreateDoctorJson()))
                .andExpect(status().isBadRequest());

        verify(doctorService, never()).createDoctor(any());
    }

    /* ================= UPDATE ================= */

    @Test
    @DisplayName("Should update doctor")
    void updateDoctor() throws Exception {
        when(doctorService.updateDoctor(eq(doctorId), any(UpdateDoctorRequest.class))).thenReturn(doctorResponse);

        mockMvc.perform(put("/api/v1/doctors/{id}", doctorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateDoctorJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(doctorId.toString()));

        verify(doctorService).updateDoctor(eq(doctorId), any(UpdateDoctorRequest.class));
    }

    @Test
    @DisplayName("Should fail when update json is invalid")
    void updateDoctorShouldFailWhenInvalid() throws Exception {
        mockMvc.perform(put("/api/v1/doctors/{id}", doctorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidUpdateDoctorJson()))
                .andExpect(status().isBadRequest());

        verify(doctorService, never()).updateDoctor(eq(doctorId), any(UpdateDoctorRequest.class));
    }

    /* ================= HELPERS ================= */

    private DoctorResponse sampleDoctorResponse(UUID id) {
        return new DoctorResponse(
                id,
                "Gregory",
                "House",
                Specialty.GENERAL_MEDICINE,
                "LIC-00001",
                "house@medsync.com",
                "+1555123456",
                DoctorStatus.ACTIVE
        );
    }

    private String createDoctorJson() {
        return """
                {
                  "firstName": "Gregory",
                  "lastName": "House",
                  "specialty": "GENERAL_MEDICINE",
                  "medicalLicense": "LIC-00001",
                  "email": "house@medsync.com",
                  "phone": "+1555123456"
                }
                """;
    }

    private String invalidCreateDoctorJson() {
        return """
                {
                  "firstName": "",
                  "lastName": "",
                  "specialty": null,
                  "medicalLicense": "",
                  "email": "invalid-email",
                  "phone": ""
                }
                """;
    }

    private String updateDoctorJson() {
        return """
                {
                  "firstName": "Gregory",
                  "lastName": "House",
                  "email": "house@medsync.com",
                  "phone": "+1555123456",
                  "status": "ACTIVE"
                }
                """;
    }

    private String invalidUpdateDoctorJson() {
        return """
                {
                  "firstName": "",
                  "lastName": "",
                  "email": "invalid-email",
                  "phone": "",
                  "status": null
                }
                """;
    }
}
