package com.medsync.patientservice.service;

import com.medsync.commoncore.error.custom.DuplicateResourceException;
import com.medsync.commoncore.error.custom.ResourceNotFoundException;
import com.medsync.patientservice.domain.entity.Patient;
import com.medsync.patientservice.domain.enums.BloodType;
import com.medsync.patientservice.domain.enums.Gender;
import com.medsync.patientservice.domain.enums.PatientStatus;
import com.medsync.patientservice.dto.request.CreatePatientRequest;
import com.medsync.patientservice.dto.request.UpdatePatientRequest;
import com.medsync.patientservice.dto.response.PatientResponse;
import com.medsync.patientservice.mapper.PatientMapper;
import com.medsync.patientservice.repository.PatientRepository;
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

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceImplTest {

    /* ================= MOCKS ================= */

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PatientMapper patientMapper;

    @Mock
    private PatientHashService patientHashService;

    @Mock
    private PatientUniquenessValidator patientUniquenessValidator;

    @InjectMocks
    private PatientServiceImpl patientService;

    /* ================= FIXTURES ================= */

    private UUID patientId;
    private Patient patient;
    private PatientResponse patientResponse;
    private CreatePatientRequest createRequest;
    private UpdatePatientRequest updateRequest;

    @BeforeEach
    void setUp() {
        patientId = UUID.randomUUID();

        patient = samplePatient();
        patientResponse = samplePatientResponse(patientId);
        createRequest = sampleCreateRequest();
        updateRequest = sampleUpdateRequest();
    }

    /* ================= GET ALL ================= */

    @Test
    void getAllPatients() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Patient> patientPage = new PageImpl<>(List.of(patient), pageable, 1);

        when(patientRepository.findAll(pageable)).thenReturn(patientPage);
        when(patientMapper.toResponse(patient)).thenReturn(patientResponse);

        Page<PatientResponse> result = patientService.getAllPatients(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(patientResponse, result.getContent().get(0));
        verify(patientRepository).findAll(pageable);
        verify(patientMapper).toResponse(patient);
    }

    @Test
    void getAllPatientsShouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Patient> emptyPage = new PageImpl<>(Collections.emptyList());

        when(patientRepository.findAll(pageable)).thenReturn(emptyPage);

        Page<PatientResponse> result = patientService.getAllPatients(pageable);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(patientRepository).findAll(pageable);
    }

    @Test
    void getAllPatientsShouldFailWhenPageSizeExceedsMaximum() {
        Pageable pageable = PageRequest.of(0, 101);

        assertThrows(IllegalArgumentException.class, () -> patientService.getAllPatients(pageable));
        verify(patientRepository, never()).findAll(any(Pageable.class));
    }

    /* ================= GET BY ID ================= */

    @Test
    void getPatientById() {
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(patientMapper.toResponse(patient)).thenReturn(patientResponse);

        PatientResponse result = patientService.getPatientById(patientId);

        assertNotNull(result);
        assertEquals(patientResponse, result);
        verify(patientRepository).findById(patientId);
        verify(patientMapper).toResponse(patient);
    }

    @Test
    void getPatientByIdShouldFailWhenMissing() {
        when(patientRepository.findById(patientId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> patientService.getPatientById(patientId));
        verify(patientRepository).findById(patientId);
        verify(patientMapper, never()).toResponse(any());
    }

    /* ================= CREATE ================= */

    @Test
    void createPatient() {
        Patient createdPatient = Patient.create(
                createRequest.firstName(),
                createRequest.lastName(),
                createRequest.documentNumber(),
                createRequest.gender(),
                createRequest.birthDate(),
                createRequest.phone(),
                createRequest.email(),
                createRequest.address(),
                createRequest.bloodType()
        );

        when(patientHashService.fromCreateRequest(createRequest)).thenReturn(sampleHashes());
        doNothing().when(patientUniquenessValidator).validateForCreate(any());
        when(patientRepository.save(any(Patient.class))).thenReturn(createdPatient);
        when(patientMapper.toResponse(createdPatient)).thenReturn(patientResponse);

        PatientResponse result = patientService.createPatient(createRequest);

        assertNotNull(result);
        assertEquals(patientResponse.firstName(), result.firstName());
        assertEquals(patientResponse.email(), result.email());
        verify(patientRepository).save(any(Patient.class));
        verify(patientMapper).toResponse(any(Patient.class));
    }

    @Test
    void createPatientShouldFailWhenEmailAlreadyExists() {
        when(patientHashService.fromCreateRequest(createRequest)).thenReturn(sampleHashes());
        doThrow(new DuplicateResourceException("Email already exists"))
                .when(patientUniquenessValidator).validateForCreate(any());

        assertThrows(DuplicateResourceException.class, () -> patientService.createPatient(createRequest));

        verify(patientHashService).fromCreateRequest(createRequest);
        verify(patientUniquenessValidator).validateForCreate(any());
        verify(patientRepository, never()).save(any(Patient.class));
    }

    /* ================= UPDATE ================= */

    @Test
    void updatePatient() {
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(patientHashService.fromUpdateRequest(updateRequest)).thenReturn(sampleHashes());
        doNothing().when(patientUniquenessValidator).validateForUpdate(eq(patientId), any());
        when(patientRepository.save(any(Patient.class))).thenReturn(patient);
        PatientResponse updatedResponse = samplePatientResponse(patientId, updateRequest);
        when(patientMapper.toResponse(patient)).thenReturn(updatedResponse);
        PatientResponse result = patientService.updatePatient(patientId, updateRequest);
        assertNotNull(result);
        assertEquals(updateRequest.firstName(), result.firstName());
        assertEquals(updateRequest.email(), result.email());
        verify(patientRepository).findById(patientId);
        verify(patientRepository).save(any(Patient.class));
        verify(patientMapper).toResponse(patient);
    }

    @Test
    void updatePatientShouldFailWhenMissing() {
        when(patientRepository.findById(patientId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> patientService.updatePatient(patientId, updateRequest));
        verify(patientRepository).findById(patientId);
    }

    @Test
    void updatePatientShouldFailWhenDocumentNumberAlreadyExists() {
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(patientHashService.fromUpdateRequest(updateRequest)).thenReturn(sampleHashes());
        doThrow(new DuplicateResourceException("Document number already exists"))
                .when(patientUniquenessValidator).validateForUpdate(eq(patientId), any());

        assertThrows(DuplicateResourceException.class, () -> patientService.updatePatient(patientId, updateRequest));

        verify(patientRepository).findById(patientId);
        verify(patientHashService).fromUpdateRequest(updateRequest);
        verify(patientUniquenessValidator).validateForUpdate(eq(patientId), any());
        verify(patientRepository, never()).save(any(Patient.class));
    }

    /* ================= DEACTIVATE ================= */

    @Test
    void deactivatePatient() {
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> invocation.getArgument(0));

        patientService.deactivatePatient(patientId);

        verify(patientRepository).findById(patientId);
        verify(patientRepository).save(any(Patient.class));
        assertEquals(PatientStatus.INACTIVE, patient.getStatus());
    }

    @Test
    void deactivatePatientShouldFailWhenMissing() {
        when(patientRepository.findById(patientId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> patientService.deactivatePatient(patientId));

        verify(patientRepository).findById(patientId);
        verify(patientRepository, never()).save(any(Patient.class));
    }


    /* ================= HELPERS ================= */

    private Patient samplePatient() {
        return Patient.create(
                "Juan",
                "Pérez",
                "12345678",
                Gender.MALE,
                LocalDate.of(1990, 1, 15),
                "+573001234567",
                "juan@example.com",
                "Calle 123",
                BloodType.O_POSITIVE
        );
    }

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

    private CreatePatientRequest sampleCreateRequest() {
        return new CreatePatientRequest(
                "Juan",
                "Pérez",
                "12345678",
                Gender.MALE,
                LocalDate.of(1990, 1, 15),
                "+573001234567",
                "juan@example.com",
                "Calle 123",
                BloodType.O_POSITIVE
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

    private PatientHashService.PatientHashes sampleHashes() {
        return new PatientHashService.PatientHashes("email-hash", "document-hash", "phone-hash");
    }
}