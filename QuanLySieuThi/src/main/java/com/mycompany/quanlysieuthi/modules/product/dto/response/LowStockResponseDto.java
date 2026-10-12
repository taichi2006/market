package com.mycompany.quanlysieuthi.modules.product.dto.response;

import java.io.Serializable;
import java.util.List;

public class LowStockResponseDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer threshold;
    private Long totalItems;
    private List<ProductResponseDto> items;

    public LowStockResponseDto() {
    }

    public LowStockResponseDto(Integer threshold, Long totalItems, List<ProductResponseDto> items) {
        this.threshold = threshold;
        this.totalItems = totalItems;
        this.items = items;
    }

    public Integer getThreshold() {
        return threshold;
    }

    public void setThreshold(Integer threshold) {
        this.threshold = threshold;
    }

    public Long getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(Long totalItems) {
        this.totalItems = totalItems;
    }

    public List<ProductResponseDto> getItems() {
        return items;
    }

    public void setItems(List<ProductResponseDto> items) {
        this.items = items;
    }
}
