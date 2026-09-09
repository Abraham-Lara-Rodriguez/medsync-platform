package com.medsync.commonsecurity.jwt;

import com.medsync.commonsecurity.config.JwtValidationProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

import javax.crypto.SecretKey;
import java.util.List;
import java.util.Set;

/**
 * Resource-server side JWT handling.
 * <p>
 * Deliberately the mirror-image of auth-service's {@code JwtService}: it can
 * parse and verify a token signed by auth-service, but it has no method to
 * build one. Downstream services must never issue tokens — only auth-service
 * owns the credential/login flow.
 */
@RequiredArgsConstructor
public class JwtValidator {

    private static final String CLAIM_TYPE = "type";
    private static final String CLAIM_ROLES = "roles";
    private static final String TYPE_ACCESS = "access";

    private final JwtValidationProperties properties;

    /**
     * Parses and verifies a signed JWT. Throws JwtException if the signature,
     * expiration, or structure is invalid.
     */
    public Claims parse(String token) {

        Claims claims = Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        if (!"medsync-auth-service".equals(claims.getIssuer())) {
            throw new JwtException("Invalid issuer");
        }

        Set<String> audience = claims.getAudience();

        if (audience == null || !audience.contains("medsync-platform")) {
            throw new JwtException("Invalid audience");
        }

        return claims;
    }

    /**
     * True only for a valid, non-expired ACCESS token (rejects refresh tokens,
     * mirroring the same "type" claim check auth-service performs).
     */
    public boolean isAccessToken(Claims claims) {
        return TYPE_ACCESS.equals(claims.get(CLAIM_TYPE, String.class));
    }

    public String extractUsername(Claims claims) {
        return claims.getSubject();
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(Claims claims) {
        List<String> raw = claims.get(CLAIM_ROLES, List.class);
        return raw == null ? List.of() : raw;
    }

    private SecretKey getSignInKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.getSecretKey()));
    }
}
