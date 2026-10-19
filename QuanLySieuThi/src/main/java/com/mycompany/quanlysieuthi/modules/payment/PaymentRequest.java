package com.mycompany.quanlysieuthi.modules.payment;

import java.io.Serializable;

/**
 * Request payload definitions for Payment module.
 */
public class PaymentRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    public record Process(String invoiceId, String paymentMethod) implements Serializable {
        public String getInvoiceId() { return invoiceId; }
        public String getPaymentMethod() { return paymentMethod; }
    }

    public record CashDirect(String invoiceId, java.math.BigDecimal amount) implements Serializable {
        public String getInvoiceId() { return invoiceId; }
        public java.math.BigDecimal getAmount() { return amount; }
    }
}

