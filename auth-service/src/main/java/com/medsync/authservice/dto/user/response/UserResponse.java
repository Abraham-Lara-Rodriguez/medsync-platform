package com.medsync.authservice.dto.user.response;

import com.medsync.authservice.domain.enums.Role;
import com.medsync.authservice.domain.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Represents a user returned by API operations.")
public record UserResponse(
        @Schema(description = "Unique identifier of the user", example = "42")
        UUID id,

        @Schema(description = "Registered email of the user", example = "abraham@example.com")
        String email,

        @Schema(description = "Assigned role defining authorization level", example = "ADMIN")
        Role role,

        @Schema(description = "Current account status", example = "ACTIVE")
        UserStatus status
) {
}