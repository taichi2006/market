package com.mycompany.quanlysieuthi.purchasereceipt.dto.response;

import java.io.Serializable;
import java.math.BigDecimal;

public class PurchaseReceiptListItemDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String voucherId;
    private String supplierName;
    private BigDecimal totalAmount;
    private String createdAt;
    private PurchaseReceiptEmployeeDto employee;

    public PurchaseReceiptListItemDto() {
    }

    public PurchaseReceiptListItemDto(String voucherId, String supplierName, BigDecimal totalAmount,
                                      String createdAt, PurchaseReceiptEmployeeDto employee) {
        this.voucherId = voucherId;
        this.supplierName = supplierName;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
        this.employee = employee;
    }

    public String getVoucherId() {
        return voucherId;
    }

    public void setVoucherId(String voucherId) {
        this.voucherId = voucherId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public PurchaseReceiptEmployeeDto getEmployee() {
        return employee;
    }

    public void setEmployee(PurchaseReceiptEmployeeDto employee) {
        this.employee = employee;
    }
}
