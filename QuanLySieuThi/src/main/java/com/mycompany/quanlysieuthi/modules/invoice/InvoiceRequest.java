package com.mycompany.quanlysieuthi.modules.invoice;

import java.io.Serializable;
import java.util.List;

/**
 * Request payload definitions for Invoice module.
 */
public class InvoiceRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    public static class Create implements Serializable {
        private String customerId;
        private Boolean useLoyaltyPoints;
        private Boolean usePoints;
        private List<Item> items;

        public Create() {
        }

        public Create(String customerId, Boolean useLoyaltyPoints, List<Item> items) {
            this.customerId = customerId;
            this.useLoyaltyPoints = useLoyaltyPoints;
            this.usePoints = useLoyaltyPoints;
            this.items = items;
        }

        public String getCustomerId() {
            return customerId;
        }

        public void setCustomerId(String customerId) {
            this.customerId = customerId;
        }

        public Boolean getUseLoyaltyPoints() {
            if (Boolean.TRUE.equals(useLoyaltyPoints)) {
                return true;
            }
            if (Boolean.TRUE.equals(usePoints)) {
                return true;
            }
            return false;
        }

        public void setUseLoyaltyPoints(Boolean useLoyaltyPoints) {
            this.useLoyaltyPoints = useLoyaltyPoints;
        }

        public Boolean getUsePoints() {
            return getUseLoyaltyPoints();
        }

        public void setUsePoints(Boolean usePoints) {
            this.usePoints = usePoints;
        }

        public List<Item> getItems() {
            return items;
        }

        public void setItems(List<Item> items) {
            this.items = items;
        }
    }

    public record Item(String productId, Integer quantity) implements Serializable {
        public String getProductId() { return productId; }
        public Integer getQuantity() { return quantity; }
    }
}
