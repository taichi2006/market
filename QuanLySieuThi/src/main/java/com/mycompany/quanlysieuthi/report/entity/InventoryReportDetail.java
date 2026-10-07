package com.mycompany.quanlysieuthi.report.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.io.Serializable;

/**
 * Entity ánh xạ chính xác bảng inventory_report_detail trên Neon DB.
 */
@Entity
@Table(name = "inventory_report_detail")
@IdClass(InventoryReportDetailId.class)
public class InventoryReportDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "report_id", length = 36, nullable = false)
    private String reportId;

    @Id
    @Column(name = "product_id", length = 36, nullable = false)
    private String productId;

    @Column(name = "system_quantity", nullable = false)
    private Integer systemQuantity;

    @Column(name = "actual_quantity", nullable = false)
    private Integer actualQuantity;

    public InventoryReportDetail() {
    }

    public InventoryReportDetail(String reportId, String productId, Integer systemQuantity, Integer actualQuantity) {
        this.reportId = reportId;
        this.productId = productId;
        this.systemQuantity = systemQuantity;
        this.actualQuantity = actualQuantity;
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
}
