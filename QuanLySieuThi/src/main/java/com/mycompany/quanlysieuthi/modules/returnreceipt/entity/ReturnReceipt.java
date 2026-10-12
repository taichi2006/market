package com.mycompany.quanlysieuthi.modules.returnreceipt.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * JPA Entity mapping return_receipt table in PostgreSQL.
 */
@Entity
@Table(name = "return_receipt")
public class ReturnReceipt implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "return_receipt_id", length = 36, nullable = false)
    private String returnReceiptId;

    @Column(name = "original_invoice_id", length = 36, nullable = false)
    private String invoiceId;

    @Column(name = "employee_id", length = 36, nullable = false)
    private String employeeId;

    @Column(name = "refund_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal refundAmount;

    @Column(name = "return_reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public ReturnReceipt() {
    }

    public ReturnReceipt(String returnReceiptId, String invoiceId, String employeeId,
                         BigDecimal refundAmount, String reason, LocalDateTime createdAt) {
        this.returnReceiptId = returnReceiptId;
        this.invoiceId = invoiceId;
        this.employeeId = employeeId;
        this.refundAmount = refundAmount;
        this.reason = reason;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
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

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReturnReceipt that = (ReturnReceipt) o;
        return Objects.equals(returnReceiptId, that.returnReceiptId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(returnReceiptId);
    }
}
