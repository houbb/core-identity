package com.coreplatform.identity.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RefreshTokenTest {

    @Test
    void shouldBeValidWhenNotRevokedAndNotExpired() {
        RefreshToken token = new RefreshToken();
        token.setRevoked(false);
        token.setExpireTime(java.time.LocalDateTime.now().plusDays(1));

        assertThat(token.isValid()).isTrue();
    }

    @Test
    void shouldBeInvalidWhenRevoked() {
        RefreshToken token = new RefreshToken();
        token.setRevoked(true);
        token.setExpireTime(java.time.LocalDateTime.now().plusDays(1));

        assertThat(token.isValid()).isFalse();
    }

    @Test
    void shouldBeInvalidWhenExpired() {
        RefreshToken token = new RefreshToken();
        token.setRevoked(false);
        token.setExpireTime(java.time.LocalDateTime.now().minusDays(1));

        assertThat(token.isValid()).isFalse();
    }

    @Test
    void shouldBeInvalidWhenBothRevokedAndExpired() {
        RefreshToken token = new RefreshToken();
        token.setRevoked(true);
        token.setExpireTime(java.time.LocalDateTime.now().minusDays(1));

        assertThat(token.isValid()).isFalse();
    }
}