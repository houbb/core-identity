package com.coreplatform.identity.admin.controller;

import com.coreplatform.common.response.ApiResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HealthControllerTest {

    @Test
    void shouldReturnOk() {
        HealthController controller = new HealthController();
        ApiResponse<String> response = controller.health();

        assertThat(response.getCode()).isEqualTo(0);
        assertThat(response.getMessage()).isEqualTo("success");
        assertThat(response.getData()).isEqualTo("ok");
    }
}