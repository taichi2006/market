package com.mycompany.quanlysieuthi.modules.returnreceipt;

import java.io.Serializable;
import java.util.List;

/**
 * Request payload definitions for Return Receipt module.
 */
public class ReturnReceiptRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    public record Create(String invoiceId, String reason, List<Item> items) implements Serializable {
        public String getInvoiceId() { return invoiceId; }
        public String getReason() { return reason; }
        public List<Item> getItems() { return items; }
    }

    public record Item(String productId, Integer returnedQuantity) implements Serializable {
        public String getProductId() { return productId; }
        public Integer getReturnedQuantity() { return returnedQuantity; }
    }
}
