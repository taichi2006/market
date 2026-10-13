package com.mycompany.quanlysieuthi.modules.category;

import java.io.Serializable;

/**
 * Response DTO for Category module.
 */
public class CategoryResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private String categoryId;
    private String categoryName;
    private Long productCount;

    public CategoryResponse() {
    }

    public CategoryResponse(String categoryId, String categoryName, Long productCount) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.productCount = productCount != null ? productCount : 0L;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Long getProductCount() {
        return productCount;
    }

    public void setProductCount(Long productCount) {
        this.productCount = productCount;
    }
}
