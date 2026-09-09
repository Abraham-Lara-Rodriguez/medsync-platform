package com.medsync.patientservice.repository;

import com.medsync.patientservice.domain.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {
    boolean existsByEmailHash(String emailHash);

    boolean existsByEmailHashAndIdNot(String emailHash, UUID id);

    boolean existsByDocumentNumberHash(String documentNumberHash);

    boolean existsByDocumentNumberHashAndIdNot(String documentNumberHash, UUID id);

    boolean existsByPhoneHash(String phoneHash);

    boolean existsByPhoneHashAndIdNot(String phoneHash, UUID id);
}