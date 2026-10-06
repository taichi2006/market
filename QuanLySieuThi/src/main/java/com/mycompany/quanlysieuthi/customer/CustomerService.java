package com.mycompany.quanlysieuthi.customer;

import com.mycompany.quanlysieuthi.util.ConflictException;
import com.mycompany.quanlysieuthi.util.NotFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Service handling business logic for Customer operations.
 */
public class CustomerService {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9+]{9,15}$");

    private final CustomerDao customerDao;

    public CustomerService() {
        this.customerDao = new CustomerDao();
    }

    public CustomerService(CustomerDao customerDao) {
        this.customerDao = customerDao;
    }

    public List<CustomerResponse> getCustomers(Integer pageParam, Integer limitParam, String keyword) {
        int page = (pageParam != null) ? pageParam : 1;
        int limit = (limitParam != null) ? limitParam : 10;

        if (page <= 0) {
            throw new IllegalArgumentException("Tham số page phải lớn hơn 0");
        }
        if (limit <= 0) {
            throw new IllegalArgumentException("Tham số limit phải lớn hơn 0");
        }

        List<Customer> customers = customerDao.findCustomers(page, limit, keyword);
        List<CustomerResponse> result = new ArrayList<>();

        for (Customer c : customers) {
            result.add(mapToResponse(c));
        }

        return result;
    }

    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Dữ liệu khách hàng không được để trống");
        }

        if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
            throw new IllegalArgumentException("Thiếu fullName hoặc phoneNumber; Số điện thoại không đúng định dạng.");
        }

        if (request.getPhoneNumber() == null || request.getPhoneNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Thiếu fullName hoặc phoneNumber; Số điện thoại không đúng định dạng.");
        }

        String phoneNumber = request.getPhoneNumber().trim();
        if (!PHONE_PATTERN.matcher(phoneNumber).matches()) {
            throw new IllegalArgumentException("Thiếu fullName hoặc phoneNumber; Số điện thoại không đúng định dạng.");
        }

        // 409 Conflict: Số điện thoại đã tồn tại
        if (customerDao.existsByPhoneNumber(phoneNumber)) {
            throw new ConflictException("Số điện thoại (phoneNumber) này đã được đăng ký thành viên trước đó.");
        }

        String customerId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        String email = (request.getEmail() != null && !request.getEmail().trim().isEmpty())
                ? request.getEmail().trim()
                : null;

        Customer customer = new Customer(
                customerId,
                request.getFullName().trim(),
                phoneNumber,
                email,
                0, // loyaltyPoints default = 0
                now
        );

        Customer saved = customerDao.save(customer);
        return mapToResponse(saved);
    }

    public CustomerResponse getCustomerDetail(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID khách hàng không được để trống");
        }

        Customer customer = customerDao.findById(customerId.trim())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy khách hàng với ID tương ứng: " + customerId.trim()));

        return mapToResponse(customer);
    }

    public List<CustomerInvoiceHistoryDto> getCustomerInvoices(String customerId,
                                                               Integer pageParam,
                                                               Integer limitParam,
                                                               String status,
                                                               String fromDateStr,
                                                               String toDateStr) {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID khách hàng không được để trống");
        }

        // 404 Not Found nếu khách hàng không tồn tại
        customerDao.findById(customerId.trim())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy khách hàng với ID tương ứng: " + customerId.trim()));

        int page = (pageParam != null) ? pageParam : 1;
        int limit = (limitParam != null) ? limitParam : 10;

        if (page <= 0) {
            throw new IllegalArgumentException("Tham số page phải lớn hơn 0");
        }
        if (limit <= 0) {
            throw new IllegalArgumentException("Tham số limit phải lớn hơn 0");
        }

        if (status != null && !status.trim().isEmpty()) {
            String upperStatus = status.trim().toUpperCase();
            if (!upperStatus.equals("PENDING") && !upperStatus.equals("PAID") && !upperStatus.equals("CANCELLED")) {
                throw new IllegalArgumentException("Trạng thái status không hợp lệ (phải là PENDING, PAID hoặc CANCELLED)");
            }
        }

        LocalDateTime fromDateTime = null;
        LocalDateTime toDateTime = null;

        if (fromDateStr != null && !fromDateStr.trim().isEmpty()) {
            try {
                LocalDate fromDate = LocalDate.parse(fromDateStr.trim());
                fromDateTime = fromDate.atStartOfDay();
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Định dạng ngày tháng fromDate không hợp lệ (yêu cầu YYYY-MM-DD)");
            }
        }

        if (toDateStr != null && !toDateStr.trim().isEmpty()) {
            try {
                LocalDate toDate = LocalDate.parse(toDateStr.trim());
                toDateTime = toDate.atTime(LocalTime.MAX);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Định dạng ngày tháng toDate không hợp lệ (yêu cầu YYYY-MM-DD)");
            }
        }

        if (fromDateTime != null && toDateTime != null && fromDateTime.isAfter(toDateTime)) {
            throw new IllegalArgumentException("fromDate không được sau toDate");
        }

        return customerDao.findCustomerInvoices(customerId.trim(), page, limit, status, fromDateTime, toDateTime);
    }

    private CustomerResponse mapToResponse(Customer customer) {
        String formattedDate = (customer.getCreatedAt() != null)
                ? customer.getCreatedAt().atOffset(ZoneOffset.UTC).format(ISO_FORMATTER)
                : null;

        return new CustomerResponse(
                customer.getCustomerId(),
                customer.getFullName(),
                customer.getPhoneNumber(),
                customer.getEmail(),
                customer.getLoyaltyPoints(),
                formattedDate
        );
    }
}
