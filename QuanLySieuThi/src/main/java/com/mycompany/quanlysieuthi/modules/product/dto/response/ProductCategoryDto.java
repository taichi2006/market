package com.mycompany.quanlysieuthi.modules.product.dto.response;

import java.io.Serializable;

public class ProductCategoryDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String categoryId;
    private String categoryName;

    public ProductCategoryDto() {
    }

    public ProductCategoryDto(String categoryId, String categoryName) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
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
}
