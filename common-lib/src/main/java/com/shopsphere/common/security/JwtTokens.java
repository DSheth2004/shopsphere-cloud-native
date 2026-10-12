package com.shopsphere.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import javax.crypto.SecretKey;

/** Issues and verifies the HS256 access tokens shared by auth-service and api-gateway. */
public final class JwtTokens {
    public static final String ISSUER = "shopsphere-auth";
    private final SecretKey key;
    private final Duration ttl;

    public JwtTokens(String secret, Duration ttl) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("JWT_SECRET must be at least 32 bytes");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.ttl = ttl;
    }

    public String issue(String subject, String role) {
        Instant now = Instant.now();
        return Jwts.builder().issuer(ISSUER).subject(subject).claim("role", role)
                .issuedAt(Date.from(now)).expiration(Date.from(now.plus(ttl))).signWith(key).compact();
    }

    public Optional<Principal> verify(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(key).requireIssuer(ISSUER).build()
                    .parseSignedClaims(token).getPayload();
            return Optional.of(new Principal(claims.getSubject(), claims.get("role", String.class)));
        } catch (JwtException | IllegalArgumentException invalid) {
            return Optional.empty();
        }
    }

    public Duration ttl() { return ttl; }

    public record Principal(String userId, String role) { }
}
