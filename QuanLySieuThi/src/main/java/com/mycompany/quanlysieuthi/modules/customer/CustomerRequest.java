package com.mycompany.quanlysieuthi.modules.customer;

import java.io.Serializable;

/**
 * Request payload definitions for Customer module.
 */
public class CustomerRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    public record Create(String fullName, String phoneNumber, String email) implements Serializable {
        public String getFullName() { return fullName; }
        public String getPhoneNumber() { return phoneNumber; }
        public String getEmail() { return email; }
    }

    public record RedeemPoints(String customerId, String invoiceId, Integer points) implements Serializable {
        public String getCustomerId() { return customerId; }
        public String getInvoiceId() { return invoiceId; }
        public Integer getPoints() { return points; }
    }
}

