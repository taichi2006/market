package com.mycompany.quanlysieuthi.modules.report.dto.response.detail;

import java.io.Serializable;

/**
 * DTO đại diện cho một mặt hàng trong chi tiết biên bản kiểm kê kho (GET /report/stock-discrepancy/:id).
 */
public class StockDiscrepancyDetailItemDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String auditDetailId;
    private String productId;
    private String productName;
    private String categoryName;
    private Integer systemQuantity;
    private Integer actualQuantity;
    private Integer difference;
    private String status;
    private String reason;

    public StockDiscrepancyDetailItemDto() {
    }

    public StockDiscrepancyDetailItemDto(String auditDetailId, String productId,
                                         String productName, String categoryName,
                                         Integer systemQuantity, Integer actualQuantity,
                                         Integer difference, String status, String reason) {
        this.auditDetailId = auditDetailId;
        this.productId = productId;
        this.productName = productName;
        this.categoryName = categoryName;
        this.systemQuantity = systemQuantity;
        this.actualQuantity = actualQuantity;
        this.difference = difference;
        this.status = status;
        this.reason = reason;
    }

    public String getAuditDetailId() {
        return auditDetailId;
    }

    public void setAuditDetailId(String auditDetailId) {
        this.auditDetailId = auditDetailId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Integer getSystemQuantity() {
        return systemQuantity;
    }

    public void setSystemQuantity(Integer systemQuantity) {
        this.systemQuantity = systemQuantity;
    }

    public Integer getActualQuantity() {
        return actualQuantity;
    }

    public void setActualQuantity(Integer actualQuantity) {
        this.actualQuantity = actualQuantity;
    }

    public Integer getDifference() {
        return difference;
    }

    public void setDifference(Integer difference) {
        this.difference = difference;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
