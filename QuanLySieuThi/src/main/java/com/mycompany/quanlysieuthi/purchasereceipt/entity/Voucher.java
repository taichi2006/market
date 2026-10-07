package com.mycompany.quanlysieuthi.purchasereceipt.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity mapping voucher table matching Neon DB schema.
 */
@Entity
@Table(name = "voucher")
public class Voucher implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "voucher_id", length = 36, nullable = false)
    private String voucherId;

    @Column(name = "employee_id", length = 36, nullable = false)
    private String employeeId;

    @Column(name = "total_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public Voucher() {
    }

    public Voucher(String voucherId, String employeeId, BigDecimal totalAmount, LocalDateTime createdAt) {
        this.voucherId = voucherId;
        this.employeeId = employeeId;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public String getVoucherId() {
        return voucherId;
    }

    public void setVoucherId(String voucherId) {
        this.voucherId = voucherId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Voucher voucher = (Voucher) o;
        return Objects.equals(voucherId, voucher.voucherId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(voucherId);
    }
}
