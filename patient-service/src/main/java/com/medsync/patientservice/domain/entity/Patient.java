package com.medsync.patientservice.domain.entity;

import com.medsync.patientservice.domain.constants.PatientConstraints;
import com.medsync.patientservice.domain.converter.DeterministicHasher;
import com.medsync.patientservice.domain.converter.EncryptedStringConverter;
import com.medsync.patientservice.domain.enums.BloodType;
import com.medsync.patientservice.domain.enums.Gender;
import com.medsync.patientservice.domain.enums.PatientStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Objects;
import java.util.UUID;

import static com.medsync.patientservice.domain.constants.PatientConstraints.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "patients")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = MAX_NAME_LENGTH)
    private String firstName;

    @Column(nullable = false, length = MAX_NAME_LENGTH)
    private String lastName;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Column(nullable = false)
    private LocalDate birthDate;

    @Column(columnDefinition = "TEXT", nullable = false)
    @Convert(converter = EncryptedStringConverter.class)
    private String documentNumber;

    @Column(columnDefinition = "TEXT", nullable = false)
    @Convert(converter = EncryptedStringConverter.class)
    private String phone;

    @Column(columnDefinition = "TEXT", nullable = false)
    @Convert(converter = EncryptedStringConverter.class)
    private String email;

    @Column(name = "document_number_hash", nullable = false, unique = true, length = 64)
    private String documentNumberHash;

    @Column(name = "phone_hash", nullable = false, unique = true, length = 64)
    private String phoneHash;

    @Column(name = "email_hash", nullable = false, unique = true, length = 64)
    private String emailHash;

    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BloodType bloodType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PatientStatus status;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    // Factory method público
    public static Patient create(String firstName, String lastName, String documentNumber, Gender gender,
                                 LocalDate birthDate, String phone, String email, String address, BloodType bloodType) {

        Patient patient = new Patient();
        patient.changeFirstName(firstName);
        patient.changeLastName(lastName);
        patient.changeDocumentNumber(documentNumber);
        patient.changeGender(gender);
        patient.changeBirthDate(birthDate);
        patient.changePhone(phone);
        patient.changeEmail(email);
        patient.changeAddress(address);
        patient.changeBloodType(bloodType);
        patient.changeStatus(PatientStatus.ACTIVE);  // default status
        return patient;
    }

    //------------------------//
    // domain update methods //
    //----------------------//
    public void changeFirstName(String firstName) {
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("First name cannot be null or empty");
        }
        if (firstName.trim().length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("First name cannot exceed 80 characters");
        }
        this.firstName = firstName.trim();
    }

    public void changeLastName(String lastName) {
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Last name cannot be null or empty");
        }
        if (lastName.trim().length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("Last name cannot exceed 80 characters");
        }
        this.lastName = lastName.trim();
    }

    public void changeDocumentNumber(String documentNumber) {
        if (documentNumber == null || documentNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Document number cannot be null or empty");
        }
        String cleanedDoc = normalizeDocument(documentNumber);
        if (cleanedDoc.length() > MAX_DOCUMENT_LENGTH) {
            throw new IllegalArgumentException("Document number cannot exceed 20 characters");
        }
        if (!isValidDocument(cleanedDoc)) {
            throw new IllegalArgumentException("Document number format is invalid. Expected: DNI (8 digits), Cédula (10 digits), or Passport (6-9 alphanumeric)");
        }
        this.documentNumber = cleanedDoc;
        this.documentNumberHash = DeterministicHasher.hash(cleanedDoc);
    }

    public void changeGender(Gender gender) {
        if (gender == null) {
            throw new IllegalArgumentException("Gender cannot be null");
        }
        this.gender = gender;
    }

    public void changeBirthDate(LocalDate birthDate) {
        if (birthDate == null) {
            throw new IllegalArgumentException("Birth date cannot be null");
        }
        if (birthDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Birth date cannot be in the future");
        }
        if (Period.between(birthDate, LocalDate.now()).getYears() > MAX_HUMAN_LIFETIME_YEARS) {
            throw new IllegalArgumentException("Birth date is not valid");
        }
        this.birthDate = birthDate;
    }

    public void changePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone cannot be null or empty");
        }
        String cleanedPhone = normalizePhone(phone);
        if (cleanedPhone.length() > MAX_PHONE_LENGTH) {
            throw new IllegalArgumentException("Phone cannot exceed 20 characters");
        }
        if (!cleanedPhone.matches(PHONE_REGEX)) {
            throw new IllegalArgumentException("Phone contains invalid characters");
        }
        this.phone = cleanedPhone;
        this.phoneHash = DeterministicHasher.hash(cleanedPhone);
    }

    public void changeEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        String cleanedEmail = normalizeEmail(email);
        if (cleanedEmail.length() > MAX_EMAIL_LENGTH) {
            throw new IllegalArgumentException("Email cannot exceed 120 characters");
        }
        if (!cleanedEmail.matches(EMAIL_REGEX)) {
            throw new IllegalArgumentException("Email format is invalid");
        }
        this.email = cleanedEmail;
        this.emailHash = DeterministicHasher.hash(cleanedEmail);
    }

    public void changeAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            this.address = null;
            return;
        }
        String cleanedAddress = address.trim();
        if (cleanedAddress.length() > MAX_ADDRESS_LENGTH) {
            throw new IllegalArgumentException("Address cannot exceed 255 characters");
        }
        this.address = cleanedAddress;
    }

    public void changeBloodType(BloodType bloodType) {
        if (bloodType == null) {
            throw new IllegalArgumentException("Blood type cannot be null");
        }
        this.bloodType = bloodType;
    }

    public void changeStatus(PatientStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        this.status = status;
    }

    //------------------//
    // domain methods //
    //---------------//
    private boolean isValidDocument(String document) {
        return document.matches("^\\d{8}$")      // DNI
                || document.matches("^\\d{10}$")     // Cédula
                || document.matches(PatientConstraints.PASSPORT_REGEX); // Passport
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Patient patient = (Patient) o;
        return id != null && Objects.equals(id, patient.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
