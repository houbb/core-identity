package com.coreplatform.starter;

import com.coreplatform.common.exception.BusinessException;
import com.coreplatform.common.exception.CommonErrorCode;
import com.coreplatform.common.response.ApiResponse;
import com.coreplatform.starter.config.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldHandleBusinessException() {
        BusinessException ex = new BusinessException(10001, "用户不存在");

        ResponseEntity<ApiResponse<Void>> response = handler.handleBusinessException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(10001);
        assertThat(response.getBody().getMessage()).isEqualTo("用户不存在");
    }

    @Test
    void shouldHandleGenericException() {
        Exception ex = new RuntimeException("Something went wrong");

        ResponseEntity<ApiResponse<Void>> response = handler.handleException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(CommonErrorCode.SYSTEM_ERROR.getCode());
        assertThat(response.getBody().getMessage()).isEqualTo(CommonErrorCode.SYSTEM_ERROR.getMessage());
    }

    @Test
    void businessExceptionResponseShouldHaveNullData() {
        BusinessException ex = new BusinessException(CommonErrorCode.VALIDATION_ERROR);

        ResponseEntity<ApiResponse<Void>> response = handler.handleBusinessException(ex);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isNull();
    }
}