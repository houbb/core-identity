package com.coreplatform.common.response;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageResultTest {

    @Test
    void shouldCreatePageResult() {
        List<String> records = List.of("a", "b", "c");
        PageResult<String> result = new PageResult<>(3, 1, 20, records);

        assertThat(result.getTotal()).isEqualTo(3);
        assertThat(result.getPage()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(20);
        assertThat(result.getRecords()).containsExactly("a", "b", "c");
    }

    @Test
    void shouldCreateEmptyPageResult() {
        PageResult<String> result = PageResult.empty(1, 20);

        assertThat(result.getTotal()).isEqualTo(0);
        assertThat(result.getPage()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(20);
        assertThat(result.getRecords()).isEmpty();
    }

    @Test
    void shouldHandleEmptyRecords() {
        PageResult<String> result = new PageResult<>(0, 1, 10, List.of());

        assertThat(result.getTotal()).isEqualTo(0);
        assertThat(result.getRecords()).isEmpty();
    }
}