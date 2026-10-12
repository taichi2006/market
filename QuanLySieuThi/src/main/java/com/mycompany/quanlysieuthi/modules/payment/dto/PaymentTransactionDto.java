package com.mycompany.quanlysieuthi.modules.payment.dto;

import java.math.BigDecimal;

/**
 * DTO đại diện cho một bản ghi giao dịch trong danh sách lịch sử thanh toán.
 */
public class PaymentTransactionDto {

    private String transactionId;   // Mã định danh giao dịch
    private String invoiceId;       // Mã hóa đơn tương ứng
    private String paymentMethod;   // Phương thức thanh toán (CASH, QR, BANK_TRANSFER)
    private BigDecimal amount;      // Số tiền thanh toán
    private String status;          // Trạng thái (UNPAID, PAID, FAILED)
    private String paymentTime;     // Thời điểm thanh toán (ISO-8601)
    private String cashierName;     // Họ tên nhân viên thu ngân phụ trách hóa đơn

    public PaymentTransactionDto() {
    }

    public PaymentTransactionDto(String transactionId, String invoiceId, String paymentMethod,
                                 BigDecimal amount, String status, String paymentTime, String cashierName) {
        this.transactionId = transactionId;
        this.invoiceId = invoiceId;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.status = status;
        this.paymentTime = paymentTime;
        this.cashierName = cashierName;
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

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentTime() {
        return paymentTime;
    }

    public void setPaymentTime(String paymentTime) {
        this.paymentTime = paymentTime;
    }

    public String getCashierName() {
        return cashierName;
    }

    public void setCashierName(String cashierName) {
        this.cashierName = cashierName;
    }
}
