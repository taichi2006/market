package com.mycompany.quanlysieuthi.modules.invoice;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO definitions for Invoice module.
 */
public class InvoiceResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    public record EmployeeDto(String employeeId, String fullName, String username) implements Serializable {
        public EmployeeDto(String employeeId, String fullName) {
            this(employeeId, fullName, null);
        }
        public String getEmployeeId() { return employeeId; }
        public String getFullName() { return fullName; }
        public String getUsername() { return username; }
    }

    public record CustomerDto(String customerId, String fullName, String phoneNumber, Integer loyaltyPoints) implements Serializable {
        public CustomerDto(String customerId, String fullName, String phoneNumber) {
            this(customerId, fullName, phoneNumber, null);
        }
        public String getCustomerId() { return customerId; }
        public String getFullName() { return fullName; }
        public String getPhoneNumber() { return phoneNumber; }
        public Integer getLoyaltyPoints() { return loyaltyPoints; }
    }

    public record ItemDetailDto(String invoiceDetailId, String productId, String productName, Integer quantity,
                                BigDecimal unitPrice, BigDecimal subtotal) implements Serializable {
        public String getInvoiceDetailId() { return invoiceDetailId; }
        public String getProductId() { return productId; }
        public String getProductName() { return productName; }
        public Integer getQuantity() { return quantity; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public BigDecimal getSubtotal() { return subtotal; }
    }

    public record CreateResponse(String invoiceId, String createdAt, String status, BigDecimal originalTotal,
                                 BigDecimal discountAmount, BigDecimal finalTotal, EmployeeDto employee,
                                 CustomerDto customer, List<ItemDetailDto> items) implements Serializable {
        public String getInvoiceId() { return invoiceId; }
        public String getCreatedAt() { return createdAt; }
        public String getStatus() { return status; }
        public BigDecimal getOriginalTotal() { return originalTotal; }
        public BigDecimal getDiscountAmount() { return discountAmount; }
        public BigDecimal getFinalTotal() { return finalTotal; }
        public EmployeeDto getEmployee() { return employee; }
        public CustomerDto getCustomer() { return customer; }
        public List<ItemDetailDto> getItems() { return items; }
    }

    public record ListItemDto(String invoiceId, String createdAt, String status, BigDecimal originalTotal,
                              BigDecimal discountAmount, BigDecimal finalAmount, EmployeeDto employee,
                              CustomerDto customer) implements Serializable {
        public String getInvoiceId() { return invoiceId; }
        public String getCreatedAt() { return createdAt; }
        public String getStatus() { return status; }
        public BigDecimal getOriginalTotal() { return originalTotal; }
        public BigDecimal getDiscountAmount() { return discountAmount; }
        public BigDecimal getFinalAmount() { return finalAmount; }
        public EmployeeDto getEmployee() { return employee; }
        public CustomerDto getCustomer() { return customer; }
    }

    public record DetailResponse(String invoiceId, String createdAt, String status, BigDecimal originalTotal,
                                 BigDecimal discountAmount, BigDecimal finalAmount, EmployeeDto employee,
                                 CustomerDto customer, List<ItemDetailDto> items) implements Serializable {
        public String getInvoiceId() { return invoiceId; }
        public String getCreatedAt() { return createdAt; }
        public String getStatus() { return status; }
        public BigDecimal getOriginalTotal() { return originalTotal; }
        public BigDecimal getDiscountAmount() { return discountAmount; }
        public BigDecimal getFinalAmount() { return finalAmount; }
        public EmployeeDto getEmployee() { return employee; }
        public CustomerDto getCustomer() { return customer; }
        public List<ItemDetailDto> getItems() { return items; }
    }

    public record CancelResponse(String invoiceId, String status) implements Serializable {
        public String getInvoiceId() { return invoiceId; }
        public String getStatus() { return status; }
    }
}
