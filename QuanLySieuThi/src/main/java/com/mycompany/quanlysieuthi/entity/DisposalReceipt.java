package com.mycompany.quanlysieuthi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
