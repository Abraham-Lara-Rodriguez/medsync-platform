package com.medsync.patientservice.controller;

import com.medsync.commoncore.error.custom.ResourceNotFoundException;
import com.medsync.patientservice.domain.enums.BloodType;
import com.medsync.patientservice.domain.enums.Gender;
import com.medsync.patientservice.dto.request.CreatePatientRequest;
import com.medsync.patientservice.dto.request.UpdatePatientRequest;
import com.medsync.patientservice.dto.response.PatientResponse;
import com.medsync.patientservice.exception.handler.GlobalExceptionHandler;
import com.medsync.patientservice.service.PatientService;
import org.junit.jupiter.api.BeforeEach;
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

import java.time.LocalDate;
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
class PatientControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PatientService patientService;

    @InjectMocks
    private PatientController patientController;

    private UUID patientId;
    private PatientResponse patientResponse;
    private UpdatePatientRequest updateRequest;

    @BeforeEach
    void setUp() {
        patientId = UUID.randomUUID();

        mockMvc = MockMvcBuilders
                .standaloneSetup(patientController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        patientResponse = samplePatientResponse(patientId);
        updateRequest = sampleUpdateRequest();
    }

    /* ================= GET ALL ================= */

    @Test
    void getAllPatients() throws Exception {
        Page<PatientResponse> page = new PageImpl<>(List.of(patientResponse), PageRequest.of(0, 10), 1);
        when(patientService.getAllPatients(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/patients")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(patientId.toString()))
                .andExpect(jsonPath("$.content[0].firstName").value("Juan"));

        verify(patientService).getAllPatients(any(Pageable.class));
    }

    @Test
    void getAllPatientsShouldReturnEmptyPage() throws Exception {
        Page<PatientResponse> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);
        when(patientService.getAllPatients(any(Pageable.class))).thenReturn(emptyPage);

        mockMvc.perform(get("/api/v1/patients")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));

        verify(patientService).getAllPatients(any(Pageable.class));
    }

    /* ================= GET BY ID ================= */

    @Test
    void getPatientById() throws Exception {
        when(patientService.getPatientById(patientId)).thenReturn(patientResponse);

        mockMvc.perform(get("/api/v1/patients/{id}", patientId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(patientId.toString()))
                .andExpect(jsonPath("$.firstName").value("Juan"))
                .andExpect(jsonPath("$.email").value("juan@example.com"));

        verify(patientService).getPatientById(patientId);
    }

    @Test
    void getPatientByIdShouldReturnNotFoundWhenMissing() throws Exception {
        when(patientService.getPatientById(patientId))
                .thenThrow(new ResourceNotFoundException("Patient not found"));

        mockMvc.perform(get("/api/v1/patients/{id}", patientId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(patientService).getPatientById(patientId);
    }

    /* ================= CREATE ================= */

    @Test
    void createPatient() throws Exception {
        when(patientService.createPatient(any(CreatePatientRequest.class))).thenReturn(patientResponse);

        mockMvc.perform(post("/api/v1/patients")
                        .contentType("application/json")
                        .content(createPatientJson()))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(patientId.toString()))
                .andExpect(jsonPath("$.firstName").value("Juan"));

        verify(patientService).createPatient(any(CreatePatientRequest.class));
    }

    @Test
    void createPatientShouldFailWhenInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/patients")
                        .contentType("application/json")
                        .content("{"))
                .andExpect(status().isBadRequest());

        verify(patientService, never()).createPatient(any());
    }

    @Test
    void createPatientShouldReturnBadRequestWhenDtoIsInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidCreatePatientJson()))
                .andExpect(status().isBadRequest());

        verify(patientService, never()).createPatient(any());
    }

    /* ================= UPDATE ================= */

    @Test
    void updatePatient() throws Exception {
        when(patientService.updatePatient(eq(patientId), any(UpdatePatientRequest.class)))
                .thenReturn(samplePatientResponse(patientId, updateRequest));

        mockMvc.perform(put("/api/v1/patients/{id}", patientId)
                        .contentType("application/json")
                        .content(updatePatientJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(patientId.toString()))
                .andExpect(jsonPath("$.firstName").value("Juan Carlos"));

        verify(patientService).updatePatient(eq(patientId), any(UpdatePatientRequest.class));
    }

    @Test
    void updatePatientShouldReturnNotFoundWhenMissing() throws Exception {
        when(patientService.updatePatient(eq(patientId), any(UpdatePatientRequest.class)))
                .thenThrow(new ResourceNotFoundException("Patient not found"));

        mockMvc.perform(put("/api/v1/patients/{id}", patientId)
                        .contentType("application/json")
                        .content(updatePatientJson()))
                .andExpect(status().isNotFound());

        verify(patientService).updatePatient(eq(patientId), any(UpdatePatientRequest.class));
    }

    @Test
    void updatePatientShouldReturnBadRequestWhenDtoIsInvalid() throws Exception {
        mockMvc.perform(put("/api/v1/patients/{id}", patientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidUpdatePatientJson()))
                .andExpect(status().isBadRequest());

        verify(patientService, never()).updatePatient(eq(patientId), any(UpdatePatientRequest.class));
    }

    /* ================= DEACTIVATE ================= */

    @Test
    void deactivatePatient() throws Exception {
        doNothing().when(patientService).deactivatePatient(patientId);

        mockMvc.perform(patch("/api/v1/patients/deactivate/{id}", patientId))
                .andExpect(status().isNoContent());

        verify(patientService).deactivatePatient(patientId);
    }

    @Test
    void deactivatePatientShouldReturnNotFoundWhenMissing() throws Exception {
        doThrow(new ResourceNotFoundException("Patient not found"))
                .when(patientService).deactivatePatient(patientId);

        mockMvc.perform(patch("/api/v1/patients/deactivate/{id}", patientId))
                .andExpect(status().isNotFound());

        verify(patientService).deactivatePatient(patientId);
    }


    /* ================= HELPERS ================= */

    private PatientResponse samplePatientResponse(UUID id) {
        return new PatientResponse(
                id,
                "Juan",
                "Pérez",
                "12345678",
                Gender.MALE,
                LocalDate.of(1990, 1, 15),
                "+573001234567",
                "juan@example.com",
                "Calle 123",
                BloodType.O_POSITIVE,
                com.medsync.patientservice.domain.enums.PatientStatus.ACTIVE
        );
    }

    private PatientResponse samplePatientResponse(UUID id, UpdatePatientRequest request) {
        return new PatientResponse(
                id,
                request.firstName(),
                request.lastName(),
                request.documentNumber(),
                request.gender(),
                request.birthDate(),
                request.phone(),
                request.email(),
                request.address(),
                request.bloodType(),
                com.medsync.patientservice.domain.enums.PatientStatus.ACTIVE
        );
    }

    private UpdatePatientRequest sampleUpdateRequest() {
        return new UpdatePatientRequest(
                "Juan Carlos",
                "Pérez López",
                "87654321",
                Gender.MALE,
                LocalDate.of(1990, 1, 15),
                "+573007654321",
                "juancarlos@example.com",
                "Calle 456",
                BloodType.A_POSITIVE
        );
    }

    private String createPatientJson() {
        return """
                {
                  "firstName": "Juan",
                  "lastName": "Pérez",
                  "documentNumber": "12345678",
                  "gender": "MALE",
                  "birthDate": "1990-01-15",
                  "phone": "+573001234567",
                  "email": "juan@example.com",
                  "address": "Calle 123",
                  "bloodType": "O_POSITIVE"
                }
                """;
    }

    private String invalidCreatePatientJson() {
        return """
                {
                  "firstName": "",
                  "lastName": "",
                  "documentNumber": "",
                  "gender": null,
                  "birthDate": null,
                  "phone": "",
                  "email": "invalid-email",
                  "address": "a",
                  "bloodType": null
                }
                """;
    }

    private String updatePatientJson() {
        return """
                {
                  "firstName": "Juan Carlos",
                  "lastName": "Pérez López",
                  "documentNumber": "87654321",
                  "gender": "MALE",
                  "birthDate": "1990-01-15",
                  "phone": "+573007654321",
                  "email": "juancarlos@example.com",
                  "address": "Calle 456",
                  "bloodType": "A_POSITIVE",
                  "status": "ACTIVE"
                }
                """;
    }

    private String invalidUpdatePatientJson() {
        return """
                {
                  "firstName": "",
                  "lastName": "",
                  "documentNumber": "",
                  "gender": null,
                  "birthDate": null,
                  "phone": "",
                  "email": "invalid-email",
                  "address": "a",
                  "bloodType": null
                }
                """;
    }
}