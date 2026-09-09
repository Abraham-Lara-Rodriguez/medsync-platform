package com.medsync.commoncore.error.dto;

import com.medsync.commoncore.error.enums.ErrorCode;

import java.time.Instant;

public record ProblemDetails(
        String type,      // URI that identifies the error category
        String title,     // Human-readable summary
        int status,        // HTTP status code
        String detail,    // Detailed explanation
        String instance,  // URI of the resource involved
        Instant timestamp,
        ErrorCode code     // Custom application code
) {
}