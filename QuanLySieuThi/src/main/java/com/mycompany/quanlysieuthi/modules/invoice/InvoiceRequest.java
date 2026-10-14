package com.mycompany.quanlysieuthi.modules.invoice;

import java.io.Serializable;
import java.util.List;

/**
 * Request payload definitions for Invoice module.
 */
public class InvoiceRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    public record Create(String customerId, Boolean useLoyaltyPoints, List<Item> items) implements Serializable {
        public String getCustomerId() { return customerId; }
        public Boolean getUseLoyaltyPoints() { return useLoyaltyPoints; }
        public List<Item> getItems() { return items; }
    }

    public record Item(String productId, Integer quantity) implements Serializable {
        public String getProductId() { return productId; }
        public Integer getQuantity() { return quantity; }
    }
}
