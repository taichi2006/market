package com.mycompany.quanlysieuthi.modules.invoice.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class CreateInvoiceResponseData implements Serializable {

    private static final long serialVersionUID = 1L;

    private String invoiceId;
    private String createdAt;
    private String status;
    private String employeeId;
    private String customerId;
    private BigDecimal originalTotal;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private Integer pointsUsed;
    private Integer pointsEarned;
    private Integer customerRemainingPoints;

    public CreateInvoiceResponseData() {
    }

    public CreateInvoiceResponseData(String invoiceId, String createdAt, String status,
                                     String employeeId, String customerId,
                                     BigDecimal originalTotal, BigDecimal discountAmount, BigDecimal finalAmount,
                                     Integer pointsUsed, Integer pointsEarned, Integer customerRemainingPoints) {
        this.invoiceId = invoiceId;
        this.createdAt = createdAt;
        this.status = status;
        this.employeeId = employeeId;
        this.customerId = customerId;
        this.originalTotal = originalTotal;
        this.discountAmount = discountAmount;
        this.finalAmount = finalAmount;
        this.pointsUsed = pointsUsed;
        this.pointsEarned = pointsEarned;
        this.customerRemainingPoints = customerRemainingPoints;
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

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
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

    public Integer getPointsUsed() {
        return pointsUsed;
    }

    public void setPointsUsed(Integer pointsUsed) {
        this.pointsUsed = pointsUsed;
    }

    public Integer getPointsEarned() {
        return pointsEarned;
    }

    public void setPointsEarned(Integer pointsEarned) {
        this.pointsEarned = pointsEarned;
    }

    public Integer getCustomerRemainingPoints() {
        return customerRemainingPoints;
    }

    public void setCustomerRemainingPoints(Integer customerRemainingPoints) {
        this.customerRemainingPoints = customerRemainingPoints;
    }
}
