package com.mycompany.quanlysieuthi.report.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite Primary Key cho bảng inventory_report_detail (report_id + product_id).
 */
public class InventoryReportDetailId implements Serializable {

    private static final long serialVersionUID = 1L;

    private String reportId;
    private String productId;

    public InventoryReportDetailId() {
    }

    public InventoryReportDetailId(String reportId, String productId) {
        this.reportId = reportId;
        this.productId = productId;
    }

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InventoryReportDetailId that = (InventoryReportDetailId) o;
        return Objects.equals(reportId, that.reportId) && Objects.equals(productId, that.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reportId, productId);
    }
}
