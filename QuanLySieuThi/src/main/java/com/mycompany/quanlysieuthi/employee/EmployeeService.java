package com.mycompany.quanlysieuthi.employee;

import com.mycompany.quanlysieuthi.util.ConflictException;
import com.mycompany.quanlysieuthi.util.PageMeta;
import com.mycompany.quanlysieuthi.util.PasswordUtil;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service handling business logic for Employee CRUD operations.
 */
public class EmployeeService {

    public static final Set<String> VALID_POSITIONS = Set.of("STORE_OWNER", "CASHIER", "INVENTORY_MANAGER");

    private final EmployeeDao employeeDao;

    public EmployeeService() {
        this.employeeDao = new EmployeeDao();
    }

    public EmployeeService(EmployeeDao employeeDao) {
        this.employeeDao = employeeDao;
    }

    /**
     * Check if a string is a valid UUID or valid employee ID format.
     */
    public static boolean isValidId(String id) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        return id.trim().matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
                || id.startsWith("EMP-");
    }

    /**
     * 1. Create/register a new employee account (POST /api/employee).
     */
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Dữ liệu nhân viên không được để trống");
        }
        if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
            throw new IllegalArgumentException("Họ và tên không được để trống");
        }
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên đăng nhập không được để trống");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Mật khẩu không được để trống");
        }
        if (request.getPhoneNumber() == null || request.getPhoneNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Số điện thoại không được để trống");
        }
        if (request.getPosition() == null || !VALID_POSITIONS.contains(request.getPosition().trim().toUpperCase())) {
            throw new IllegalArgumentException("Thiếu thông tin bắt buộc hoặc position không nằm trong [STORE_OWNER, CASHIER, INVENTORY_MANAGER]");
        }

        String username = request.getUsername().trim();
        String phoneNumber = request.getPhoneNumber().trim();

        // 409 Conflict: username hoặc phone_number đã tồn tại
        if (employeeDao.findByUsername(username).isPresent()) {
            throw new ConflictException("username hoặc phone_number đã tồn tại.");
        }

        if (employeeDao.findByPhoneNumber(phoneNumber).isPresent()) {
            throw new ConflictException("username hoặc phone_number đã tồn tại.");
        }

        // Tự sinh UUID cho employee_id
        String employeeId = UUID.randomUUID().toString();

        // Mật khẩu được băm bằng BCrypt trước khi INSERT
        String hashedPassword = PasswordUtil.hash(request.getPassword());

        // Mặc định status = true (hoặc theo request nếu có)
        Boolean status = (request.getStatus() != null) ? request.getStatus() : Boolean.TRUE;

        Employee employee = new Employee(
                employeeId,
                request.getFullName().trim(),
                username,
                hashedPassword,
                phoneNumber,
                request.getPosition().trim().toUpperCase(),
                status,
                null // initial refresh_token is null
        );

        Employee saved = employeeDao.save(employee);
        return toResponse(saved);
    }

    /**
     * 2. Get paginated employees matching optional filters (GET /api/employee).
     */
    public EmployeePageResult getEmployees(Integer page, Integer limit, String keyword, String position, Boolean status) {
        int currentPage = (page == null || page < 1) ? 1 : page;
        int currentLimit = (limit == null || limit < 1) ? 10 : limit;
        if (currentLimit > 100) {
            currentLimit = 100;
        }

        EmployeeDao.PageResult<Employee> result = employeeDao.findEmployees(
                currentPage, currentLimit, keyword, position, status
        );

        List<EmployeeResponse> responses = result.getItems().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        long totalElements = result.getTotalElements();
        int totalPages = (totalElements == 0) ? 0 : (int) Math.ceil((double) totalElements / currentLimit);

        PageMeta meta = new PageMeta(currentPage, currentLimit, totalElements, totalPages);
        return new EmployeePageResult(responses, meta);
    }

    /**
     * 3. Get single employee details by ID (GET /api/employee/:id).
     */
    public EmployeeResponse getEmployeeById(String id) {
        if (!isValidId(id)) {
            throw new IllegalArgumentException("ID không đúng định dạng [UUID]");
        }

        return employeeDao.findById(id.trim())
                .map(this::toResponse)
                .orElse(null);
    }

    /**
     * 4. Update employee details by ID (PUT /api/employee/:id).
     */
    public EmployeeResponse updateEmployee(String id, EmployeeRequest request) {
        if (!isValidId(id)) {
            throw new IllegalArgumentException("ID không đúng định dạng [UUID]");
        }
        if (request == null) {
            throw new IllegalArgumentException("Thiếu thông tin bắt buộc hoặc position không nằm trong [STORE_OWNER, CASHIER, INVENTORY_MANAGER]");
        }
        if (request.getFullName() == null || request.getFullName().trim().isEmpty() ||
                request.getPhoneNumber() == null || request.getPhoneNumber().trim().isEmpty() ||
                request.getPosition() == null || !VALID_POSITIONS.contains(request.getPosition().trim().toUpperCase())) {
            throw new IllegalArgumentException("Thiếu thông tin bắt buộc hoặc position không nằm trong [STORE_OWNER, CASHIER, INVENTORY_MANAGER]");
        }

        Employee existing = employeeDao.findById(id.trim()).orElse(null);
        if (existing == null) {
            return null; // Not found -> Controller returns 404
        }

        String newPhone = request.getPhoneNumber().trim();
        // Kiểm tra trùng lặp phone_number trước khi UPDATE (loại trừ ID của chính nhân viên đang sửa)
        if (employeeDao.findByPhoneNumberExcludingId(newPhone, id.trim()).isPresent()) {
            throw new ConflictException("Số điện thoại (phone_number) đã được sử dụng bởi nhân viên khác.");
        }

        // Cập nhật các trường cho phép (không cho phép cập nhật employee_id và username)
        existing.setFullName(request.getFullName().trim());
        existing.setPhoneNumber(newPhone);
        existing.setPosition(request.getPosition().trim().toUpperCase());

        if (request.getStatus() != null) {
            existing.setStatus(request.getStatus());
        }

        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            existing.setPassword(PasswordUtil.hash(request.getPassword().trim()));
        }

        Employee saved = employeeDao.save(existing);
        return toResponse(saved);
    }

    /**
     * 5. Soft delete employee by ID (DELETE /api/employee/:id).
     */
    public EmployeeDeleteResponse deleteEmployee(String id, String currentUserId) {
        if (!isValidId(id)) {
            throw new IllegalArgumentException("ID không đúng định dạng UUID hoặc tài khoản đang cố tự xóa chính mình.");
        }

        // Không cho phép STORE_OWNER tự xóa mềm tài khoản của chính mình
        if (currentUserId != null && currentUserId.equalsIgnoreCase(id.trim())) {
            throw new IllegalArgumentException("ID không đúng định dạng UUID hoặc tài khoản đang cố tự xóa chính mình.");
        }

        Employee existing = employeeDao.findById(id.trim()).orElse(null);
        if (existing == null) {
            return null; // Not found -> Controller returns 404
        }

        // Áp dụng cơ chế xóa mềm: UPDATE employee SET status = false WHERE employee_id = :id
        employeeDao.softDelete(id.trim());

        return new EmployeeDeleteResponse(existing.getEmployeeId(), Boolean.FALSE);
    }

    private EmployeeResponse toResponse(Employee saved) {
        if (saved == null) {
            return null;
        }
        // Output tuyệt đối không trả về trường password và refreshToken
        return new EmployeeResponse(
                saved.getEmployeeId(),
                saved.getFullName(),
                saved.getUsername(),
                saved.getPhoneNumber(),
                saved.getPosition(),
                saved.getStatus()
        );
    }
}
