package com.mycompany.quanlysieuthi.report.dto.response.detail;

import com.mycompany.quanlysieuthi.report.dto.response.common.StockAuditEmployeeDto;

import java.io.Serializable;
import java.util.List;

/**
 * DTO phản hồi chi tiết biên bản kiểm kê kho (GET /report/stock-discrepancy/:id).
 */
public class StockAuditDetailResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private String reportId;
    private String auditTitle;
    private String note;
    private String createdAt;
    private StockAuditEmployeeDto employee;
    private Integer totalDiscrepancyItems;
    private List<StockDiscrepancyDetailItemDto> items;

    public StockAuditDetailResponse() {
    }

    public StockAuditDetailResponse(String reportId, String auditTitle, String note,
                                    String createdAt,
                                    StockAuditEmployeeDto employee,
                                    Integer totalDiscrepancyItems,
                                    List<StockDiscrepancyDetailItemDto> items) {
        this.reportId = reportId;
        this.auditTitle = auditTitle;
        this.note = note;
        this.createdAt = createdAt;
        this.employee = employee;
        this.totalDiscrepancyItems = totalDiscrepancyItems;
        this.items = items;
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

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public StockAuditEmployeeDto getEmployee() {
        return employee;
    }

    public void setEmployee(StockAuditEmployeeDto employee) {
        this.employee = employee;
    }

    public Integer getTotalDiscrepancyItems() {
        return totalDiscrepancyItems;
    }

    public void setTotalDiscrepancyItems(Integer totalDiscrepancyItems) {
        this.totalDiscrepancyItems = totalDiscrepancyItems;
    }

    public List<StockDiscrepancyDetailItemDto> getItems() {
        return items;
    }

    public void setItems(List<StockDiscrepancyDetailItemDto> items) {
        this.items = items;
    }
}
