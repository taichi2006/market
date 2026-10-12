package com.mycompany.quanlysieuthi.modules.payment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Thực thể JPA ánh xạ trực tiếp tới bảng payment trong PostgreSQL.
 */
@Entity
@Table(name = "payment")
public class Payment implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "transaction_id", length = 36, nullable = false)
    private String transactionId; // Mã giao dịch (UUID)

    @Column(name = "invoice_id", length = 36)
    private String invoiceId; // Mã hóa đơn liên kết

    @Column(name = "amount", precision = 12, scale = 2)
    private BigDecimal amount; // Số tiền thanh toán

    @Column(name = "payment_method", length = 30)
    private String paymentMethod; // Phương thức: CASH, BANK_TRANSFER, QR

    @Column(name = "status", length = 30)
    private String status; // Trạng thái: UNPAID, PAID, FAILED

    @Column(name = "payment_time")
    private LocalDateTime paymentTime; // Thời gian ghi nhận thanh toán

    public Payment() {
    }

    public Payment(String transactionId, String invoiceId, BigDecimal amount,
                   String paymentMethod, String status, LocalDateTime paymentTime) {
        this.transactionId = transactionId;
        this.invoiceId = invoiceId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.paymentTime = paymentTime;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getPaymentTime() {
        return paymentTime;
    }

    public void setPaymentTime(LocalDateTime paymentTime) {
        this.paymentTime = paymentTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Payment payment = (Payment) o;
        return Objects.equals(transactionId, payment.transactionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId);
    }
}
