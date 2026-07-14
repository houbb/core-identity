package com.coreplatform.identity.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PageResultTest {

    @Test
    void shouldCreatePageResult() {
        var records = java.util.List.of("a", "b", "c");
        PageResult<String> page = new PageResult<>(3, 1, 20, records);

        assertThat(page.getTotal()).isEqualTo(3);
        assertThat(page.getPage()).isEqualTo(1);
        assertThat(page.getSize()).isEqualTo(20);
        assertThat(page.getRecords()).hasSize(3);
    }

    @Test
    void shouldHandleEmptyPage() {
        PageResult<String> page = new PageResult<>(0, 1, 20, java.util.List.of());

        assertThat(page.getTotal()).isEqualTo(0);
        assertThat(page.getRecords()).isEmpty();
    }

    @Test
    void shouldHaveDefaultConstructor() {
        PageResult<String> page = new PageResult<>();

        assertThat(page.getTotal()).isEqualTo(0);
        assertThat(page.getRecords()).isNull();
    }
}