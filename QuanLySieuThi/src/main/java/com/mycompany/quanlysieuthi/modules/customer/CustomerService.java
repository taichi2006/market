package com.mycompany.quanlysieuthi.modules.customer;

import com.mycompany.quanlysieuthi.entity.Customer;
import com.mycompany.quanlysieuthi.common.exception.AppException.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;
import com.mycompany.quanlysieuthi.util.IdGenerator;
import java.util.stream.Collectors;

/**
 * Service handling customer business logic.
 */
public class CustomerService {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    private final CustomerRepository customerRepository;

    public CustomerService() {
        this.customerRepository = new CustomerRepository();
    }

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    /**
     * Tạo khách hàng mới (hoặc đăng ký thành viên).
     */
    public CustomerResponse createCustomer(CustomerRequest.Create request) {
        if (request == null) {
            throw new IllegalArgumentException("Dữ liệu khách hàng không được để trống");
        }
        if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
            throw new IllegalArgumentException("Họ và tên khách hàng không được để trống");
        }
        if (request.getPhoneNumber() == null || request.getPhoneNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Số điện thoại khách hàng không được để trống");
        }

        String phone = request.getPhoneNumber().trim();
        if (customerRepository.existsByPhoneNumber(phone)) {
            throw new ConflictException("Số điện thoại đã tồn tại trên hệ thống");
        }

        Customer customer = new Customer();
        customer.setCustomerId(IdGenerator.nextCustomerId());
        customer.setFullName(request.getFullName().trim());
        customer.setPhoneNumber(phone);
        customer.setEmail((request.getEmail() != null && !request.getEmail().trim().isEmpty())
                ? request.getEmail().trim() : null);
        customer.setLoyaltyPoints(0);
        customer.setCreatedAt(LocalDateTime.now());

        Customer saved = customerRepository.save(customer);
        return toCustomerResponse(saved);
    }

    /**
     * Lấy danh sách khách hàng (tìm kiếm theo keyword + phân trang).
     */
    public List<CustomerResponse> getCustomers(int page, int limit, String keyword) {
        if (page <= 0) {
            throw new IllegalArgumentException("Tham số page phải lớn hơn 0");
        }
        if (limit <= 0) {
            throw new IllegalArgumentException("Tham số limit phải lớn hơn 0");
        }

        List<Customer> list = customerRepository.findCustomers(page, limit, keyword);
        return list.stream().map(this::toCustomerResponse).collect(Collectors.toList());
    }

    /**
     * Lấy lịch sử mua hàng (danh sách hóa đơn) của khách hàng.
     */
    public List<CustomerResponse.InvoiceHistoryDto> getCustomerInvoices(String customerId,
                                                                       int page,
                                                                       int limit,
                                                                       String status,
                                                                       String fromDateStr,
                                                                       String toDateStr) {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã khách hàng không được để trống");
        }
        if (page <= 0) {
            throw new IllegalArgumentException("Tham số page phải lớn hơn 0");
        }
        if (limit <= 0) {
            throw new IllegalArgumentException("Tham số limit phải lớn hơn 0");
        }

        // Kiểm tra khách hàng có tồn tại không
        customerRepository.findById(customerId.trim())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy khách hàng với ID: " + customerId.trim()));

        LocalDateTime fromDateTime = null;
        LocalDateTime toDateTime = null;

        if (fromDateStr != null && !fromDateStr.trim().isEmpty()) {
            try {
                LocalDate fromDate = LocalDate.parse(fromDateStr.trim());
                fromDateTime = fromDate.atStartOfDay();
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("fromDate không đúng định dạng YYYY-MM-DD");
            }
        }

        if (toDateStr != null && !toDateStr.trim().isEmpty()) {
            try {
                LocalDate toDate = LocalDate.parse(toDateStr.trim());
                toDateTime = toDate.atTime(LocalTime.MAX);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("toDate không đúng định dạng YYYY-MM-DD");
            }
        }

        if (fromDateTime != null && toDateTime != null && fromDateTime.isAfter(toDateTime)) {
            throw new IllegalArgumentException("fromDate không được sau toDate");
        }

        return customerRepository.findCustomerInvoices(customerId.trim(), page, limit, status, fromDateTime, toDateTime);
    }

    private CustomerResponse toCustomerResponse(Customer customer) {
        if (customer == null) {
            return null;
        }
        String formattedCreatedAt = null;
        if (customer.getCreatedAt() != null) {
            formattedCreatedAt = customer.getCreatedAt().atOffset(ZoneOffset.UTC).format(ISO_FORMATTER);
        }
        return new CustomerResponse(
                customer.getCustomerId(),
                customer.getFullName(),
                customer.getPhoneNumber(),
                customer.getEmail(),
                customer.getLoyaltyPoints(),
                formattedCreatedAt
        );
    }
}
