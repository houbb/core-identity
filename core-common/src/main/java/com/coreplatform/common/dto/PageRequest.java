package com.coreplatform.common.dto;

/**
 * 统一分页请求 — 所有 core-* 模块的分页查询入参。
 *
 * <p>默认 page=1, size=20。</p>
 */
public class PageRequest {

    private int page = 1;
    private int size = 20;

    public PageRequest() {
    }

    public PageRequest(int page, int size) {
        this.page = page > 0 ? page : 1;
        this.size = size > 0 ? size : 20;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page > 0 ? page : 1;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size > 0 ? size : 20;
    }
}