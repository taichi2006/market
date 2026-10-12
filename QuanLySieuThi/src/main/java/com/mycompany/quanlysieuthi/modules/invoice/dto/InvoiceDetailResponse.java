package com.mycompany.quanlysieuthi.modules.invoice.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public class InvoiceDetailResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private String invoiceId;
    private String createdAt;
    private String status;
    private BigDecimal originalTotal;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private InvoiceEmployeeDto employee;
    private InvoiceCustomerDto customer;
    private List<InvoiceItemDetailDto> items;

    public InvoiceDetailResponse() {
    }

    public InvoiceDetailResponse(String invoiceId, String createdAt, String status,
                                 BigDecimal originalTotal, BigDecimal discountAmount, BigDecimal finalAmount,
                                 InvoiceEmployeeDto employee, InvoiceCustomerDto customer,
                                 List<InvoiceItemDetailDto> items) {
        this.invoiceId = invoiceId;
        this.createdAt = createdAt;
        this.status = status;
        this.originalTotal = originalTotal;
        this.discountAmount = discountAmount;
        this.finalAmount = finalAmount;
        this.employee = employee;
        this.customer = customer;
        this.items = items;
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

    public BigDecimal getFinalAmount() {
        return finalAmount;
    }

    public void setFinalAmount(BigDecimal finalAmount) {
        this.finalAmount = finalAmount;
    }

    public InvoiceEmployeeDto getEmployee() {
        return employee;
    }

    public void setEmployee(InvoiceEmployeeDto employee) {
        this.employee = employee;
    }

    public InvoiceCustomerDto getCustomer() {
        return customer;
    }

    public void setCustomer(InvoiceCustomerDto customer) {
        this.customer = customer;
    }

    public List<InvoiceItemDetailDto> getItems() {
        return items;
    }

    public void setItems(List<InvoiceItemDetailDto> items) {
        this.items = items;
    }
}
