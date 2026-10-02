package com.medsync.doctorservice.integration;

import com.medsync.commoncore.error.custom.ResourceNotFoundException;
import com.medsync.doctorservice.controller.DoctorController;
import com.medsync.doctorservice.dto.request.CreateDoctorRequest;
import com.medsync.doctorservice.dto.response.DoctorResponse;
import com.medsync.doctorservice.service.DoctorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class DoctorControllerIntegrationTest {

    private MockMvc mockMvc;
    private DoctorService doctorService;
    private UUID doctorId;

    @BeforeEach
    void setUp() {
        doctorService = org.mockito.Mockito.mock(DoctorService.class);
        DoctorController controller = new DoctorController(doctorService);
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new com.medsync.doctorservice.exception.handler.GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
        doctorId = UUID.randomUUID();
    }

    @Test
    @DisplayName("GET /api/v1/doctors returns paginated list")
    void getDoctorsPaginated() throws Exception {
        DoctorResponse response = new DoctorResponse(doctorId, "Dr. House", "Gregory",
                com.medsync.doctorservice.domain.enums.Specialty.CARDIOLOGY,
                "LIC-001", "house@test.com", "+555001",
                com.medsync.doctorservice.domain.enums.DoctorStatus.ACTIVE);
        Page<DoctorResponse> page = new PageImpl<>(Collections.singletonList(response), PageRequest.of(0, 10), 1);
        when(doctorService.getAllDoctor(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/doctors")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(doctorId.toString()));

        verify(doctorService).getAllDoctor(any(Pageable.class));
    }

    @Test
    @DisplayName("GET /api/v1/doctors/{id} returns 404 for missing doctor")
    void getDoctorNotFound() throws Exception {
        UUID missingId = UUID.randomUUID();
        when(doctorService.getDoctorById(missingId))
                .thenThrow(new ResourceNotFoundException("Doctor not found"));

        mockMvc.perform(get("/api/v1/doctors/{id}", missingId))
                .andExpect(status().isNotFound());

        verify(doctorService).getDoctorById(missingId);
    }

    @Test
    @DisplayName("POST /api/v1/doctors creates new doctor")
    void createDoctor() throws Exception {
        DoctorResponse response = new DoctorResponse(doctorId, "Dr. Smith", "Smith",
                com.medsync.doctorservice.domain.enums.Specialty.GENERAL_MEDICINE,
                "LIC-999", "smith@test.com", "+555999",
                com.medsync.doctorservice.domain.enums.DoctorStatus.ACTIVE);
        when(doctorService.createDoctor(any(CreateDoctorRequest.class))).thenReturn(response);

        String json = """
                {"firstName":"Dr","lastName":"Smith","specialty":"GENERAL_MEDICINE","medicalLicense":"LIC-999","email":"smith@test.com","phone":"+555999"}
                """;
        mockMvc.perform(post("/api/v1/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(doctorId.toString()));

        verify(doctorService).createDoctor(any(CreateDoctorRequest.class));
    }
}