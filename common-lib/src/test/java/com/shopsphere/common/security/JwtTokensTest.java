package com.shopsphere.common.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class JwtTokensTest {
    private final JwtTokens tokens = new JwtTokens("unit-test-secret-that-is-at-least-32-bytes", Duration.ofMinutes(5));

    @Test
    void issuedTokenRoundTrips() {
        String token = tokens.issue("ada@shopsphere.local", "ADMIN");
        assertThat(tokens.verify(token)).hasValueSatisfying(principal -> {
            assertThat(principal.userId()).isEqualTo("ada@shopsphere.local");
            assertThat(principal.role()).isEqualTo("ADMIN");
        });
    }

    @Test
    void tokenSignedWithAnotherKeyIsRejected() {
        JwtTokens other = new JwtTokens("a-completely-different-secret-of-32-bytes", Duration.ofMinutes(5));
        assertThat(tokens.verify(other.issue("mallory", "ADMIN"))).isEmpty();
        assertThat(tokens.verify("not-a-jwt")).isEmpty();
    }

    @Test
    void shortSecretsAreRefused() {
        assertThatThrownBy(() -> new JwtTokens("short", Duration.ofMinutes(5))).isInstanceOf(IllegalArgumentException.class);
    }
}
