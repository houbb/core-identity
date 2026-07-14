package com.coreplatform.identity.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TokenGeneratorTest {

    private final TokenGenerator tokenGenerator = new TokenGenerator();

    @Test
    void shouldGenerateNonEmptyToken() {
        String token = tokenGenerator.generateRefreshToken();

        assertThat(token).isNotEmpty();
    }

    @Test
    void shouldGenerateUniqueTokens() {
        String token1 = tokenGenerator.generateRefreshToken();
        String token2 = tokenGenerator.generateRefreshToken();

        assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    void shouldGenerateTokenWithSufficientLength() {
        String token = tokenGenerator.generateRefreshToken();

        // Base64 of 64 random bytes should be at least 86 chars
        assertThat(token.length()).isGreaterThanOrEqualTo(80);
    }
}