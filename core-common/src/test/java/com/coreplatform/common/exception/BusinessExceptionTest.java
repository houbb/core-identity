package com.coreplatform.common.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BusinessExceptionTest {

    @Test
    void shouldCreateFromErrorCode() {
        BusinessException ex = new BusinessException(CommonErrorCode.VALIDATION_ERROR);

        assertThat(ex.getCode()).isEqualTo(30001);
        assertThat(ex.getMessage()).isEqualTo("参数校验失败");
    }

    @Test
    void shouldCreateFromCodeAndMessage() {
        BusinessException ex = new BusinessException(10001, "用户不存在");

        assertThat(ex.getCode()).isEqualTo(10001);
        assertThat(ex.getMessage()).isEqualTo("用户不存在");
    }

    @Test
    void shouldBeRuntimeException() {
        BusinessException ex = new BusinessException(CommonErrorCode.SYSTEM_ERROR);

        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldThrowAndCatchCorrectly() {
        assertThatThrownBy(() -> {
            throw new BusinessException(CommonErrorCode.SYSTEM_ERROR);
        })
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(90001);
    }

    @Test
    void shouldSupportCustomErrorCodeEnum() {
        // Simulate a custom business error code
        ErrorCode customError = new ErrorCode() {
            @Override
            public int getCode() {
                return 20001;
            }

            @Override
            public String getMessage() {
                return "用户名或密码错误";
            }
        };

        BusinessException ex = new BusinessException(customError);

        assertThat(ex.getCode()).isEqualTo(20001);
        assertThat(ex.getMessage()).isEqualTo("用户名或密码错误");
    }
}