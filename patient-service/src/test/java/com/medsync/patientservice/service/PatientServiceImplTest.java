package com.medsync.patientservice.service;

import com.medsync.commoncore.error.custom.DuplicateResourceException;
import com.medsync.commoncore.error.custom.ResourceNotFoundException;
import com.medsync.patientservice.domain.converter.DeterministicHasher;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class PatientServiceImplTest {

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

    private UUID patientId;
    private Patient patient;
    private PatientResponse response;
    private CreatePatientRequest createRequest;
    private UpdatePatientRequest updateRequest;
    private PatientHashService.PatientHashes hashes;
    
    @BeforeEach
    void setUp() {
        DeterministicHasher.initialize("test-hash-secret");
        patientId = UUID.randomUUID();
        patient = samplePatient();
        response = sampleResponse(patientId);
        createRequest = sampleCreateRequest();
        updateRequest = sampleUpdateRequest();
        hashes = new PatientHashService.PatientHashes("email-hash", "document-hash", "phone-hash");
    }

    @Test
    @DisplayName("Should get All Patients Should Return Mapped Page")
    void getAllPatientsShouldReturnMappedPage() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Patient> page = new PageImpl<>(List.of(patient), pageable, 1);

        when(patientRepository.findAll(pageable)).thenReturn(page);
        when(patientMapper.toResponse(patient)).thenReturn(response);

        Page<PatientResponse> result = patientService.getAllPatients(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(response, result.getContent().get(0));
        verify(patientRepository).findAll(pageable);
        verify(patientMapper).toResponse(patient);
    }

    @Test
    @DisplayName("Should get All Patients Should Allow Maximum Page Size")
    void getAllPatientsShouldAllowMaximumPageSize() {
        Pageable pageable = PageRequest.of(0, 100);
        when(patientRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of()));

        assertDoesNotThrow(() -> patientService.getAllPatients(pageable));
        verify(patientRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Should get All Patients Should Reject Page Size Above Maximum")
    void getAllPatientsShouldRejectPageSizeAboveMaximum() {
        Pageable pageable = PageRequest.of(0, 101);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> patientService.getAllPatients(pageable)
        );

        assertEquals("Page size must not exceed 100", ex.getMessage());
        verify(patientRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("Should get Patient By Id Should Return Mapped Patient")
    void getPatientByIdShouldReturnMappedPatient() {
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(patientMapper.toResponse(patient)).thenReturn(response);

        assertEquals(response, patientService.getPatientById(patientId));
    }

    @Test
    @DisplayName("Should get Patient By Id Should Throw When Missing")
    void getPatientByIdShouldThrowWhenMissing() {
        when(patientRepository.findById(patientId)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> patientService.getPatientById(patientId)
        );

        assertEquals("Patient not found with id: " + patientId, ex.getMessage());
        verify(patientMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("Should create Patient Should Hash Validate Save And Map")
    void createPatientShouldHashValidateSaveAndMap() {
        when(patientHashService.fromCreateRequest(createRequest)).thenReturn(hashes);
        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(patientMapper.toResponse(any(Patient.class))).thenReturn(response);

        PatientResponse result = patientService.createPatient(createRequest);

        assertEquals(response, result);
        verify(patientHashService).fromCreateRequest(createRequest);
        verify(patientUniquenessValidator).validateForCreate(hashes);
        verify(patientRepository).save(any(Patient.class));
        verify(patientMapper).toResponse(any(Patient.class));
    }

    @Test
    @DisplayName("Should create Patient Should Not Save When Duplicate Exists")
    void createPatientShouldNotSaveWhenDuplicateExists() {
        when(patientHashService.fromCreateRequest(createRequest)).thenReturn(hashes);
        doThrow(new DuplicateResourceException("Email already exists"))
                .when(patientUniquenessValidator).validateForCreate(hashes);

        assertThrows(DuplicateResourceException.class, () -> patientService.createPatient(createRequest));

        verify(patientRepository, never()).save(any());
        verify(patientMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("Should update Patient Should Change All Mutable Fields")
    void updatePatientShouldChangeAllMutableFields() {
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(patientHashService.fromUpdateRequest(updateRequest)).thenReturn(hashes);
        when(patientRepository.save(patient)).thenReturn(patient);
        when(patientMapper.toResponse(patient)).thenReturn(response);

        PatientResponse result = patientService.updatePatient(patientId, updateRequest);

        assertEquals(response, result);
        assertAll(
                () -> assertEquals("Juan Carlos", patient.getFirstName()),
                () -> assertEquals("Pérez López", patient.getLastName()),
                () -> assertEquals("87654321", patient.getDocumentNumber()),
                () -> assertEquals(Gender.MALE, patient.getGender()),
                () -> assertEquals(LocalDate.of(1990, 1, 15), patient.getBirthDate()),
                () -> assertEquals("+573007654321", patient.getPhone()),
                () -> assertEquals("juancarlos@example.com", patient.getEmail()),
                () -> assertEquals("Calle 456", patient.getAddress()),
                () -> assertEquals(BloodType.A_POSITIVE, patient.getBloodType())
        );
        verify(patientUniquenessValidator).validateForUpdate(patientId, hashes);
        verify(patientRepository).save(patient);
    }

    @Test
    @DisplayName("Should update Patient Should Throw When Patient Does Not Exist")
    void updatePatientShouldThrowWhenPatientDoesNotExist() {
        when(patientRepository.findById(patientId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> patientService.updatePatient(patientId, updateRequest));

        verify(patientHashService, never()).fromUpdateRequest(any());
        verify(patientRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update Patient Should Not Save When Duplicate Email Exists")
    void updatePatientShouldNotSaveWhenDuplicateEmailExists() {
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(patientHashService.fromUpdateRequest(updateRequest)).thenReturn(hashes);
        doThrow(new DuplicateResourceException("Email already registered"))
                .when(patientUniquenessValidator).validateForUpdate(patientId, hashes);

        assertThrows(DuplicateResourceException.class,
                () -> patientService.updatePatient(patientId, updateRequest));

        verify(patientRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should deactivate Patient Should Set Inactive And Save")
    void deactivatePatientShouldSetInactiveAndSave() {
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(patientRepository.save(patient)).thenReturn(patient);

        patientService.deactivatePatient(patientId);

        assertEquals(PatientStatus.INACTIVE, patient.getStatus());
        verify(patientRepository).save(patient);
    }

    @Test
    @DisplayName("Should deactivate Patient Should Throw When Missing")
    void deactivatePatientShouldThrowWhenMissing() {
        when(patientRepository.findById(patientId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> patientService.deactivatePatient(patientId));

        verify(patientRepository, never()).save(any());
    }

    private Patient samplePatient() {
        return Patient.create(
                "Juan", "Pérez", "12345678", Gender.MALE,
                LocalDate.of(1990, 1, 15), "+573001234567",
                "juan@example.com", "Calle 123", BloodType.O_POSITIVE
        );
    }

    private PatientResponse sampleResponse(UUID id) {
        return new PatientResponse(
                id, "Juan", "Pérez", "12345678", Gender.MALE,
                LocalDate.of(1990, 1, 15), "+573001234567",
                "juan@example.com", "Calle 123",
                BloodType.O_POSITIVE, PatientStatus.ACTIVE
        );
    }

    private CreatePatientRequest sampleCreateRequest() {
        return new CreatePatientRequest(
                "Juan", "Pérez", "12345678", Gender.MALE,
                LocalDate.of(1990, 1, 15), "+573001234567",
                "juan@example.com", "Calle 123", BloodType.O_POSITIVE
        );
    }

    private UpdatePatientRequest sampleUpdateRequest() {
        return new UpdatePatientRequest(
                "Juan Carlos", "Pérez López", "87654321", Gender.MALE,
                LocalDate.of(1990, 1, 15), "+573007654321",
                "juancarlos@example.com", "Calle 456", BloodType.A_POSITIVE
        );
    }
}
