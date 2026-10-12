package com.mycompany.quanlysieuthi.modules.payment.dto;

import java.math.BigDecimal;

/**
 * DTO dữ liệu trả về khi thanh toán tiền mặt thành công hoặc hiển thị kết quả giao dịch.
 */
public class PaymentCashResponseData {

    private String transactionId;   // Mã giao dịch thanh toán
    private String invoiceId;       // Mã hóa đơn
    private String paymentMethod;   // Phương thức thanh toán (CASH)
    private String status;          // Trạng thái thanh toán (PAID)
    private BigDecimal amount;      // Số tiền đã thanh toán
    private String paymentTime;     // Thời điểm thanh toán (ISO-8601)

    public PaymentCashResponseData() {
    }

    public PaymentCashResponseData(String transactionId, String invoiceId, String paymentMethod,
                                  String status, BigDecimal amount, String paymentTime) {
        this.transactionId = transactionId;
        this.invoiceId = invoiceId;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.amount = amount;
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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPaymentTime() {
        return paymentTime;
    }

    public void setPaymentTime(String paymentTime) {
        this.paymentTime = paymentTime;
    }
}
