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

    public static class EmployeeDto implements Serializable {
        private static final long serialVersionUID = 1L;

        private String employeeId;
        private String fullName;

        public EmployeeDto() {
        }

        public EmployeeDto(String employeeId, String fullName) {
            this.employeeId = employeeId;
            this.fullName = fullName;
        }

        public String getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(String employeeId) {
            this.employeeId = employeeId;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }
    }

    public static class InvoiceHistoryDto implements Serializable {
        private static final long serialVersionUID = 1L;

        private String invoiceId;
        private String createdAt;
        private String status;
        private BigDecimal originalTotal;
        private BigDecimal discountAmount;
        private BigDecimal finalTotal;
        private EmployeeDto employee;

        public InvoiceHistoryDto() {
        }

        public InvoiceHistoryDto(String invoiceId, String createdAt, String status,
                                 BigDecimal originalTotal, BigDecimal discountAmount, BigDecimal finalTotal,
                                 EmployeeDto employee) {
            this.invoiceId = invoiceId;
            this.createdAt = createdAt;
            this.status = status;
            this.originalTotal = originalTotal;
            this.discountAmount = discountAmount;
            this.finalTotal = finalTotal;
            this.employee = employee;
        }

        public String getInvoiceId() {
            return invoiceId;
        }

        public void setInvoiceId(String invoiceId) {
            this.invoiceId = invoiceId;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public BigDecimal getOriginalTotal() {
            return originalTotal;
        }

        public void setOriginalTotal(BigDecimal originalTotal) {
            this.originalTotal = originalTotal;
        }

        public BigDecimal getDiscountAmount() {
            return discountAmount;
        }

        public void setDiscountAmount(BigDecimal discountAmount) {
            this.discountAmount = discountAmount;
        }

        public BigDecimal getFinalTotal() {
            return finalTotal;
        }

        public void setFinalTotal(BigDecimal finalTotal) {
            this.finalTotal = finalTotal;
        }

        public EmployeeDto getEmployee() {
            return employee;
        }

        public void setEmployee(EmployeeDto employee) {
            this.employee = employee;
        }
    }
}
