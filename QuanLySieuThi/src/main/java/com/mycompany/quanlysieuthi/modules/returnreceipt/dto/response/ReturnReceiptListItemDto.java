package com.mycompany.quanlysieuthi.modules.returnreceipt.dto.response;

import java.math.BigDecimal;

/**
 * DTO for items in return receipt list response (GET /api/return-receipt).
 */
public class ReturnReceiptListItemDto {

    private String returnReceiptId;
    private String invoiceId;
    private BigDecimal refundAmount;
    private String reason;
    private String createdAt;
    private ReturnReceiptEmployeeDto employee;

    public ReturnReceiptListItemDto() {
    }

    public ReturnReceiptListItemDto(String returnReceiptId, String invoiceId, BigDecimal refundAmount,
                                    String reason, String createdAt, ReturnReceiptEmployeeDto employee) {
        this.returnReceiptId = returnReceiptId;
        this.invoiceId = invoiceId;
        this.refundAmount = refundAmount;
        this.reason = reason;
        this.createdAt = createdAt;
        this.employee = employee;
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
}
