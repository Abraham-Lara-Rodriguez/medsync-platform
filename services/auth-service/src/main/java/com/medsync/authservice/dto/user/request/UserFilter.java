package com.medsync.authservice.dto.user.request;

import com.medsync.authservice.domain.enums.auth.Role;
import com.medsync.authservice.domain.enums.user.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Filtering options for querying users.")
public record UserFilter(
        @Schema(description = "Free text matching username or email.")
        String search,

        @Schema(description = "Filter by role.")
        Role role,

        @Schema(description = "Filter by status.")
        UserStatus status
) {
}