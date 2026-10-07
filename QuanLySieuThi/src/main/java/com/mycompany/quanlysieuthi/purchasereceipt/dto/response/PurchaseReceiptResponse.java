package com.mycompany.quanlysieuthi.purchasereceipt.dto.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO representing the response payload for POST /api/purchase-receipt.
 */
public class PurchaseReceiptResponse {

    private String voucherId;
    private String supplierName;
    private BigDecimal totalAmount;
    private String createdAt;
    private CreatedEmployeeDto createdEmployee;
    private List<CreatedProductDto> createdProducts;

    public PurchaseReceiptResponse() {
    }

    public PurchaseReceiptResponse(String voucherId, String supplierName, BigDecimal totalAmount,
                                   String createdAt, CreatedEmployeeDto createdEmployee,
                                   List<CreatedProductDto> createdProducts) {
        this.voucherId = voucherId;
        this.supplierName = supplierName;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
        this.createdEmployee = createdEmployee;
        this.createdProducts = createdProducts;
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

    public CreatedEmployeeDto getCreatedEmployee() {
        return createdEmployee;
    }

    public void setCreatedEmployee(CreatedEmployeeDto createdEmployee) {
        this.createdEmployee = createdEmployee;
    }

    public List<CreatedProductDto> getCreatedProducts() {
        return createdProducts;
    }

    public void setCreatedProducts(List<CreatedProductDto> createdProducts) {
        this.createdProducts = createdProducts;
    }
}
