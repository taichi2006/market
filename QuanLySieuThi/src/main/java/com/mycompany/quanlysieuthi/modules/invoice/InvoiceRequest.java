package com.mycompany.quanlysieuthi.modules.invoice;

import java.io.Serializable;
import java.util.List;

/**
 * Request payload definitions for Invoice module.
 */
public class InvoiceRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    public static class Create implements Serializable {
        private static final long serialVersionUID = 1L;

        private String customerId;
        private Boolean useLoyaltyPoints;
        private List<Item> items;

        public Create() {
        }

        public Create(String customerId, Boolean useLoyaltyPoints, List<Item> items) {
            this.customerId = customerId;
            this.useLoyaltyPoints = useLoyaltyPoints;
            this.items = items;
        }

        public String getCustomerId() {
            return customerId;
        }

        public void setCustomerId(String customerId) {
            this.customerId = customerId;
        }

        public Boolean getUseLoyaltyPoints() {
            return useLoyaltyPoints;
        }

        public void setUseLoyaltyPoints(Boolean useLoyaltyPoints) {
            this.useLoyaltyPoints = useLoyaltyPoints;
        }

        public List<Item> getItems() {
            return items;
        }

        public void setItems(List<Item> items) {
            this.items = items;
        }
    }

    public static class Item implements Serializable {
        private static final long serialVersionUID = 1L;

        private String productId;
        private Integer quantity;

        public Item() {
        }

        public Item(String productId, Integer quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }

        public String getProductId() {
            return productId;
        }

        public void setProductId(String productId) {
            this.productId = productId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }
}
