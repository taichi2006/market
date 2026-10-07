package com.mycompany.quanlysieuthi.modules.purchasereceipt.dto.response;

import java.io.Serializable;
import java.math.BigDecimal;

public class PurchaseReceiptItemDetailDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String purchaseDetailId;
    private String productId;
    private String productName;
    private String categoryName;
    private Integer purchasedQuantity;
    private BigDecimal purchasePrice;
    private BigDecimal lineTotal;
    private String expirationDate;

    public PurchaseReceiptItemDetailDto() {
    }

    public PurchaseReceiptItemDetailDto(String purchaseDetailId, String productId, String productName,
                                        String categoryName, Integer purchasedQuantity, BigDecimal purchasePrice,
                                        BigDecimal lineTotal, String expirationDate) {
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
