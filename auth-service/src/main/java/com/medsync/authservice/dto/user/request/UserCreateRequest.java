package com.medsync.authservice.dto.user.request;

import com.medsync.authservice.domain.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload for creating a new user via admin.")
public record UserCreateRequest(
        @Email @NotBlank @Size(max = 100) @Schema(description = "User email address.", example = "abraham@example.com") String email,

        @Schema(description = "Raw password. Must meet security requirements.") @NotBlank String password,

        @Schema(description = "Assigned role for the new user.", example = "ADMIN") @NotNull Role role) {
}
