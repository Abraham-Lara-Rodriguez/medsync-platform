package com.medsync.doctorservice.mapper;

import com.medsync.doctorservice.domain.converter.DeterministicHasher;
import com.medsync.doctorservice.domain.entity.Doctor;
import com.medsync.doctorservice.domain.enums.Specialty;
import com.medsync.doctorservice.dto.response.DoctorResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DoctorMapperTest {

    private final DoctorMapper mapper = Mappers.getMapper(DoctorMapper.class);

    @BeforeAll
    static void initHasher() {
        DeterministicHasher.initialize("test-secret-key-for-jwt-tests-must-be-long-enough");
    }

    @Test
    void toResponseShouldMapFieldsCorrectly() {
        Doctor doctor = Doctor.create(
                "Alice", "Wonder", Specialty.NEUROLOGY, "LIC-11111",
                "alice@medsync.com", "+111222333"
        );

        DoctorResponse response = mapper.toResponse(doctor);

        assertNotNull(response);
        assertEquals("Alice", response.firstName());
        assertEquals("Wonder", response.lastName());
        assertEquals(Specialty.NEUROLOGY, response.specialty());
        assertEquals("alice@medsync.com", response.email());
    }
}
