package com.medsync.doctorservice.service;

import com.medsync.commoncore.error.custom.ResourceNotFoundException;
import com.medsync.doctorservice.domain.converter.DeterministicHasher;
import com.medsync.doctorservice.domain.entity.Doctor;
import com.medsync.doctorservice.domain.enums.DoctorStatus;
import com.medsync.doctorservice.domain.enums.Specialty;
import com.medsync.doctorservice.dto.request.CreateDoctorRequest;
import com.medsync.doctorservice.dto.request.UpdateDoctorRequest;
import com.medsync.doctorservice.dto.response.DoctorResponse;
import com.medsync.doctorservice.mapper.DoctorMapper;
import com.medsync.doctorservice.repository.DoctorRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorServiceImplTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private DoctorMapper doctorMapper;

    @Mock
    private DoctorHashService doctorHashService;

    @Mock
    private DoctorUniquenessValidator doctorUniquenessValidator;

    @InjectMocks
    private DoctorServiceImpl doctorService;

    @BeforeAll
    static void initHasher() {
        DeterministicHasher.initialize("test-secret-key-for-jwt-tests-must-be-long-enough");
    }

    @Test
    void getAllDoctorShouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Doctor doctor = Doctor.create("Dr. House", "Gregory", Specialty.GENERAL_MEDICINE, "LIC-000", "house@medsync.com", "555123");
        DoctorResponse response = new DoctorResponse(UUID.randomUUID(), "Dr. House", "Gregory", Specialty.GENERAL_MEDICINE, "LIC-000", "house@medsync.com", "555123", DoctorStatus.ACTIVE);

        when(doctorRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(doctor)));
        when(doctorMapper.toResponse(doctor)).thenReturn(response);

        Page<DoctorResponse> result = doctorService.getAllDoctor(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getDoctorByIdShouldReturnResponse() {
        UUID id = UUID.randomUUID();
        Doctor doctor = Doctor.create("Dr. House", "Gregory", Specialty.GENERAL_MEDICINE, "LIC-000", "house@medsync.com", "555123");
        DoctorResponse response = new DoctorResponse(id, "Dr. House", "Gregory", Specialty.GENERAL_MEDICINE, "LIC-000", "house@medsync.com", "555123", DoctorStatus.ACTIVE);

        when(doctorRepository.findById(id)).thenReturn(Optional.of(doctor));
        when(doctorMapper.toResponse(doctor)).thenReturn(response);

        DoctorResponse result = doctorService.getDoctorById(id);

        assertNotNull(result);
        assertEquals("Dr. House", result.firstName());
    }

    @Test
    void getDoctorByIdShouldThrowNotFoundWhenMissing() {
        UUID id = UUID.randomUUID();
        when(doctorRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> doctorService.getDoctorById(id));
    }

    @Test
    void createDoctorShouldCreateAndReturnResponse() {
        CreateDoctorRequest request = new CreateDoctorRequest(
                "Gregory", "House", Specialty.GENERAL_MEDICINE, "LIC-000", "house@medsync.com", "555123"
        );
        DoctorHashService.DoctorHashes hashes = new DoctorHashService.DoctorHashes("h1", "h2", "h3");
        Doctor doctor = Doctor.create("Gregory", "House", Specialty.GENERAL_MEDICINE, "LIC-000", "house@medsync.com", "555123");
        DoctorResponse response = new DoctorResponse(UUID.randomUUID(), "Gregory", "House", Specialty.GENERAL_MEDICINE, "LIC-000", "house@medsync.com", "555123", DoctorStatus.ACTIVE);

        when(doctorHashService.fromCreateRequest(request)).thenReturn(hashes);
        doNothing().when(doctorUniquenessValidator).validateForCreate(hashes);
        when(doctorRepository.save(any(Doctor.class))).thenReturn(doctor);
        when(doctorMapper.toResponse(doctor)).thenReturn(response);

        DoctorResponse result = doctorService.createDoctor(request);

        assertNotNull(result);
        assertEquals("Gregory", result.firstName());
        verify(doctorUniquenessValidator).validateForCreate(hashes);
    }

    @Test
    void updateDoctorShouldUpdateAndReturnResponse() {
        UUID id = UUID.randomUUID();
        UpdateDoctorRequest request = new UpdateDoctorRequest("Gregory", "House", "house@medsync.com", "555123", DoctorStatus.ACTIVE);
        DoctorHashService.DoctorHashes hashes = new DoctorHashService.DoctorHashes("h1", null, "h3");
        Doctor doctor = Doctor.create("Gregory", "House", Specialty.GENERAL_MEDICINE, "LIC-000", "house@medsync.com", "555123");
        DoctorResponse response = new DoctorResponse(id, "Gregory", "House", Specialty.GENERAL_MEDICINE, "LIC-000", "house@medsync.com", "555123", DoctorStatus.ACTIVE);

        when(doctorRepository.findById(id)).thenReturn(Optional.of(doctor));
        when(doctorHashService.fromUpdateRequest(request)).thenReturn(hashes);
        doNothing().when(doctorUniquenessValidator).validateForUpdate(id, hashes);
        when(doctorRepository.save(doctor)).thenReturn(doctor);
        when(doctorMapper.toResponse(doctor)).thenReturn(response);

        DoctorResponse result = doctorService.updateDoctor(id, request);

        assertNotNull(result);
        assertEquals("Gregory", result.firstName());
        verify(doctorUniquenessValidator).validateForUpdate(id, hashes);
    }
}
