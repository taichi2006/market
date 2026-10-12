package com.mycompany.quanlysieuthi.modules.product.dto.response;

import java.io.Serializable;
import java.math.BigDecimal;

public class UpdateProductResponseDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String productId;
    private String productName;
    private String categoryId;
    private BigDecimal unitPrice;
    private String productStatus;
    private Integer stockQuantity;
    private String expirationDate;

    public UpdateProductResponseDto() {
    }

    public UpdateProductResponseDto(String productId, String productName, String categoryId,
                                    BigDecimal unitPrice, String productStatus,
                                    Integer stockQuantity, String expirationDate) {
        this.productId = productId;
        this.productName = productName;
        this.categoryId = categoryId;
        this.unitPrice = unitPrice;
        this.productStatus = productStatus;
        this.stockQuantity = stockQuantity;
        this.expirationDate = expirationDate;
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

    public String getProductStatus() {
        return productStatus;
    }

    public void setProductStatus(String productStatus) {
        this.productStatus = productStatus;
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
}
