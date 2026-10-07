package com.mycompany.quanlysieuthi.modules.report.dto.response.create;

import java.io.Serializable;
import java.util.List;

/**
 * DTO phản hồi thành công sau khi tạo biên bản kiểm kê kho (POST /report/stock-discrepancy).
 */
public class StockAuditResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private String reportId;
    private String auditTitle;
    private String createdAt;
    private AuditCreatedByDto createdBy;
    private Integer totalDiscrepancyItems;
    private List<StockDiscrepancyItemDto> discrepancies;

    public StockAuditResponse() {
    }

    public StockAuditResponse(String reportId, String auditTitle, String createdAt,
                              AuditCreatedByDto createdBy, Integer totalDiscrepancyItems,
                              List<StockDiscrepancyItemDto> discrepancies) {
        this.reportId = reportId;
        this.auditTitle = auditTitle;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.totalDiscrepancyItems = totalDiscrepancyItems;
        this.discrepancies = discrepancies;
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

    public AuditCreatedByDto getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(AuditCreatedByDto createdBy) {
        this.createdBy = createdBy;
    }

    public Integer getTotalDiscrepancyItems() {
        return totalDiscrepancyItems;
    }

    public void setTotalDiscrepancyItems(Integer totalDiscrepancyItems) {
        this.totalDiscrepancyItems = totalDiscrepancyItems;
    }

    public List<StockDiscrepancyItemDto> getDiscrepancies() {
        return discrepancies;
    }

    public void setDiscrepancies(List<StockDiscrepancyItemDto> discrepancies) {
        this.discrepancies = discrepancies;
    }
}
