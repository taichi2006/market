package com.mycompany.quanlysieuthi.modules.customer;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Response DTO definitions for Customer module.
 */
public class CustomerResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private String customerId;
    private String fullName;
    private String phoneNumber;
    private String email;
    private Integer loyaltyPoints;
    private String createdAt;

    public CustomerResponse() {
    }

    public CustomerResponse(String customerId, String fullName, String phoneNumber, String email,
                            Integer loyaltyPoints, String createdAt) {
        this.customerId = customerId;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.loyaltyPoints = loyaltyPoints;
        this.createdAt = createdAt;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getLoyaltyPoints() {
        return loyaltyPoints;
    }

    public void setLoyaltyPoints(Integer loyaltyPoints) {
        this.loyaltyPoints = loyaltyPoints;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public record EmployeeDto(String employeeId, String fullName) implements Serializable {}

    public record InvoiceHistoryDto(String invoiceId, String createdAt, String status,
                                    BigDecimal originalTotal, BigDecimal discountAmount, BigDecimal finalTotal,
                                    EmployeeDto employee) implements Serializable {}

    public record CustomerSummaryDto(String customerId, String fullName, String phoneNumber, String email,
                                    Integer loyaltyPoints, Long totalOrders, BigDecimal totalSpent,
                                    String lastPurchaseDate, String memberTier) implements Serializable {}

    public record CustomerTransactionDto(String transactionId, String invoiceId, BigDecimal amount,
                                        String paymentMethod, String paymentTime, String status) implements Serializable {}

    public record RedeemPointsResponse(String customerId, String invoiceId, int pointsRedeemed,
                                       BigDecimal discountApplied, String message) implements Serializable {}
}

