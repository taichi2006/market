package com.mycompany.quanlysieuthi.customer;

/**
 * Request DTO for creating a new Customer:
 * POST /api/customer
 */
public class CreateCustomerRequest {

    private String fullName;
    private String phoneNumber;
    private String email;

    public CreateCustomerRequest() {
    }

    public CreateCustomerRequest(String fullName, String phoneNumber, String email) {
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.email = email;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
