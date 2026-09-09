package com.medsync.doctorservice.mapper;

import com.medsync.doctorservice.domain.entity.Doctor;
import com.medsync.doctorservice.dto.response.DoctorResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DoctorMapper {
    DoctorResponse toResponse(Doctor doctor);
}
