package com.mycompany.quanlysieuthi.modules.purchasereceipt.dto.request;

import java.io.Serializable;
import java.math.BigDecimal;

public class PurchaseReceiptItemRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String productName;
    private String categoryId;
    private Integer purchasedQuantity;
    private BigDecimal purchasePrice;
    private String expirationDate;

    public PurchaseReceiptItemRequest() {
    }

    public PurchaseReceiptItemRequest(String productName, String categoryId, Integer purchasedQuantity,
                                      BigDecimal purchasePrice, String expirationDate) {
        this.productName = productName;
        this.categoryId = categoryId;
        this.purchasedQuantity = purchasedQuantity;
        this.purchasePrice = purchasePrice;
        this.expirationDate = expirationDate;
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

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }
}
