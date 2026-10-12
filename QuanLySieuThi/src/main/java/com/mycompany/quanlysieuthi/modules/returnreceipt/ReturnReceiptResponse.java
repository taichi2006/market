package com.mycompany.quanlysieuthi.modules.returnreceipt;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Response DTO definitions for Return Receipt module.
 */
public class ReturnReceiptResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private String returnReceiptId;
    private String invoiceId;
    private String reason;
    private BigDecimal refundAmount;
    private String createdAt;
    private EmployeeDto createdBy;

    public ReturnReceiptResponse() {
    }

    public ReturnReceiptResponse(String returnReceiptId, String invoiceId, String reason,
                                 BigDecimal refundAmount, String createdAt, EmployeeDto createdBy) {
        this.returnReceiptId = returnReceiptId;
        this.invoiceId = invoiceId;
        this.reason = reason;
        this.refundAmount = refundAmount;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
    }

    public String getReturnReceiptId() {
        return returnReceiptId;
    }

    public void setReturnReceiptId(String returnReceiptId) {
        this.returnReceiptId = returnReceiptId;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public EmployeeDto getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(EmployeeDto createdBy) {
        this.createdBy = createdBy;
    }

    public static class EmployeeDto implements Serializable {
        private static final long serialVersionUID = 1L;

        private String employeeId;
        private String fullName;
        private String username;

        public EmployeeDto() {
        }

        public EmployeeDto(String employeeId, String fullName) {
            this.employeeId = employeeId;
            this.fullName = fullName;
        }

        public EmployeeDto(String employeeId, String fullName, String username) {
            this.employeeId = employeeId;
            this.fullName = fullName;
            this.username = username;
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

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }
    }

    public static class CustomerDto implements Serializable {
        private static final long serialVersionUID = 1L;

        private String customerId;
        private String fullName;
        private String phoneNumber;

        public CustomerDto() {
        }

        public CustomerDto(String customerId, String fullName, String phoneNumber) {
            this.customerId = customerId;
            this.fullName = fullName;
            this.phoneNumber = phoneNumber;
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
    }

    public static class ListItemDto implements Serializable {
        private static final long serialVersionUID = 1L;

        private String returnReceiptId;
        private String invoiceId;
        private BigDecimal refundAmount;
        private String reason;
        private String createdAt;
        private EmployeeDto createdBy;

        public ListItemDto() {
        }

        public ListItemDto(String returnReceiptId, String invoiceId, BigDecimal refundAmount,
                           String reason, String createdAt, EmployeeDto createdBy) {
            this.returnReceiptId = returnReceiptId;
            this.invoiceId = invoiceId;
            this.refundAmount = refundAmount;
            this.reason = reason;
            this.createdAt = createdAt;
            this.createdBy = createdBy;
        }

        public String getReturnReceiptId() {
            return returnReceiptId;
        }

        public void setReturnReceiptId(String returnReceiptId) {
            this.returnReceiptId = returnReceiptId;
        }

        public String getInvoiceId() {
            return invoiceId;
        }

        public void setInvoiceId(String invoiceId) {
            this.invoiceId = invoiceId;
        }

        public BigDecimal getRefundAmount() {
            return refundAmount;
        }

        public void setRefundAmount(BigDecimal refundAmount) {
            this.refundAmount = refundAmount;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public EmployeeDto getCreatedBy() {
            return createdBy;
        }

        public void setCreatedBy(EmployeeDto createdBy) {
            this.createdBy = createdBy;
        }
    }

    public static class DetailResponse implements Serializable {
        private static final long serialVersionUID = 1L;

        private String returnReceiptId;
        private String invoiceId;
        private BigDecimal refundAmount;
        private String reason;
        private String createdAt;
        private EmployeeDto createdBy;
        private CustomerDto customer;

        public DetailResponse() {
        }

        public DetailResponse(String returnReceiptId, String invoiceId, BigDecimal refundAmount,
                              String reason, String createdAt, EmployeeDto createdBy, CustomerDto customer) {
            this.returnReceiptId = returnReceiptId;
            this.invoiceId = invoiceId;
            this.refundAmount = refundAmount;
            this.reason = reason;
            this.createdAt = createdAt;
            this.createdBy = createdBy;
            this.customer = customer;
        }

        public String getReturnReceiptId() {
            return returnReceiptId;
        }

        public void setReturnReceiptId(String returnReceiptId) {
            this.returnReceiptId = returnReceiptId;
        }

        public String getInvoiceId() {
            return invoiceId;
        }

        public void setInvoiceId(String invoiceId) {
            this.invoiceId = invoiceId;
        }

        public BigDecimal getRefundAmount() {
            return refundAmount;
        }

        public void setRefundAmount(BigDecimal refundAmount) {
            this.refundAmount = refundAmount;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public EmployeeDto getCreatedBy() {
            return createdBy;
        }

        public void setCreatedBy(EmployeeDto createdBy) {
            this.createdBy = createdBy;
        }

        public CustomerDto getCustomer() {
            return customer;
        }

        public void setCustomer(CustomerDto customer) {
            this.customer = customer;
        }
    }
}
