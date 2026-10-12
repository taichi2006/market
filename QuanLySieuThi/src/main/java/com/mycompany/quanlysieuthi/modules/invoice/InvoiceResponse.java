package com.mycompany.quanlysieuthi.modules.invoice;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO definitions for Invoice module.
 */
public class InvoiceResponse implements Serializable {

    private static final long serialVersionUID = 1L;

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
        private Integer loyaltyPoints;

        public CustomerDto() {
        }

        public CustomerDto(String customerId, String fullName, String phoneNumber) {
            this.customerId = customerId;
            this.fullName = fullName;
            this.phoneNumber = phoneNumber;
        }

        public CustomerDto(String customerId, String fullName, String phoneNumber, Integer loyaltyPoints) {
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

    public static class ItemDetailDto implements Serializable {
        private static final long serialVersionUID = 1L;

        private String invoiceDetailId;
        private String productId;
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;

        public ItemDetailDto() {
        }

        public ItemDetailDto(String invoiceDetailId, String productId, String productName, Integer quantity, BigDecimal unitPrice, BigDecimal subtotal) {
            this.invoiceDetailId = invoiceDetailId;
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.subtotal = subtotal;
        }

        public String getInvoiceDetailId() {
            return invoiceDetailId;
        }

        public void setInvoiceDetailId(String invoiceDetailId) {
            this.invoiceDetailId = invoiceDetailId;
        }

        public String getProductId() {
            return productId;
        }

        public void setProductId(String productId) {
            this.productId = productId;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
        }

        public BigDecimal getSubtotal() {
            return subtotal;
        }

        public void setSubtotal(BigDecimal subtotal) {
            this.subtotal = subtotal;
        }
    }

    public static class CreateResponse implements Serializable {
        private static final long serialVersionUID = 1L;

        private String invoiceId;
        private String createdAt;
        private String status;
        private BigDecimal originalTotal;
        private BigDecimal discountAmount;
        private BigDecimal finalTotal;
        private EmployeeDto employee;
        private CustomerDto customer;
        private List<ItemDetailDto> items;

        public CreateResponse() {
        }

        public CreateResponse(String invoiceId, String createdAt, String status, BigDecimal originalTotal,
                              BigDecimal discountAmount, BigDecimal finalTotal, EmployeeDto employee,
                              CustomerDto customer, List<ItemDetailDto> items) {
            this.invoiceId = invoiceId;
            this.createdAt = createdAt;
            this.status = status;
            this.originalTotal = originalTotal;
            this.discountAmount = discountAmount;
            this.finalTotal = finalTotal;
            this.employee = employee;
            this.customer = customer;
            this.items = items;
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

        public CustomerDto getCustomer() {
            return customer;
        }

        public void setCustomer(CustomerDto customer) {
            this.customer = customer;
        }

        public List<ItemDetailDto> getItems() {
            return items;
        }

        public void setItems(List<ItemDetailDto> items) {
            this.items = items;
        }
    }

    public static class ListItemDto implements Serializable {
        private static final long serialVersionUID = 1L;

        private String invoiceId;
        private String createdAt;
        private String status;
        private BigDecimal originalTotal;
        private BigDecimal discountAmount;
        private BigDecimal finalAmount;
        private EmployeeDto employee;
        private CustomerDto customer;

        public ListItemDto() {
        }

        public ListItemDto(String invoiceId, String createdAt, String status, BigDecimal originalTotal,
                           BigDecimal discountAmount, BigDecimal finalAmount, EmployeeDto employee, CustomerDto customer) {
            this.invoiceId = invoiceId;
            this.createdAt = createdAt;
            this.status = status;
            this.originalTotal = originalTotal;
            this.discountAmount = discountAmount;
            this.finalAmount = finalAmount;
            this.employee = employee;
            this.customer = customer;
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

        public BigDecimal getFinalAmount() {
            return finalAmount;
        }

        public void setFinalAmount(BigDecimal finalAmount) {
            this.finalAmount = finalAmount;
        }

        public EmployeeDto getEmployee() {
            return employee;
        }

        public void setEmployee(EmployeeDto employee) {
            this.employee = employee;
        }

        public CustomerDto getCustomer() {
            return customer;
        }

        public void setCustomer(CustomerDto customer) {
            this.customer = customer;
        }
    }

    public static class DetailResponse implements Serializable {
        private static final long serialVersionUID = 1L;

        private String invoiceId;
        private String createdAt;
        private String status;
        private BigDecimal originalTotal;
        private BigDecimal discountAmount;
        private BigDecimal finalAmount;
        private EmployeeDto employee;
        private CustomerDto customer;
        private List<ItemDetailDto> items;

        public DetailResponse() {
        }

        public DetailResponse(String invoiceId, String createdAt, String status, BigDecimal originalTotal,
                              BigDecimal discountAmount, BigDecimal finalAmount, EmployeeDto employee,
                              CustomerDto customer, List<ItemDetailDto> items) {
            this.invoiceId = invoiceId;
            this.createdAt = createdAt;
            this.status = status;
            this.originalTotal = originalTotal;
            this.discountAmount = discountAmount;
            this.finalAmount = finalAmount;
            this.employee = employee;
            this.customer = customer;
            this.items = items;
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

        public BigDecimal getFinalAmount() {
            return finalAmount;
        }

        public void setFinalAmount(BigDecimal finalAmount) {
            this.finalAmount = finalAmount;
        }

        public EmployeeDto getEmployee() {
            return employee;
        }

        public void setEmployee(EmployeeDto employee) {
            this.employee = employee;
        }

        public CustomerDto getCustomer() {
            return customer;
        }

        public void setCustomer(CustomerDto customer) {
            this.customer = customer;
        }

        public List<ItemDetailDto> getItems() {
            return items;
        }

        public void setItems(List<ItemDetailDto> items) {
            this.items = items;
        }
    }

    public static class CancelResponse implements Serializable {
        private static final long serialVersionUID = 1L;

        private String invoiceId;
        private String status;

        public CancelResponse() {
        }

        public CancelResponse(String invoiceId, String status) {
            this.invoiceId = invoiceId;
            this.status = status;
        }

        public String getInvoiceId() {
            return invoiceId;
        }

        public void setInvoiceId(String invoiceId) {
            this.invoiceId = invoiceId;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
