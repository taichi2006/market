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

    public static class CategoryDto implements Serializable {
        private static final long serialVersionUID = 1L;

        private String categoryId;
        private String categoryName;

        public CategoryDto() {
        }

        public CategoryDto(String categoryId, String categoryName) {
            this.categoryId = categoryId;
            this.categoryName = categoryName;
        }

        public String getCategoryId() {
            return categoryId;
        }

        public void setCategoryId(String categoryId) {
            this.categoryId = categoryId;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }
    }

    public static class LowStockDto implements Serializable {
        private static final long serialVersionUID = 1L;

        private String productId;
        private String productName;
        private Integer stockQuantity;
        private CategoryDto category;

        public LowStockDto() {
        }

        public LowStockDto(String productId, String productName, Integer stockQuantity, CategoryDto category) {
            this.productId = productId;
            this.productName = productName;
            this.stockQuantity = stockQuantity;
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

        public Integer getStockQuantity() {
            return stockQuantity;
        }

        public void setStockQuantity(Integer stockQuantity) {
            this.stockQuantity = stockQuantity;
        }

        public CategoryDto getCategory() {
            return category;
        }

        public void setCategory(CategoryDto category) {
            this.category = category;
        }
    }

    public static class UpdateResponseDto implements Serializable {
        private static final long serialVersionUID = 1L;

        private String productId;
        private String productName;
        private BigDecimal unitPrice;
        private Integer stockQuantity;
        private String expirationDate;
        private String productStatus;
        private CategoryDto category;

        public UpdateResponseDto() {
        }

        public UpdateResponseDto(String productId, String productName, BigDecimal unitPrice, Integer stockQuantity,
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
    }
}
