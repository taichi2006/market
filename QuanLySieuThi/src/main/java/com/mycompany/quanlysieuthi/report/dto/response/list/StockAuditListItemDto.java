package com.mycompany.quanlysieuthi.report.dto.response.list;

import com.mycompany.quanlysieuthi.report.dto.response.common.StockAuditEmployeeDto;

import java.io.Serializable;

/**
 * DTO đại diện cho một phần tử trong danh sách biên bản kiểm kê kho (GET /report/stock-discrepancy).
 */
public class StockAuditListItemDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String reportId;
    private String auditTitle;
    private String createdAt;
    private Integer totalDiscrepancyItems;
    private StockAuditEmployeeDto employee;

    public StockAuditListItemDto() {
    }

    public StockAuditListItemDto(String reportId, String auditTitle, String createdAt,
                                 Integer totalDiscrepancyItems,
                                 StockAuditEmployeeDto employee) {
        this.reportId = reportId;
        this.auditTitle = auditTitle;
        this.createdAt = createdAt;
        this.totalDiscrepancyItems = totalDiscrepancyItems;
        this.employee = employee;
    }

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getAuditTitle() {
        return auditTitle;
    }

    public void setAuditTitle(String auditTitle) {
        this.auditTitle = auditTitle;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getTotalDiscrepancyItems() {
        return totalDiscrepancyItems;
    }

    public void setTotalDiscrepancyItems(Integer totalDiscrepancyItems) {
        this.totalDiscrepancyItems = totalDiscrepancyItems;
    }

    public StockAuditEmployeeDto getEmployee() {
        return employee;
    }

    public void setEmployee(StockAuditEmployeeDto employee) {
        this.employee = employee;
    }
}
