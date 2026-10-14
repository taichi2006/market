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

    public static class Item implements Serializable {
        private String productId;
        private Integer returnedQuantity;
        private Integer returnQuantity;

        public Item() {
        }

        public Item(String productId, Integer returnedQuantity) {
            this.productId = productId;
            this.returnedQuantity = returnedQuantity;
            this.returnQuantity = returnedQuantity;
        }

        public String getProductId() {
            return productId;
        }

        public void setProductId(String productId) {
            this.productId = productId;
        }

        public Integer getReturnedQuantity() {
            if (returnedQuantity != null) {
                return returnedQuantity;
            }
            return returnQuantity;
        }

        public void setReturnedQuantity(Integer returnedQuantity) {
            this.returnedQuantity = returnedQuantity;
        }

        public Integer getReturnQuantity() {
            return getReturnedQuantity();
        }

        public void setReturnQuantity(Integer returnQuantity) {
            this.returnQuantity = returnQuantity;
        }
    }
}
