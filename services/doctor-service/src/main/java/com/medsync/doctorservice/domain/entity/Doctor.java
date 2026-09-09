package com.medsync.doctorservice.domain.entity;

import com.medsync.doctorservice.domain.constants.DoctorConstraints;
import com.medsync.doctorservice.domain.enums.DoctorStatus;
import com.medsync.doctorservice.domain.enums.Specialty;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import static com.medsync.doctorservice.domain.constants.DoctorConstraints.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "doctors")
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = MAX_NAME_LENGTH)
    private String firstName;

    @Column(nullable = false, length = MAX_NAME_LENGTH)
    private String lastName;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Specialty specialty;

    @Column(nullable = false, unique = true, length = MAX_MEDICAL_LICENSE_LENGTH)
    private String medicalLicense;

    @Column(nullable = false, unique = true, length = MAX_EMAIL_LENGTH)
    private String email;

    @Column(nullable = false, unique = true, length = MAX_PHONE_LENGTH)
    private String phone;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DoctorStatus status;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    // factory public method
    public static Doctor create(String firstName, String lastName, Specialty specialty,
                                String medicalLicense, String email, String phone) {
        Doctor doctor = new Doctor();
        doctor.changeFirstName(firstName);
        doctor.changeLastName(lastName);
        doctor.changeSpecialty(specialty);
        doctor.changeMedicalLicense(medicalLicense);
        doctor.changeEmail(email);
        doctor.changePhone(phone);
        doctor.changeStatus(DoctorStatus.ACTIVE);
        return doctor;
    }

    //------------------------//
    // domain update methods //
    //----------------------//
    public void changeFirstName(String firstName) {
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("First name cannot be null or empty");
        }
        String cleaned = normalizeName(firstName);
        if (cleaned.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("First name cannot exceed " + MAX_NAME_LENGTH + " characters");
        }
        this.firstName = cleaned;
    }

    public void changeLastName(String lastName) {
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Last name cannot be null or empty");
        }
        String cleaned = normalizeName(lastName);
        if (cleaned.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("Last name cannot exceed " + MAX_NAME_LENGTH + " characters");
        }
        this.lastName = cleaned;
    }

    public void changeSpecialty(Specialty specialty) {
        if (specialty == null) {
            throw new IllegalArgumentException("Specialty cannot be null");
        }
        this.specialty = specialty;
    }

    public void changeMedicalLicense(String medicalLicense) {
        if (medicalLicense == null || medicalLicense.trim().isEmpty()) {
            throw new IllegalArgumentException("Medical license cannot be null or empty");
        }
        String cleaned = normalizeMedicalLicense(medicalLicense);
        if (cleaned.length() > MAX_MEDICAL_LICENSE_LENGTH) {
            throw new IllegalArgumentException("Medical license cannot exceed " + MAX_MEDICAL_LICENSE_LENGTH + " characters");
        }
        if (!cleaned.matches(MEDICAL_LICENSE_REGEX)) {
            throw new IllegalArgumentException("Medical license contains invalid characters");
        }
        this.medicalLicense = cleaned;
    }

    public void changeEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        String cleaned = normalizeEmail(email);
        if (cleaned.length() > MAX_EMAIL_LENGTH) {
            throw new IllegalArgumentException("Email cannot exceed " + MAX_EMAIL_LENGTH + " characters");
        }
        if (!cleaned.matches(EMAIL_REGEX)) {
            throw new IllegalArgumentException("Email format is invalid");
        }
        this.email = cleaned;
    }

    public void changePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone cannot be null or empty");
        }
        String cleaned = normalizePhone(phone);
        if (cleaned.length() > MAX_PHONE_LENGTH) {
            throw new IllegalArgumentException("Phone cannot exceed " + MAX_PHONE_LENGTH + " characters");
        }
        if (!cleaned.matches(PHONE_REGEX)) {
            throw new IllegalArgumentException("Phone contains invalid characters");
        }
        this.phone = cleaned;
    }

    public void changeStatus(DoctorStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        this.status = status;
    }

    //------------------//
    // domain methods //
    //---------------//
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Doctor doctor = (Doctor) o;
        return id != null && Objects.equals(id, doctor.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
