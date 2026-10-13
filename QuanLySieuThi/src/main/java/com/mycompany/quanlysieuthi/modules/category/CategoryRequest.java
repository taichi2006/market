package com.mycompany.quanlysieuthi.modules.category;

import java.io.Serializable;

/**
 * Request DTO for Category creation and update.
 */
public class CategoryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String categoryId;
    private String categoryName;

    public CategoryRequest() {
    }

    public CategoryRequest(String categoryId, String categoryName) {
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
