package com.coreplatform.common.response;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {

    @Test
    void shouldCreateSuccessResponseWithData() {
        ApiResponse<String> response = ApiResponse.success("hello");

        assertThat(response.getCode()).isEqualTo(0);
        assertThat(response.getMessage()).isEqualTo("success");
        assertThat(response.getData()).isEqualTo("hello");
    }

    @Test
    void shouldCreateSuccessResponseWithNullData() {
        ApiResponse<Void> response = ApiResponse.success();

        assertThat(response.getCode()).isEqualTo(0);
        assertThat(response.getMessage()).isEqualTo("success");
        assertThat(response.getData()).isNull();
    }

    @Test
    void shouldCreateErrorResponse() {
        ApiResponse<Void> response = ApiResponse.error(10001, "用户不存在");

        assertThat(response.getCode()).isEqualTo(10001);
        assertThat(response.getMessage()).isEqualTo("用户不存在");
        assertThat(response.getData()).isNull();
    }

    @Test
    void shouldCreateErrorResponseWithSystemError() {
        ApiResponse<Void> response = ApiResponse.error(90001, "系统内部错误");

        assertThat(response.getCode()).isEqualTo(90001);
        assertThat(response.getMessage()).isEqualTo("系统内部错误");
    }

    @Test
    void shouldSupportGenericTypes() {
        ApiResponse<Integer> response = ApiResponse.success(42);

        assertThat(response.getData()).isEqualTo(42);
    }
}