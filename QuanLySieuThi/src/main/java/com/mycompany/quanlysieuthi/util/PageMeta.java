package com.mycompany.quanlysieuthi.util;

import java.io.Serializable;

/**
 * Metadata DTO for paginated responses.
 */
public class PageMeta implements Serializable {

    private static final long serialVersionUID = 1L;

    private int page;
    private int limit;
    private long totalElements;
    private int totalPages;

    public PageMeta() {
    }

    public PageMeta(int page, int limit, long totalElements, int totalPages) {
        this.page = page;
        this.limit = limit;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}
