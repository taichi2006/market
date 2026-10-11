package com.mycompany.quanlysieuthi.modules.invoice.dto;

import java.io.Serializable;
import java.util.List;

public class CreateInvoiceRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String customerId;
    private Boolean usePoints;
    private Integer pointsToUse;
    private List<InvoiceItemRequest> items;

    public CreateInvoiceRequest() {
    }

    public CreateInvoiceRequest(String customerId, Boolean usePoints, Integer pointsToUse, List<InvoiceItemRequest> items) {
        this.customerId = customerId;
        this.usePoints = usePoints;
        this.pointsToUse = pointsToUse;
        this.items = items;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public Boolean getUsePoints() {
        return usePoints;
    }

    public void setUsePoints(Boolean usePoints) {
        this.usePoints = usePoints;
    }

    public Integer getPointsToUse() {
        return pointsToUse;
    }

    public void setPointsToUse(Integer pointsToUse) {
        this.pointsToUse = pointsToUse;
    }

    public List<InvoiceItemRequest> getItems() {
        return items;
    }

    public void setItems(List<InvoiceItemRequest> items) {
        this.items = items;
    }
}
