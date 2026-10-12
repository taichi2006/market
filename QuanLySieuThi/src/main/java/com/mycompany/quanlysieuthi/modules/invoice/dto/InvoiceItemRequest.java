package com.mycompany.quanlysieuthi.modules.invoice.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class InvoiceItemRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String productId;
    private Integer quantity;
    private BigDecimal unitPrice;

    public InvoiceItemRequest() {
    }

    public InvoiceItemRequest(String productId, Integer quantity, BigDecimal unitPrice) {
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
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

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }
}
