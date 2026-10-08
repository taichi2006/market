package com.mycompany.quanlysieuthi.modules.product.dto.response;

import java.io.Serializable;

public class ProductResponseDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String productId;
    private String productName;
    private Integer stockQuantity;
    private String expirationDate;
    private String productStatus;
    private ProductCategoryDto category;

    public ProductResponseDto() {
    }

    public ProductResponseDto(String productId, String productName, Integer stockQuantity,
                              String expirationDate, String productStatus, ProductCategoryDto category) {
        this.productId = productId;
        this.productName = productName;
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

    public ProductCategoryDto getCategory() {
        return category;
    }

    public void setCategory(ProductCategoryDto category) {
        this.category = category;
    }
}
