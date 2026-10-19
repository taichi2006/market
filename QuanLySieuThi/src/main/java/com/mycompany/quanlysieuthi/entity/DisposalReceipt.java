package com.mycompany.quanlysieuthi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.Objects;

/**
 * Entity mapping disposal_receipt table matching Neon DB schema.
 */
@Entity
@Table(name = "disposal_receipt")
public class DisposalReceipt implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "voucher_id", length = 36, nullable = false)
    private String voucherId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id", referencedColumnName = "voucher_id", insertable = false, updatable = false)
    private Voucher voucher;

    public DisposalReceipt() {
    }

    public DisposalReceipt(String voucherId) {
        this.voucherId = voucherId;
    }

    public String getVoucherId() {
        return voucherId;
    }

    public void setVoucherId(String voucherId) {
        this.voucherId = voucherId;
    }

    public Voucher getVoucher() {
        return voucher;
    }

    public void setVoucher(Voucher voucher) {
        this.voucher = voucher;
        if (voucher != null) {
            this.voucherId = voucher.getVoucherId();
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DisposalReceipt that = (DisposalReceipt) o;
        return Objects.equals(voucherId, that.voucherId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(voucherId);
    }
}
