package com.mycompany.quanlysieuthi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.Objects;

/**
 * Entity mapping disposal_receipt_detail table matching Neon DB schema.
 */
@Entity
@Table(name = "disposal_receipt_detail")
public class DisposalReceiptDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "disposal_detail_id", length = 36, nullable = false)
    private String disposalDetailId;

    @Column(name = "voucher_id", length = 36, nullable = false)
    private String voucherId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id", referencedColumnName = "voucher_id", insertable = false, updatable = false)
    private DisposalReceipt disposalReceipt;

    @Column(name = "product_id", length = 36, nullable = false)
    private String productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", referencedColumnName = "product_id", insertable = false, updatable = false)
    private Product product;

    @Column(name = "disposed_quantity", nullable = false)
    private Integer disposedQuantity;

    @Column(name = "disposal_reason", columnDefinition = "TEXT")
    private String disposalReason;

    public DisposalReceiptDetail() {
    }

    public DisposalReceiptDetail(String disposalDetailId, String voucherId, String productId,
                                 Integer disposedQuantity, String disposalReason) {
        this.disposalDetailId = disposalDetailId;
        this.voucherId = voucherId;
        this.productId = productId;
        this.disposedQuantity = disposedQuantity;
        this.disposalReason = disposalReason;
    }

    public String getDisposalDetailId() {
        return disposalDetailId;
    }

    public void setDisposalDetailId(String disposalDetailId) {
        this.disposalDetailId = disposalDetailId;
    }

    public String getVoucherId() {
        return voucherId;
    }

    public void setVoucherId(String voucherId) {
        this.voucherId = voucherId;
    }

    public DisposalReceipt getDisposalReceipt() {
        return disposalReceipt;
    }

    public void setDisposalReceipt(DisposalReceipt disposalReceipt) {
        this.disposalReceipt = disposalReceipt;
        if (disposalReceipt != null) {
            this.voucherId = disposalReceipt.getVoucherId();
        }
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
        if (product != null) {
            this.productId = product.getProductId();
        }
    }


    public Integer getDisposedQuantity() {
        return disposedQuantity;
    }

    public void setDisposedQuantity(Integer disposedQuantity) {
        this.disposedQuantity = disposedQuantity;
    }

    public String getDisposalReason() {
        return disposalReason;
    }

    public void setDisposalReason(String disposalReason) {
        this.disposalReason = disposalReason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DisposalReceiptDetail that = (DisposalReceiptDetail) o;
        return Objects.equals(disposalDetailId, that.disposalDetailId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(disposalDetailId);
    }
}
