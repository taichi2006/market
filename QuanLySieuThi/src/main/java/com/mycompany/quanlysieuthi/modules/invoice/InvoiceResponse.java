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
    }

    public record CustomerDto(String customerId, String fullName, String phoneNumber, Integer loyaltyPoints) implements Serializable {
        public CustomerDto(String customerId, String fullName, String phoneNumber) {
            this(customerId, fullName, phoneNumber, null);
        }
    }

    public record ItemDetailDto(String invoiceDetailId, String productId, String productName, Integer quantity,
                                BigDecimal unitPrice, BigDecimal subtotal) implements Serializable {}

    public record CreateResponse(String invoiceId, String createdAt, String status, BigDecimal originalTotal,
                                 BigDecimal discountAmount, BigDecimal finalTotal, EmployeeDto employee,
                                 CustomerDto customer, List<ItemDetailDto> items) implements Serializable {}

    public record ListItemDto(String invoiceId, String createdAt, String status, BigDecimal originalTotal,
                              BigDecimal discountAmount, BigDecimal finalAmount, EmployeeDto employee,
                              CustomerDto customer) implements Serializable {}

    public record DetailResponse(String invoiceId, String createdAt, String status, BigDecimal originalTotal,
                                 BigDecimal discountAmount, BigDecimal finalAmount, EmployeeDto employee,
                                 CustomerDto customer, List<ItemDetailDto> items) implements Serializable {}

    public record CancelResponse(String invoiceId, String status) implements Serializable {}
}
