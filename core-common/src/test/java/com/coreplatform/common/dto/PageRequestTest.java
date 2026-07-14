package com.coreplatform.common.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PageRequestTest {

    @Test
    void shouldUseDefaultValues() {
        PageRequest request = new PageRequest();

        assertThat(request.getPage()).isEqualTo(1);
        assertThat(request.getSize()).isEqualTo(20);
    }

    @Test
    void shouldAcceptValidValues() {
        PageRequest request = new PageRequest(2, 50);

        assertThat(request.getPage()).isEqualTo(2);
        assertThat(request.getSize()).isEqualTo(50);
    }

    @Test
    void shouldClampNegativePageTo1() {
        PageRequest request = new PageRequest(-1, 20);

        assertThat(request.getPage()).isEqualTo(1);
    }

    @Test
    void shouldClampZeroPageTo1() {
        PageRequest request = new PageRequest(0, 20);

        assertThat(request.getPage()).isEqualTo(1);
    }

    @Test
    void shouldClampNegativeSizeTo20() {
        PageRequest request = new PageRequest(1, -5);

        assertThat(request.getSize()).isEqualTo(20);
    }

    @Test
    void shouldClampZeroSizeTo20() {
        PageRequest request = new PageRequest(1, 0);

        assertThat(request.getSize()).isEqualTo(20);
    }

    @Test
    void shouldClampViaSetters() {
        PageRequest request = new PageRequest();
        request.setPage(-1);
        request.setSize(-1);

        assertThat(request.getPage()).isEqualTo(1);
        assertThat(request.getSize()).isEqualTo(20);
    }
}