package com.mycompany.quanlysieuthi.modules.report.dto.request;

import java.io.Serializable;

/**
 * DTO đại diện cho một mặt hàng kiểm kê gửi lên từ client.
 */
public class StockAuditItemRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String productId;
    private Integer actualQuantity;
    private String reason;

    public StockAuditItemRequest() {
    }

    public StockAuditItemRequest(String productId, Integer actualQuantity, String reason) {
        this.productId = productId;
        this.actualQuantity = actualQuantity;
        this.reason = reason;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Integer getActualQuantity() {
        return actualQuantity;
    }

    public void setActualQuantity(Integer actualQuantity) {
        this.actualQuantity = actualQuantity;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
