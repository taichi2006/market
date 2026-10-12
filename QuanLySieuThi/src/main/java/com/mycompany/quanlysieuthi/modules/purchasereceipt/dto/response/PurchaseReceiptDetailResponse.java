package com.mycompany.quanlysieuthi.modules.purchasereceipt.dto.response;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public class PurchaseReceiptDetailResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private String voucherId;
    private String supplierName;
    private BigDecimal totalAmount;
    private String createdAt;
    private PurchaseReceiptEmployeeDto employee;
    private List<PurchaseReceiptItemDetailDto> items;

    public PurchaseReceiptDetailResponse() {
    }

    public PurchaseReceiptDetailResponse(String voucherId, String supplierName, BigDecimal totalAmount,
                                         String createdAt, PurchaseReceiptEmployeeDto employee,
                                         List<PurchaseReceiptItemDetailDto> items) {
        this.voucherId = voucherId;
        this.supplierName = supplierName;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
        this.employee = employee;
        this.items = items;
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

    public List<PurchaseReceiptItemDetailDto> getItems() {
        return items;
    }

    public void setItems(List<PurchaseReceiptItemDetailDto> items) {
        this.items = items;
    }
}
