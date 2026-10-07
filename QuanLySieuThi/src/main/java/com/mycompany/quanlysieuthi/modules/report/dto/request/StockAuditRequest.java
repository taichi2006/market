package com.mycompany.quanlysieuthi.modules.report.dto.request;

import java.io.Serializable;
import java.util.List;

/**
 * DTO nhận dữ liệu tạo biên bản kiểm kê kho định kỳ từ client (POST /report/stock-discrepancy).
 */
public class StockAuditRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String auditTitle;
    private String note;
    private List<StockAuditItemRequest> items;

    public StockAuditRequest() {
    }

    public StockAuditRequest(String auditTitle, String note, List<StockAuditItemRequest> items) {
        this.auditTitle = auditTitle;
        this.note = note;
        this.items = items;
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

    public List<StockAuditItemRequest> getItems() {
        return items;
    }

    public void setItems(List<StockAuditItemRequest> items) {
        this.items = items;
    }
}
