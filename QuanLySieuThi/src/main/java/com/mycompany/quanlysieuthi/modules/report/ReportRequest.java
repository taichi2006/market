package com.mycompany.quanlysieuthi.modules.report;

import java.io.Serializable;
import java.util.List;

/**
 * Request payload definitions for Report module.
 */
public class ReportRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    public static class StockAudit implements Serializable {
        private static final long serialVersionUID = 1L;

        private String auditTitle;
        private String note;
        private List<Item> items;

        public StockAudit() {
        }

        public StockAudit(String auditTitle, List<Item> items) {
            this.auditTitle = auditTitle;
            this.items = items;
        }

        public String getAuditTitle() {
            if (auditTitle != null && !auditTitle.trim().isEmpty()) {
                return auditTitle;
            }
            if (note != null && !note.trim().isEmpty()) {
                return note;
            }
            return null;
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

        public List<Item> getItems() {
            return items;
        }

        public void setItems(List<Item> items) {
            this.items = items;
        }
    }

    public static class Item implements Serializable {
        private static final long serialVersionUID = 1L;

        private String productId;
        private Integer actualQuantity;
        private String reason;
        private String discrepancyReason;

        public Item() {
        }

        public Item(String productId, Integer actualQuantity, String reason) {
            this.productId = productId;
            this.actualQuantity = actualQuantity;
            this.reason = reason;
        }

        public String getProductId() {
            return productId;
        }

        public void setProductId(String productId) {
            this.productId = productId;
        }

        public Integer getActualQuantity() {
            return actualQuantity;
        }

        public void setActualQuantity(Integer actualQuantity) {
            this.actualQuantity = actualQuantity;
        }

        public String getReason() {
            if (reason != null && !reason.trim().isEmpty()) {
                return reason;
            }
            return discrepancyReason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }

        public String getDiscrepancyReason() {
            return discrepancyReason;
        }

        public void setDiscrepancyReason(String discrepancyReason) {
            this.discrepancyReason = discrepancyReason;
        }
    }
}
