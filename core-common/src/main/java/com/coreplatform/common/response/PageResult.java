package com.coreplatform.common.response;

import java.util.List;

/**
 * 统一分页结果 — 所有 core-* 模块的分页接口必须使用此格式。
 *
 * <pre>
 * {
 *   "code": 0,
 *   "message": "success",
 *   "data": {
 *     "total": 100,
 *     "page": 1,
 *     "size": 20,
 *     "records": []
 *   }
 * }
 * </pre>
 */
public class PageResult<T> {

    private long total;
    private int page;
    private int size;
    private List<T> records;

    public PageResult() {
    }

    public PageResult(long total, int page, int size, List<T> records) {
        this.total = total;
        this.page = page;
        this.size = size;
        this.records = records;
    }

    public static <T> PageResult<T> empty(int page, int size) {
        return new PageResult<>(0, page, size, List.of());
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }
}