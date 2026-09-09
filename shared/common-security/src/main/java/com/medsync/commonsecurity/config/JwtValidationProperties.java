package com.medsync.commonsecurity.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Shared JWT verification settings for every downstream (non auth-issuing) service.
 * <p>
 * Bound from {@code medsync.security.*}. Only the secret key is required to VERIFY a
 * signature — services that use this module never generate tokens and never need
 * access-token / refresh-token expiration values (that belongs to auth-service only).
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "medsync.security")
public class JwtValidationProperties {

    /**
     * Same HS256 secret configured in auth-service (auth-service.security.secret-key).
     * Must match exactly or signature verification will fail for every request.
     */
    @NotBlank
    @Size(min = 32, message = "JWT secret key must be at least 32 characters")
    private String secretKey;

    /**
     * Extra paths this service wants public, on top of the module's built-in
     * defaults (actuator health/info, swagger). Supports Ant-style patterns.
     */
    private List<String> permitAll = List.of();
}
