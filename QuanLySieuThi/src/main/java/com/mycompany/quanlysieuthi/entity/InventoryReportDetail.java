package com.mycompany.quanlysieuthi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.Objects;

/**
 * Entity ánh xạ chính xác bảng inventory_report_detail trên Neon DB.
 */
@Entity
@Table(name = "inventory_report_detail")
@IdClass(InventoryReportDetail.PK.class)
public class InventoryReportDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Composite Primary Key nội bộ cho bảng inventory_report_detail.
     */
    public static class PK implements Serializable {
        private static final long serialVersionUID = 1L;

        private String reportId;
        private String productId;

        public PK() {
        }

        public PK(String reportId, String productId) {
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
            PK pk = (PK) o;
            return Objects.equals(reportId, pk.reportId) &&
                   Objects.equals(productId, pk.productId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(reportId, productId);
        }
    }

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
