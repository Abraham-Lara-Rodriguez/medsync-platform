package com.medsync.authservice.config.properties;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "auth-service.security")
public class AuthSecurityProperties {

    @NotBlank
    @Size(min = 32, message = "JWT secret key must be at least 32 characters")
    private String secretKey;

    @Positive
    private long accessTokenExpiration;

    @Positive
    private long refreshTokenExpiration;

    @Min(4)
    @Max(31)
    private int passwordStrength = 10;

    @NotBlank
    String clientOrigin;
}
