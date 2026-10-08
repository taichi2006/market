package com.mycompany.quanlysieuthi.modules.returnreceipt.dto.response;

import java.math.BigDecimal;

/**
 * DTO representing POST /api/return-receipt response data payload.
 */
public class ReturnReceiptResponse {

    private String returnReceiptId;
    private String invoiceId;
    private String createdAt;
    private BigDecimal refundAmount;
    private Integer pointsDeducted;
    private ReturnReceiptEmployeeDto employee;
    private ReturnReceiptCustomerDto customer;

    public ReturnReceiptResponse() {
    }

    public ReturnReceiptResponse(String returnReceiptId, String invoiceId, String createdAt,
                                 BigDecimal refundAmount, Integer pointsDeducted,
                                 ReturnReceiptEmployeeDto employee, ReturnReceiptCustomerDto customer) {
        this.returnReceiptId = returnReceiptId;
        this.invoiceId = invoiceId;
        this.createdAt = createdAt;
        this.refundAmount = refundAmount;
        this.pointsDeducted = pointsDeducted;
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

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }

    public Integer getPointsDeducted() {
        return pointsDeducted;
    }

    public void setPointsDeducted(Integer pointsDeducted) {
        this.pointsDeducted = pointsDeducted;
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
