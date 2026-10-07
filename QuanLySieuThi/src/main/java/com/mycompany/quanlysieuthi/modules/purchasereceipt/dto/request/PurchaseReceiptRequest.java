package com.mycompany.quanlysieuthi.modules.purchasereceipt.dto.request;

import java.io.Serializable;
import java.util.List;

public class PurchaseReceiptRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String supplierName;
    private List<PurchaseReceiptItemRequest> items;

    public PurchaseReceiptRequest() {
    }

    public PurchaseReceiptRequest(String supplierName, List<PurchaseReceiptItemRequest> items) {
        this.supplierName = supplierName;
        this.items = items;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public List<PurchaseReceiptItemRequest> getItems() {
        return items;
    }

    public void setItems(List<PurchaseReceiptItemRequest> items) {
        this.items = items;
    }
}
