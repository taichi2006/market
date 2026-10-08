package com.mycompany.quanlysieuthi.modules.product.dto.request;

import java.io.Serializable;
import java.math.BigDecimal;

public class UpdateProductRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String productName;
    private String categoryId;
    private BigDecimal unitPrice;
    private String productStatus;

    public UpdateProductRequest() {
    }

    public UpdateProductRequest(String productName, String categoryId, BigDecimal unitPrice, String productStatus) {
        this.productName = productName;
        this.categoryId = categoryId;
        this.unitPrice = unitPrice;
        this.productStatus = productStatus;
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
}
