package com.mycompany.quanlysieuthi.modules.purchasereceipt;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Request payload definitions for Purchase Receipt module.
 */
public class PurchaseReceiptRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String supplierName;
    private List<Item> items;

    public PurchaseReceiptRequest() {
    }

    public PurchaseReceiptRequest(String supplierName, List<Item> items) {
        this.supplierName = supplierName;
        this.items = items;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public static class Item implements Serializable {
        private static final long serialVersionUID = 1L;

        private String productId;
        private String productName;
        private String categoryId;
        private BigDecimal unitPrice;
        private Integer purchasedQuantity;
        private BigDecimal purchasePrice;
        private String expirationDate;

        public Item() {
        }

        public Item(String productId, String productName, String categoryId, BigDecimal unitPrice,
                    Integer purchasedQuantity, BigDecimal purchasePrice, String expirationDate) {
            this.productId = productId;
            this.productName = productName;
            this.categoryId = categoryId;
            this.unitPrice = unitPrice;
            this.purchasedQuantity = purchasedQuantity;
            this.purchasePrice = purchasePrice;
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
}
