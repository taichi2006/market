package com.mycompany.quanlysieuthi.modules.disposalreceipt;

import java.io.Serializable;
import java.util.List;

/**
 * Request payload definitions for Disposal Receipt module.
 */
public class DisposalReceiptRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Item> items;

    public DisposalReceiptRequest() {
    }

    public DisposalReceiptRequest(List<Item> items) {
        this.items = items;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public record Item(String productId, Integer disposedQuantity, String disposalReason) implements Serializable {
        public Item() {
            this(null, null, null);
        }

        public String getProductId() {
            return productId;
        }

        public Integer getDisposedQuantity() {
            return disposedQuantity;
        }

        public String getDisposalReason() {
            return disposalReason;
        }
    }
}
