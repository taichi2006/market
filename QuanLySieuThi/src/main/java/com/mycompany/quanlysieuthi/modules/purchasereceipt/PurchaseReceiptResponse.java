package com.mycompany.quanlysieuthi.modules.purchasereceipt;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO definitions for Purchase Receipt module.
 */
public class PurchaseReceiptResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private String voucherId;
    private String supplierName;
    private BigDecimal totalAmount;
    private String createdAt;
    private EmployeeDto createdBy;
    private List<CreatedProductDto> createdProducts;

    public PurchaseReceiptResponse() {
    }

    public PurchaseReceiptResponse(String voucherId, String supplierName, BigDecimal totalAmount,
                                   String createdAt, EmployeeDto createdBy, List<CreatedProductDto> createdProducts) {
        this.voucherId = voucherId;
        this.supplierName = supplierName;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
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

    public EmployeeDto getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(EmployeeDto createdBy) {
        this.createdBy = createdBy;
    }

    public List<CreatedProductDto> getCreatedProducts() {
        return createdProducts;
    }

    public void setCreatedProducts(List<CreatedProductDto> createdProducts) {
        this.createdProducts = createdProducts;
    }

    public record EmployeeDto(String employeeId, String fullName, String username) implements Serializable {
        public EmployeeDto(String employeeId, String fullName) {
            this(employeeId, fullName, null);
        }
        public String getEmployeeId() { return employeeId; }
        public String getFullName() { return fullName; }
        public String getUsername() { return username; }
    }

    public record CreatedProductDto(String productId, String productName, String categoryId, BigDecimal unitPrice,
                                    Integer stockQuantity, String expirationDate, String productStatus) implements Serializable {
        public String getProductId() { return productId; }
        public String getProductName() { return productName; }
        public String getCategoryId() { return categoryId; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public Integer getStockQuantity() { return stockQuantity; }
        public String getExpirationDate() { return expirationDate; }
        public String getProductStatus() { return productStatus; }
    }

    public record ListItemDto(String voucherId, String supplierName, BigDecimal totalAmount, String createdAt,
                              EmployeeDto createdBy) implements Serializable {
        public String getVoucherId() { return voucherId; }
        public String getSupplierName() { return supplierName; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public String getCreatedAt() { return createdAt; }
        public EmployeeDto getCreatedBy() { return createdBy; }
    }

    public record ItemDetailDto(String purchaseDetailId, String productId, String productName, String categoryName,
                                Integer purchasedQuantity, BigDecimal purchasePrice, BigDecimal lineTotal, String expirationDate) implements Serializable {
        public String getPurchaseDetailId() { return purchaseDetailId; }
        public String getProductId() { return productId; }
        public String getProductName() { return productName; }
        public String getCategoryName() { return categoryName; }
        public Integer getPurchasedQuantity() { return purchasedQuantity; }
        public BigDecimal getPurchasePrice() { return purchasePrice; }
        public BigDecimal getLineTotal() { return lineTotal; }
        public String getExpirationDate() { return expirationDate; }
    }

    public record DetailResponse(String voucherId, String supplierName, BigDecimal totalAmount, String createdAt,
                                 EmployeeDto createdBy, List<ItemDetailDto> items) implements Serializable {
        public String getVoucherId() { return voucherId; }
        public String getSupplierName() { return supplierName; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public String getCreatedAt() { return createdAt; }
        public EmployeeDto getCreatedBy() { return createdBy; }
        public List<ItemDetailDto> getItems() { return items; }
    }
}
