package com.medsync.patientservice.dto.request;

import com.medsync.patientservice.domain.enums.BloodType;
import com.medsync.patientservice.domain.enums.Gender;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

import static com.medsync.patientservice.domain.constants.PatientConstraints.*;

public record UpdatePatientRequest(
        @NotBlank @Size(max = MAX_NAME_LENGTH)
        String firstName,

        @NotBlank @Size(max = MAX_NAME_LENGTH)
        String lastName,

        @NotBlank
        @Pattern(regexp = "^(\\d{8}|\\d{10}|[A-Za-z0-9]{6,9})$", message = "documentNumber format is invalid")
        String documentNumber,

        @NotNull
        Gender gender,

        @NotNull @PastOrPresent
        LocalDate birthDate,

        @NotBlank @Size(max = MAX_PHONE_LENGTH)
        @Pattern(regexp = "^[0-9+\\-() ]+$", message = "phone format is invalid")
        String phone,

        @NotBlank @Email
        String email,

        @Size(max = MAX_ADDRESS_LENGTH)
        String address,

        @NotNull
        BloodType bloodType
) {
}