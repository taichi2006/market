package com.mycompany.quanlysieuthi.modules.purchasereceipt.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity mapping purchase_receipt_detail table matching Neon DB schema.
 */
@Entity
@Table(name = "purchase_receipt_detail")
public class PurchaseReceiptDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "purchase_detail_id", length = 36, nullable = false)
    private String purchaseDetailId;

    @Column(name = "voucher_id", length = 36, nullable = false)
    private String voucherId;

    @Column(name = "product_id", length = 36, nullable = false)
    private String productId;

    @Column(name = "purchased_quantity", nullable = false)
    private Integer purchasedQuantity;

    @Column(name = "expiration_date", nullable = false)
    private LocalDateTime expirationDate;

    @Column(name = "purchase_price", precision = 12, scale = 2, nullable = false)
    private BigDecimal purchasePrice;

    public PurchaseReceiptDetail() {
    }

    public PurchaseReceiptDetail(String purchaseDetailId, String voucherId, String productId,
                                 Integer purchasedQuantity, LocalDateTime expirationDate, BigDecimal purchasePrice) {
        this.purchaseDetailId = purchaseDetailId;
        this.voucherId = voucherId;
        this.productId = productId;
        this.purchasedQuantity = purchasedQuantity;
        this.expirationDate = expirationDate;
        this.purchasePrice = purchasePrice;
    }

    public String getPurchaseDetailId() {
        return purchaseDetailId;
    }

    public void setPurchaseDetailId(String purchaseDetailId) {
        this.purchaseDetailId = purchaseDetailId;
    }

    public String getVoucherId() {
        return voucherId;
    }

    public void setVoucherId(String voucherId) {
        this.voucherId = voucherId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Integer getPurchasedQuantity() {
        return purchasedQuantity;
    }

    public void setPurchasedQuantity(Integer purchasedQuantity) {
        this.purchasedQuantity = purchasedQuantity;
    }

    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PurchaseReceiptDetail that = (PurchaseReceiptDetail) o;
        return Objects.equals(purchaseDetailId, that.purchaseDetailId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(purchaseDetailId);
    }
}
