package com.mycompany.quanlysieuthi.modules.product;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Response DTO definitions for Product module.
 */
public class ProductResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private String productId;
    private String productName;
    private BigDecimal unitPrice;
    private Integer stockQuantity;
    private String expirationDate;
    private String productStatus;
    private CategoryDto category;

    public ProductResponse() {
    }

    public ProductResponse(String productId, String productName, BigDecimal unitPrice, Integer stockQuantity,
                           String expirationDate, String productStatus, CategoryDto category) {
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.stockQuantity = stockQuantity;
        this.expirationDate = expirationDate;
        this.productStatus = productStatus;
        this.category = category;
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

    public CategoryDto getCategory() {
        return category;
    }

    public void setCategory(CategoryDto category) {
        this.category = category;
    }

    public record CategoryDto(String categoryId, String categoryName) implements Serializable {
        public String getCategoryId() { return categoryId; }
        public String getCategoryName() { return categoryName; }
    }

    public record LowStockDto(String productId, String productName, Integer stockQuantity, CategoryDto category) implements Serializable {
        public String getProductId() { return productId; }
        public String getProductName() { return productName; }
        public Integer getStockQuantity() { return stockQuantity; }
        public CategoryDto getCategory() { return category; }
    }

    public record UpdateResponseDto(String productId, String productName, BigDecimal unitPrice, Integer stockQuantity,
                                    String expirationDate, String productStatus, CategoryDto category) implements Serializable {
        public String getProductId() { return productId; }
        public String getProductName() { return productName; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public Integer getStockQuantity() { return stockQuantity; }
        public String getExpirationDate() { return expirationDate; }
        public String getProductStatus() { return productStatus; }
        public CategoryDto getCategory() { return category; }
    }
}
