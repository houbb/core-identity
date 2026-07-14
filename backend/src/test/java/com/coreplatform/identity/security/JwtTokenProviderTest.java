package com.coreplatform.identity.security;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(
                "this-is-a-secret-key-for-testing-purposes-at-least-256-bits-long!!",
                900000L,
                604800000L);
    }

    @Test
    void shouldGenerateAndParseAccessToken() {
        String token = jwtTokenProvider.generateAccessToken(42L, "echoma");

        assertThat(token).isNotEmpty();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getUserIdFromToken(token)).isEqualTo(42L);
        assertThat(jwtTokenProvider.getUsernameFromToken(token)).isEqualTo("echoma");
    }

    @Test
    void shouldGenerateRefreshToken() {
        String token = jwtTokenProvider.generateRefreshToken(42L);

        assertThat(token).isNotEmpty();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getUserIdFromToken(token)).isEqualTo(42L);
    }

    @Test
    void shouldRejectInvalidToken() {
        assertThat(jwtTokenProvider.validateToken("invalid-token")).isFalse();
        assertThat(jwtTokenProvider.validateToken("")).isFalse();
        assertThat(jwtTokenProvider.validateToken(null)).isFalse();
    }

    @Test
    void shouldRejectExpiredToken() {
        // Create provider with 0ms token expiry (instant expiration)
        var expiredProvider = new JwtTokenProvider(
                "another-test-secret-key-for-testing-must-be-256-bits-long!!",
                0L, 0L);

        String token = expiredProvider.generateAccessToken(1L, "test");

        assertThat(expiredProvider.validateToken(token)).isFalse();
    }

    @Test
    void shouldHaveCorrectExpirationValues() {
        assertThat(jwtTokenProvider.getAccessTokenExpiration()).isEqualTo(900000L);
    }

    @Test
    void shouldGenerateDifferentTokensForDifferentUsers() {
        String token1 = jwtTokenProvider.generateAccessToken(1L, "user1");
        String token2 = jwtTokenProvider.generateAccessToken(2L, "user2");

        assertThat(token1).isNotEqualTo(token2);
        assertThat(jwtTokenProvider.getUserIdFromToken(token1)).isEqualTo(1L);
        assertThat(jwtTokenProvider.getUserIdFromToken(token2)).isEqualTo(2L);
    }
}