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

    public static class EmployeeDto implements Serializable {
        private static final long serialVersionUID = 1L;

        private String employeeId;
        private String fullName;
        private String username;

        public EmployeeDto() {
        }

        public EmployeeDto(String employeeId, String fullName) {
            this.employeeId = employeeId;
            this.fullName = fullName;
        }

        public EmployeeDto(String employeeId, String fullName, String username) {
            this.employeeId = employeeId;
            this.fullName = fullName;
            this.username = username;
        }

        public String getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(String employeeId) {
            this.employeeId = employeeId;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }
    }

    public static class CreatedProductDto implements Serializable {
        private static final long serialVersionUID = 1L;

        private String productId;
        private String productName;
        private String categoryId;
        private BigDecimal unitPrice;
        private Integer stockQuantity;
        private String expirationDate;
        private String productStatus;

        public CreatedProductDto() {
        }

        public CreatedProductDto(String productId, String productName, String categoryId, BigDecimal unitPrice,
                                 Integer stockQuantity, String expirationDate, String productStatus) {
            this.productId = productId;
            this.productName = productName;
            this.categoryId = categoryId;
            this.unitPrice = unitPrice;
            this.stockQuantity = stockQuantity;
            this.expirationDate = expirationDate;
            this.productStatus = productStatus;
        }

        public String getProductId() {
            return productId;
        }

        public void setProductId(String productId) {
            this.productId = productId;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public String getCategoryId() {
            return categoryId;
        }

        public void setCategoryId(String categoryId) {
            this.categoryId = categoryId;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
        }

        public Integer getStockQuantity() {
            return stockQuantity;
        }

        public void setStockQuantity(Integer stockQuantity) {
            this.stockQuantity = stockQuantity;
        }

        public String getExpirationDate() {
            return expirationDate;
        }

        public void setExpirationDate(String expirationDate) {
            this.expirationDate = expirationDate;
        }

        public String getProductStatus() {
            return productStatus;
        }

        public void setProductStatus(String productStatus) {
            this.productStatus = productStatus;
        }
    }

    public static class ListItemDto implements Serializable {
        private static final long serialVersionUID = 1L;

        private String voucherId;
        private String supplierName;
        private BigDecimal totalAmount;
        private String createdAt;
        private EmployeeDto createdBy;

        public ListItemDto() {
        }

        public ListItemDto(String voucherId, String supplierName, BigDecimal totalAmount, String createdAt, EmployeeDto createdBy) {
            this.voucherId = voucherId;
            this.supplierName = supplierName;
            this.totalAmount = totalAmount;
            this.createdAt = createdAt;
            this.createdBy = createdBy;
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
    }

    public static class ItemDetailDto implements Serializable {
        private static final long serialVersionUID = 1L;

        private String purchaseDetailId;
        private String productId;
        private String productName;
        private String categoryName;
        private Integer purchasedQuantity;
        private BigDecimal purchasePrice;
        private BigDecimal lineTotal;
        private String expirationDate;

        public ItemDetailDto() {
        }

        public ItemDetailDto(String purchaseDetailId, String productId, String productName, String categoryName,
                             Integer purchasedQuantity, BigDecimal purchasePrice, BigDecimal lineTotal, String expirationDate) {
            this.purchaseDetailId = purchaseDetailId;
            this.productId = productId;
            this.productName = productName;
            this.categoryName = categoryName;
            this.purchasedQuantity = purchasedQuantity;
            this.purchasePrice = purchasePrice;
            this.lineTotal = lineTotal;
            this.expirationDate = expirationDate;
        }

        public String getPurchaseDetailId() {
            return purchaseDetailId;
        }

        public void setPurchaseDetailId(String purchaseDetailId) {
            this.purchaseDetailId = purchaseDetailId;
        }

        public String getProductId() {
            return productId;
        }

        public void setProductId(String productId) {
            this.productId = productId;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }

        public Integer getPurchasedQuantity() {
            return purchasedQuantity;
        }

        public void setPurchasedQuantity(Integer purchasedQuantity) {
            this.purchasedQuantity = purchasedQuantity;
        }

        public BigDecimal getPurchasePrice() {
            return purchasePrice;
        }

        public void setPurchasePrice(BigDecimal purchasePrice) {
            this.purchasePrice = purchasePrice;
        }

        public BigDecimal getLineTotal() {
            return lineTotal;
        }

        public void setLineTotal(BigDecimal lineTotal) {
            this.lineTotal = lineTotal;
        }

        public String getExpirationDate() {
            return expirationDate;
        }

        public void setExpirationDate(String expirationDate) {
            this.expirationDate = expirationDate;
        }
    }

    public static class DetailResponse implements Serializable {
        private static final long serialVersionUID = 1L;

        private String voucherId;
        private String supplierName;
        private BigDecimal totalAmount;
        private String createdAt;
        private EmployeeDto createdBy;
        private List<ItemDetailDto> items;

        public DetailResponse() {
        }

        public DetailResponse(String voucherId, String supplierName, BigDecimal totalAmount, String createdAt,
                              EmployeeDto createdBy, List<ItemDetailDto> items) {
            this.voucherId = voucherId;
            this.supplierName = supplierName;
            this.totalAmount = totalAmount;
            this.createdAt = createdAt;
            this.createdBy = createdBy;
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

        public EmployeeDto getCreatedBy() {
            return createdBy;
        }

        public void setCreatedBy(EmployeeDto createdBy) {
            this.createdBy = createdBy;
        }

        public List<ItemDetailDto> getItems() {
            return items;
        }

        public void setItems(List<ItemDetailDto> items) {
            this.items = items;
        }
    }
}
