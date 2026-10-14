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

    public record Item(String productId, String productName, String categoryId, BigDecimal unitPrice,
                       Integer purchasedQuantity, BigDecimal purchasePrice, String expirationDate) implements Serializable {
        public Item() {
            this(null, null, null, null, null, null, null);
        }
        public String getProductId() { return productId; }
        public String getProductName() { return productName; }
        public String getCategoryId() { return categoryId; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public Integer getPurchasedQuantity() { return purchasedQuantity; }
        public BigDecimal getPurchasePrice() { return purchasePrice; }
        public String getExpirationDate() { return expirationDate; }
    }
}
