package com.medsync.authservice.config.properties;

public final class SecurityEndpointsProperties {

    private SecurityEndpointsProperties() {
    }

    public static final String[] PUBLIC = {
            "/api/v1/auth/login",
            "/api/v1/auth/refresh-token"
    };

    public static final String[] MONITORING = {
            "/actuator/health",
            "/actuator/info"
    };

    public static final String[] SWAGGER = {
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**"
    };
}