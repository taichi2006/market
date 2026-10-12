package com.mycompany.quanlysieuthi.modules.returnreceipt.dto.response;

import java.math.BigDecimal;

/**
 * Full detail response for a return receipt (GET /api/return-receipt/:id).
 */
public class ReturnReceiptDetailResponse {

    private String returnReceiptId;
    private String invoiceId;
    private BigDecimal refundAmount;
    private String reason;
    private String createdAt;
    private ReturnReceiptEmployeeDto employee;
    private ReturnReceiptCustomerDto customer;

    public ReturnReceiptDetailResponse() {
    }

    public ReturnReceiptDetailResponse(String returnReceiptId, String invoiceId, BigDecimal refundAmount,
            String reason, String createdAt, ReturnReceiptEmployeeDto employee,
            ReturnReceiptCustomerDto customer) {
        this.returnReceiptId = returnReceiptId;
        this.invoiceId = invoiceId;
        this.refundAmount = refundAmount;
        this.reason = reason;
        this.createdAt = createdAt;
        this.employee = employee;
        this.customer = customer;
    }

    public String getReturnReceiptId() {
        return returnReceiptId;
    }

    public void setReturnReceiptId(String returnReceiptId) {
        this.returnReceiptId = returnReceiptId;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public ReturnReceiptEmployeeDto getEmployee() {
        return employee;
    }

    public void setEmployee(ReturnReceiptEmployeeDto employee) {
        this.employee = employee;
    }

    public ReturnReceiptCustomerDto getCustomer() {
        return customer;
    }

    public void setCustomer(ReturnReceiptCustomerDto customer) {
        this.customer = customer;
    }
}
