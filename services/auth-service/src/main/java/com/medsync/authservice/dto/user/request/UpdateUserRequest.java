package com.medsync.authservice.dto.user.request;

import com.medsync.authservice.domain.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;

/**
 * Request body for updating an existing user.
 * Supports administrative resets and role changes.
 */
public record UpdateUserRequest(
        @Schema(description = "New email address", example = "abraham@example.com")
        @Email
        String email,

        @Schema(description = "Optional password reset. Only used by admins.")
        String password,

        @Schema(description = "Updated role assigned to the user", example = "ADMIN")
        Role role
) {
}