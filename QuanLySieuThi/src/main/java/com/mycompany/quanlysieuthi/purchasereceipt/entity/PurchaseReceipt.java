package com.mycompany.quanlysieuthi.purchasereceipt.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.Objects;

/**
 * Entity mapping purchase_receipt table matching Neon DB schema.
 */
@Entity
@Table(name = "purchase_receipt")
public class PurchaseReceipt implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "voucher_id", length = 36, nullable = false)
    private String voucherId;

    @Column(name = "supplier_name", length = 100, nullable = false)
    private String supplierName;

    public PurchaseReceipt() {
    }

    public PurchaseReceipt(String voucherId, String supplierName) {
        this.voucherId = voucherId;
        this.supplierName = supplierName;
    }

    public String getVoucherId() {
        return voucherId;
    }

    public void setVoucherId(String voucherId) {
        this.voucherId = voucherId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PurchaseReceipt that = (PurchaseReceipt) o;
        return Objects.equals(voucherId, that.voucherId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(voucherId);
    }
}
