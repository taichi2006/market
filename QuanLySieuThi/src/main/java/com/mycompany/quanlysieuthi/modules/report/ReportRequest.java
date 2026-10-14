package com.mycompany.quanlysieuthi.modules.report;

import java.io.Serializable;
import java.util.List;

/**
 * Request payload definitions for Report module.
 */
public class ReportRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    public record StockAudit(String auditTitle, String note, List<Item> items) implements Serializable {
        public StockAudit(String auditTitle, List<Item> items) {
            this(auditTitle, null, items);
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

        public String getNote() { return note; }
        public List<Item> getItems() { return items; }
    }

    public record Item(String productId, Integer actualQuantity,
                       String reason, String discrepancyReason) implements Serializable {
        public Item(String productId, Integer actualQuantity, String reason) {
            this(productId, actualQuantity, reason, null);
        }

        public String getProductId() { return productId; }
        public Integer getActualQuantity() { return actualQuantity; }
        public String getReason() {
            if (reason != null && !reason.trim().isEmpty()) {
                return reason;
            }
            return discrepancyReason;
        }
        public String getDiscrepancyReason() { return discrepancyReason; }
    }
}
