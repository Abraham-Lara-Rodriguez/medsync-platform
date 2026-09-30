package com.medsync.doctorservice.dto.request;

import com.medsync.doctorservice.domain.enums.DoctorStatus;
import jakarta.validation.constraints.*;

import static com.medsync.doctorservice.domain.constants.DoctorConstraints.*;

// TODO: evaluate whether the fields (specialty, medicalLicense) need to be updated (modified).
public record UpdateDoctorRequest(
        @NotBlank(message = "First name is required")
        @Size(max = MAX_NAME_LENGTH, message = "First name cannot exceed " + MAX_NAME_LENGTH + " characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = MAX_NAME_LENGTH, message = "Last name cannot exceed " + MAX_NAME_LENGTH + " characters")
        String lastName,

        @NotBlank(message = "Email is required")
        @Size(max = MAX_EMAIL_LENGTH, message = "Email cannot exceed " + MAX_EMAIL_LENGTH + " characters")
        @Email(message = "Email format is invalid")
        String email,

        @NotBlank(message = "Phone is required")
        @Size(max = MAX_PHONE_LENGTH, message = "Phone cannot exceed " + MAX_PHONE_LENGTH + " characters")
        @Pattern(regexp = PHONE_REGEX, message = "Phone contains invalid characters")
        String phone,

        @NotNull(message = "Status is required")
        DoctorStatus status
) { }