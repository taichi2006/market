package com.mycompany.quanlysieuthi.modules.returnreceipt;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Response DTO definitions for Return Receipt module.
 */
public class ReturnReceiptResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private String returnReceiptId;
    private String invoiceId;
    private String reason;
    private BigDecimal refundAmount;
    private String createdAt;
    private EmployeeDto createdBy;

    public ReturnReceiptResponse() {
    }

    public ReturnReceiptResponse(String returnReceiptId, String invoiceId, String reason,
                                 BigDecimal refundAmount, String createdAt, EmployeeDto createdBy) {
        this.returnReceiptId = returnReceiptId;
        this.invoiceId = invoiceId;
        this.reason = reason;
        this.refundAmount = refundAmount;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public EmployeeDto getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(EmployeeDto createdBy) {
        this.createdBy = createdBy;
    }

    public record EmployeeDto(String employeeId, String fullName, String username) implements Serializable {
        public EmployeeDto(String employeeId, String fullName) {
            this(employeeId, fullName, null);
        }
    }

    public record CustomerDto(String customerId, String fullName, String phoneNumber) implements Serializable {}

    public record ListItemDto(String returnReceiptId, String invoiceId, BigDecimal refundAmount,
                              String reason, String createdAt, EmployeeDto createdBy) implements Serializable {}

    public record DetailResponse(String returnReceiptId, String invoiceId, BigDecimal refundAmount,
                                 String reason, String createdAt, EmployeeDto createdBy, CustomerDto customer) implements Serializable {}
}
