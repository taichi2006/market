package com.mycompany.quanlysieuthi.modules.returnreceipt;

import java.io.Serializable;
import java.util.List;

/**
 * Request payload definitions for Return Receipt module.
 */
public class ReturnReceiptRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    public static class Create implements Serializable {
        private static final long serialVersionUID = 1L;

        private String invoiceId;
        private String reason;
        private List<Item> items;

        public Create() {
        }

        public Create(String invoiceId, String reason, List<Item> items) {
            this.invoiceId = invoiceId;
            this.reason = reason;
            this.items = items;
        }

        public String getInvoiceId() {
            return invoiceId;
        }

        public void setInvoiceId(String invoiceId) {
            this.invoiceId = invoiceId;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
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
        private Integer returnedQuantity;

        public Item() {
        }

        public Item(String productId, Integer returnedQuantity) {
            this.productId = productId;
            this.returnedQuantity = returnedQuantity;
        }

        public String getProductId() {
            return productId;
        }

        public void setProductId(String productId) {
            this.productId = productId;
        }

        public Integer getReturnedQuantity() {
            return returnedQuantity;
        }

        public void setReturnedQuantity(Integer returnedQuantity) {
            this.returnedQuantity = returnedQuantity;
        }
    }
}
