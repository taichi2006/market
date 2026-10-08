package com.mycompany.quanlysieuthi.modules.returnreceipt.dto.request;

/**
 * DTO representing an item in return receipt request payload.
 */
public class ReturnReceiptItemRequest {

    private String productId;
    private Integer returnQuantity;

    public ReturnReceiptItemRequest() {
    }

    public ReturnReceiptItemRequest(String productId, Integer returnQuantity) {
        this.productId = productId;
        this.returnQuantity = returnQuantity;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Integer getReturnQuantity() {
        return returnQuantity;
    }

    public void setReturnQuantity(Integer returnQuantity) {
        this.returnQuantity = returnQuantity;
    }
}
