package com.mycompany.quanlysieuthi.modules.payment.dto;

import java.math.BigDecimal;

/**
 * DTO dữ liệu gửi lên khi yêu cầu thanh toán hóa đơn (POST /api/payment/process).
 */
public class PaymentProcessRequest {

    private String invoiceId;       // Mã hóa đơn cần thanh toán
    private String paymentMethod;   // Phương thức thanh toán ("CASH", "QR", "BANK_TRANSFER")
    private BigDecimal amount;      // Số tiền thanh toán

    public PaymentProcessRequest() {
    }

    public PaymentProcessRequest(String invoiceId, String paymentMethod, BigDecimal amount) {
        this.invoiceId = invoiceId;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
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
}
