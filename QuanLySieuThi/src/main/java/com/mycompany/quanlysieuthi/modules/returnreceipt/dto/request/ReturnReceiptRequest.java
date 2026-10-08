package com.mycompany.quanlysieuthi.modules.returnreceipt.dto.request;

import java.util.List;

/**
 * DTO representing POST /api/return-receipt request payload.
 */
public class ReturnReceiptRequest {

    private String invoiceId;
    private String reason;
    private List<ReturnReceiptItemRequest> items;

    public ReturnReceiptRequest() {
    }

    public ReturnReceiptRequest(String invoiceId, String reason, List<ReturnReceiptItemRequest> items) {
        this.invoiceId = invoiceId;
        this.reason = reason;
        this.items = items;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public List<ReturnReceiptItemRequest> getItems() {
        return items;
    }

    public void setItems(List<ReturnReceiptItemRequest> items) {
        this.items = items;
    }
}
