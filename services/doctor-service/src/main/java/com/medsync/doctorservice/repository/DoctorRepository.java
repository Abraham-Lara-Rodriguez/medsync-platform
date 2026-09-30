package com.medsync.doctorservice.repository;

import com.medsync.doctorservice.domain.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, UUID> {
    boolean existsByEmailHash(String emailHash);

    boolean existsByEmailHashAndIdNot(String emailHash, UUID id);

    boolean existsByMedicalLicenseHash(String medicalLicenseHash);

    boolean existsByMedicalLicenseHashAndIdNot(String medicalLicenseHash, UUID id);

    boolean existsByPhoneHash(String phoneHash);

    boolean existsByPhoneHashAndIdNot(String phoneHash, UUID id);
}
