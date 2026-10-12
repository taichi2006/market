package com.mycompany.quanlysieuthi.modules.payment;

import java.io.Serializable;

/**
 * Request payload definitions for Payment module.
 */
public class PaymentRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    public static class Process implements Serializable {
        private static final long serialVersionUID = 1L;

        private String invoiceId;
        private String paymentMethod;

        public Process() {
        }

        public Process(String invoiceId, String paymentMethod) {
            this.invoiceId = invoiceId;
            this.paymentMethod = paymentMethod;
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
    }
}
