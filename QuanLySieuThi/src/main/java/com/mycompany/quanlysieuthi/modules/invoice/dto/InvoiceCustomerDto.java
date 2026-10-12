package com.mycompany.quanlysieuthi.modules.invoice.dto;

import java.io.Serializable;

public class InvoiceCustomerDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String customerId;
    private String fullName;
    private String phoneNumber;
    private Integer loyaltyPoints;

    public InvoiceCustomerDto() {
    }

    public InvoiceCustomerDto(String customerId, String fullName, String phoneNumber) {
        this.customerId = customerId;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
    }

    public InvoiceCustomerDto(String customerId, String fullName, String phoneNumber, Integer loyaltyPoints) {
        this.customerId = customerId;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.loyaltyPoints = loyaltyPoints;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Integer getLoyaltyPoints() {
        return loyaltyPoints;
    }

    public void setLoyaltyPoints(Integer loyaltyPoints) {
        this.loyaltyPoints = loyaltyPoints;
    }
}
