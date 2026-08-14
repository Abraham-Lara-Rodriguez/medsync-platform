package com.medsync.patientservice.mapper;

import com.medsync.patientservice.domain.entity.Patient;
import com.medsync.patientservice.dto.response.PatientResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PatientMapper {
    PatientResponse toResponse(Patient patient);
}
