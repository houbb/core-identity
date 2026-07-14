package com.coreplatform.identity.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorCodeTest {

    @Test
    void shouldHaveCorrectCodeAndMessage() {
        assertThat(ErrorCode.USER_NOT_FOUND.getCode()).isEqualTo(10001);
        assertThat(ErrorCode.USER_NOT_FOUND.getMessage()).isEqualTo("用户不存在");
    }

    @Test
    void shouldDistinguishUserAndAuthErrors() {
        assertThat(ErrorCode.USER_NOT_FOUND.getCode()).isBetween(10000, 19999);
        assertThat(ErrorCode.AUTH_INVALID_CREDENTIALS.getCode()).isBetween(20000, 29999);
        assertThat(ErrorCode.VALIDATION_ERROR.getCode()).isBetween(30000, 39999);
        assertThat(ErrorCode.SYSTEM_ERROR.getCode()).isBetween(90000, 99999);
    }

    @Test
    void shouldAllCodesBeUnique() {
        long distinctCount = java.util.Arrays.stream(ErrorCode.values())
                .map(ErrorCode::getCode)
                .distinct()
                .count();
        assertThat(distinctCount).isEqualTo(ErrorCode.values().length);
    }
}