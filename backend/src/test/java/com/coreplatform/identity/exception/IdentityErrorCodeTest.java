package com.coreplatform.identity.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IdentityErrorCodeTest {

    @Test
    void shouldHaveCorrectCodeAndMessage() {
        assertThat(IdentityErrorCode.USER_NOT_FOUND.getCode()).isEqualTo(10001);
        assertThat(IdentityErrorCode.USER_NOT_FOUND.getMessage()).isEqualTo("用户不存在");
    }

    @Test
    void shouldDistinguishUserAndAuthErrors() {
        assertThat(IdentityErrorCode.USER_NOT_FOUND.getCode()).isBetween(10000, 19999);
        assertThat(IdentityErrorCode.AUTH_INVALID_CREDENTIALS.getCode()).isBetween(20000, 29999);
        assertThat(IdentityErrorCode.VALIDATION_ERROR.getCode()).isBetween(30000, 39999);
        assertThat(IdentityErrorCode.SYSTEM_ERROR.getCode()).isBetween(90000, 99999);
    }

    @Test
    void shouldAllCodesBeUnique() {
        long distinctCount = java.util.Arrays.stream(IdentityErrorCode.values())
                .map(IdentityErrorCode::getCode)
                .distinct()
                .count();
        assertThat(distinctCount).isEqualTo(IdentityErrorCode.values().length);
    }

    @Test
    void shouldImplementErrorCodeInterface() {
        assertThat(IdentityErrorCode.USER_NOT_FOUND)
                .isInstanceOf(com.coreplatform.common.exception.ErrorCode.class);
    }
}