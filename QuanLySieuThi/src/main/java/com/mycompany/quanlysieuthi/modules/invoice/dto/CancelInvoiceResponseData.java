package com.mycompany.quanlysieuthi.modules.invoice.dto;

import java.io.Serializable;

public class CancelInvoiceResponseData implements Serializable {

    private static final long serialVersionUID = 1L;

    private String invoiceId;
    private String status;

    public CancelInvoiceResponseData() {
    }

    public CancelInvoiceResponseData(String invoiceId, String status) {
        this.invoiceId = invoiceId;
        this.status = status;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
