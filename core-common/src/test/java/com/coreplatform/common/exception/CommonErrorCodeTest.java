package com.coreplatform.common.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CommonErrorCodeTest {

    @Test
    void shouldHaveValidationError() {
        assertThat(CommonErrorCode.VALIDATION_ERROR.getCode()).isEqualTo(30001);
        assertThat(CommonErrorCode.VALIDATION_ERROR.getMessage()).isEqualTo("参数校验失败");
    }

    @Test
    void shouldHaveSystemError() {
        assertThat(CommonErrorCode.SYSTEM_ERROR.getCode()).isEqualTo(90001);
        assertThat(CommonErrorCode.SYSTEM_ERROR.getMessage()).isEqualTo("系统内部错误");
    }

    @Test
    void shouldImplementErrorCodeInterface() {
        assertThat(CommonErrorCode.VALIDATION_ERROR).isInstanceOf(ErrorCode.class);
        assertThat(CommonErrorCode.SYSTEM_ERROR).isInstanceOf(ErrorCode.class);
    }

    @Test
    void shouldHaveUniqueCodes() {
        long distinctCount = java.util.Arrays.stream(CommonErrorCode.values())
                .mapToInt(CommonErrorCode::getCode)
                .distinct()
                .count();
        assertThat(distinctCount).isEqualTo(CommonErrorCode.values().length);
    }
}