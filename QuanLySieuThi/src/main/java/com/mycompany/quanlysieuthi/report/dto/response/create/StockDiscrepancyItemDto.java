package com.mycompany.quanlysieuthi.report.dto.response.create;

import java.io.Serializable;

/**
 * DTO đại diện cho mặt hàng bị chênh lệch trong response tạo biên bản kiểm kê kho.
 */
public class StockDiscrepancyItemDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String productId;
    private String productName;
    private Integer systemQuantity;
    private Integer actualQuantity;
    private Integer difference;
    private String status;
    private String reason;

    public StockDiscrepancyItemDto() {
    }

    public StockDiscrepancyItemDto(String productId, String productName, Integer systemQuantity,
                                   Integer actualQuantity, Integer difference, String status, String reason) {
        this.productId = productId;
        this.productName = productName;
        this.systemQuantity = systemQuantity;
        this.actualQuantity = actualQuantity;
        this.difference = difference;
        this.status = status;
        this.reason = reason;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getSystemQuantity() {
        return systemQuantity;
    }

    public void setSystemQuantity(Integer systemQuantity) {
        this.systemQuantity = systemQuantity;
    }

    public Integer getActualQuantity() {
        return actualQuantity;
    }

    public void setActualQuantity(Integer actualQuantity) {
        this.actualQuantity = actualQuantity;
    }

    public Integer getDifference() {
        return difference;
    }

    public void setDifference(Integer difference) {
        this.difference = difference;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
