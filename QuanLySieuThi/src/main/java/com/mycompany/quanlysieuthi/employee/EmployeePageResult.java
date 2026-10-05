package com.mycompany.quanlysieuthi.employee;

import com.mycompany.quanlysieuthi.util.PageMeta;

import java.io.Serializable;
import java.util.List;

/**
 * Result container for paginated employee query.
 */
public class EmployeePageResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<EmployeeResponse> data;
    private PageMeta meta;

    public EmployeePageResult() {
    }

    public EmployeePageResult(List<EmployeeResponse> data, PageMeta meta) {
        this.data = data;
        this.meta = meta;
    }

    public List<EmployeeResponse> getData() {
        return data;
    }

    public void setData(List<EmployeeResponse> data) {
        this.data = data;
    }

    public PageMeta getMeta() {
        return meta;
    }

    public void setMeta(PageMeta meta) {
        this.meta = meta;
    }
}
