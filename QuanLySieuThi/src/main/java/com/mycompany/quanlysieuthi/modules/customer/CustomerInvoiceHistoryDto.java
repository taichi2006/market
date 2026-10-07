package com.mycompany.quanlysieuthi.modules.customer;

import java.math.BigDecimal;

/**
 * Invoice history item response DTO for a customer:
 * GET /api/customer/:id/invoices
 */
public class CustomerInvoiceHistoryDto {

    private String invoiceId;
    private String createdAt;
    private String status;
    private BigDecimal originalTotal;
    private BigDecimal discountAmount;
    private BigDecimal finalTotal;
    private CustomerInvoiceEmployeeDto employee;

    public CustomerInvoiceHistoryDto() {
    }

    public CustomerInvoiceHistoryDto(String invoiceId, String createdAt, String status,
                                     BigDecimal originalTotal, BigDecimal discountAmount,
                                     BigDecimal finalTotal, CustomerInvoiceEmployeeDto employee) {
        this.invoiceId = invoiceId;
        this.createdAt = createdAt;
        this.status = status;
        this.originalTotal = originalTotal;
        this.discountAmount = discountAmount;
        this.finalTotal = finalTotal;
        this.employee = employee;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getOriginalTotal() {
        return originalTotal;
    }

    public void setOriginalTotal(BigDecimal originalTotal) {
        this.originalTotal = originalTotal;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getFinalTotal() {
        return finalTotal;
    }

    public void setFinalTotal(BigDecimal finalTotal) {
        this.finalTotal = finalTotal;
    }

    public CustomerInvoiceEmployeeDto getEmployee() {
        return employee;
    }

    public void setEmployee(CustomerInvoiceEmployeeDto employee) {
        this.employee = employee;
    }
}
