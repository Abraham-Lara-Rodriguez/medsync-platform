package com.medsync.patientservice.mapper;

import com.medsync.patientservice.domain.entity.Patient;
import com.medsync.patientservice.domain.enums.BloodType;
import com.medsync.patientservice.domain.enums.Gender;
import com.medsync.patientservice.dto.response.PatientResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PatientMapperTest {

    private final PatientMapper patientMapper = new PatientMapperImpl();

    @Test
    void toResponse() {
        Patient entity = Patient.create(
                "Gracie",
                "Conn",
                "0000004857",
                Gender.MALE,
                LocalDate.parse("2026-06-02"),
                "0000003838",
                "candelario.morar32@hotmail.com",
                "96487 S Washington Avenue",
                BloodType.B_NEGATIVE
        );

        PatientResponse response = patientMapper.toResponse(entity);
        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(entity.getFirstName(), response.firstName()),
                () -> assertEquals(entity.getLastName(), response.lastName()),
                () -> assertEquals(entity.getDocumentNumber(), response.documentNumber()),
                () -> assertEquals(entity.getGender(), response.gender()),
                () -> assertEquals(entity.getBirthDate(), response.birthDate()),
                () -> assertEquals(entity.getPhone(), response.phone()),
                () -> assertEquals(entity.getEmail(), response.email()),
                () -> assertEquals(entity.getAddress(), response.address()),
                () -> assertEquals(entity.getBloodType(), response.bloodType()),
                () -> assertEquals(entity.getStatus(), response.status())
        );
    }

    @Test
    void toResponseWithNull() {
        assertNull(patientMapper.toResponse(null));
    }
}