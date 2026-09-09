package com.medsync.commonsecurity.config;

/**
 * Endpoints every resource server permits by default. Services can add more
 * via {@code medsync.security.permit-all} in their own config — this list is
 * the floor, not the ceiling.
 */
public final class DefaultPublicEndpoints {

    private DefaultPublicEndpoints() {
    }

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
