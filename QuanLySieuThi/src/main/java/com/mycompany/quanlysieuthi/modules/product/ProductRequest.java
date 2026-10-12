package com.mycompany.quanlysieuthi.modules.product;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Request payload definitions for Product module.
 */
public class ProductRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    public static class Update implements Serializable {
        private static final long serialVersionUID = 1L;

        private String productName;
        private String categoryId;
        private BigDecimal unitPrice;
        private String productStatus;

        public Update() {
        }

        public Update(String productName, String categoryId, BigDecimal unitPrice, String productStatus) {
            this.productName = productName;
            this.categoryId = categoryId;
            this.unitPrice = unitPrice;
            this.productStatus = productStatus;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public String getCategoryId() {
            return categoryId;
        }

        public void setCategoryId(String categoryId) {
            this.categoryId = categoryId;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
        }

        public String getProductStatus() {
            return productStatus;
        }

        public void setProductStatus(String productStatus) {
            this.productStatus = productStatus;
        }
    }
}
