package com.coreplatform.identity.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {

    @Test
    void shouldCreateSuccessResponse() {
        ApiResponse<String> response = ApiResponse.success("hello");

        assertThat(response.getCode()).isEqualTo(0);
        assertThat(response.getMessage()).isEqualTo("success");
        assertThat(response.getData()).isEqualTo("hello");
    }

    @Test
    void shouldCreateSuccessResponseWithoutData() {
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
    void shouldCreateResponseWithNullData() {
        ApiResponse<String> response = ApiResponse.success(null);

        assertThat(response.getCode()).isEqualTo(0);
        assertThat(response.getData()).isNull();
    }
}