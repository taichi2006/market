package com.mycompany.quanlysieuthi.modules.product;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Request payload definitions for Product module.
 */
public class ProductRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    public record Update(String productName, String categoryId,
                         BigDecimal unitPrice, String productStatus) implements Serializable {
        public String getProductName() { return productName; }
        public String getCategoryId() { return categoryId; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public String getProductStatus() { return productStatus; }
    }
}
