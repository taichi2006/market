package com.mycompany.quanlysieuthi.modules.employee;

import com.mycompany.quanlysieuthi.entity.Employee;
import com.mycompany.quanlysieuthi.common.exception.AppException.*;
import com.mycompany.quanlysieuthi.util.PageMeta;
import com.mycompany.quanlysieuthi.util.PasswordUtil;

import java.util.List;
import java.util.Set;
import com.mycompany.quanlysieuthi.util.IdGenerator;
import java.util.stream.Collectors;

/**
 * Service handling business logic for Employee CRUD operations.
 */
public class EmployeeService {

    public static final Set<String> VALID_POSITIONS = Set.of("STORE_OWNER", "CASHIER", "INVENTORY_MANAGER");

    private final EmployeeRepository employeeRepository;

    public EmployeeService() {
        this.employeeRepository = new EmployeeRepository();
    }

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    /**
     * Check if a string is a valid ID (not null or empty).
     */
    public static boolean isValidId(String id) {
        return id != null && !id.trim().isEmpty();
    }

    private static final java.util.regex.Pattern PHONE_PATTERN = java.util.regex.Pattern.compile("^[0-9+]{9,15}$");

    /**
     * 1. Create/register a new employee account (POST /api/employee).
     */
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        if (request == null) {
            throw new BadRequestException("Dữ liệu nhân viên không được để trống");
        }
        if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
            throw new BadRequestException("Họ và tên không được để trống");
        }
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new BadRequestException("Tên đăng nhập không được để trống");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new BadRequestException("Mật khẩu không được để trống");
        }
        if (request.getPhoneNumber() == null || request.getPhoneNumber().trim().isEmpty()) {
            throw new BadRequestException("Số điện thoại không được để trống");
        }
        String phoneNumber = request.getPhoneNumber().trim();
        if (!PHONE_PATTERN.matcher(phoneNumber).matches()) {
            throw new BadRequestException("Số điện thoại không đúng định dạng (yêu cầu từ 9 đến 15 chữ số).");
        }
        if (request.getPosition() == null || !VALID_POSITIONS.contains(request.getPosition().trim().toUpperCase())) {
            throw new BadRequestException("Thiếu thông tin bắt buộc hoặc position không nằm trong [STORE_OWNER, CASHIER, INVENTORY_MANAGER]");
        }

        String username = request.getUsername().trim();

        if (employeeRepository.findByUsername(username).isPresent()) {
            throw new ConflictException("Tên đăng nhập (username) đã được sử dụng.");
        }

        if (employeeRepository.findByPhoneNumber(phoneNumber).isPresent()) {
            throw new ConflictException("Số điện thoại (phone_number) đã được sử dụng.");
        }

        String employeeId = IdGenerator.nextEmployeeId();
        String hashedPassword = PasswordUtil.hash(request.getPassword());
        Boolean status = (request.getStatus() != null) ? request.getStatus() : Boolean.TRUE;

        Employee employee = new Employee(
                employeeId,
                request.getFullName().trim(),
                username,
                hashedPassword,
                phoneNumber,
                request.getPosition().trim().toUpperCase(),
                status,
                null
        );

        Employee saved = employeeRepository.save(employee);
        return toResponse(saved);
    }

    /**
     * 2. Get paginated employees matching optional filters (GET /api/employee).
     */
    public EmployeeResponse.PageResult getEmployees(Integer page, Integer limit, String keyword, String position, Boolean status) {
        int currentPage = (page == null || page < 1) ? 1 : page;
        int currentLimit = (limit == null || limit < 1) ? 10 : limit;
        if (currentLimit > 100) {
            currentLimit = 100;
        }

        EmployeeRepository.PageResult<Employee> result = employeeRepository.findEmployees(
                currentPage, currentLimit, keyword, position, status
        );

        List<EmployeeResponse> responses = result.getItems().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        long totalElements = result.getTotalElements();
        int totalPages = (totalElements == 0) ? 0 : (int) Math.ceil((double) totalElements / currentLimit);

        PageMeta meta = new PageMeta(currentPage, currentLimit, totalElements, totalPages);
        return new EmployeeResponse.PageResult(responses, meta);
    }

    /**
     * 3. Get single employee details by ID (GET /api/employee/:id).
     */
    public EmployeeResponse getEmployeeById(String id) {
        if (!isValidId(id)) {
            throw new BadRequestException("Mã nhân viên (ID) không hợp lệ hoặc để trống.");
        }

        Employee employee = employeeRepository.findByIdIgnoreCase(id.trim())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy nhân viên với mã: " + id.trim()));

        return toResponse(employee);
    }

    /**
     * 4. Update employee details by ID (PUT /api/employee/:id).
     */
    public EmployeeResponse updateEmployee(String id, EmployeeRequest request) {
        if (!isValidId(id)) {
            throw new BadRequestException("Mã nhân viên (ID) không hợp lệ hoặc để trống.");
        }
        if (request == null) {
            throw new BadRequestException("Dữ liệu cập nhật nhân viên không được để trống.");
        }
        if (request.getFullName() == null || request.getFullName().trim().isEmpty() ||
                request.getPhoneNumber() == null || request.getPhoneNumber().trim().isEmpty() ||
                request.getPosition() == null || !VALID_POSITIONS.contains(request.getPosition().trim().toUpperCase())) {
            throw new BadRequestException("Thiếu thông tin bắt buộc hoặc position không nằm trong [STORE_OWNER, CASHIER, INVENTORY_MANAGER]");
        }

        Employee existing = employeeRepository.findByIdIgnoreCase(id.trim())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy nhân viên với mã: " + id.trim()));

        String newPhone = request.getPhoneNumber().trim();
        if (!PHONE_PATTERN.matcher(newPhone).matches()) {
            throw new BadRequestException("Số điện thoại không đúng định dạng (yêu cầu từ 9 đến 15 chữ số).");
        }
        if (employeeRepository.findByPhoneNumberExcludingId(newPhone, existing.getEmployeeId()).isPresent()) {
            throw new ConflictException("Số điện thoại (phone_number) đã được sử dụng bởi nhân viên khác.");
        }

        String newPosition = request.getPosition().trim().toUpperCase();
        if ("STORE_OWNER".equalsIgnoreCase(existing.getPosition()) && !newPosition.equalsIgnoreCase("STORE_OWNER")) {
            long activeOwners = employeeRepository.countActiveStoreOwners();
            if (activeOwners <= 1) {
                throw new BusinessException("Không thể thay đổi vai trò của Quản trị viên (STORE_OWNER) duy nhất trong hệ thống.");
            }
        }

        existing.setFullName(request.getFullName().trim());
        existing.setPhoneNumber(newPhone);
        existing.setPosition(newPosition);

        if (request.getStatus() != null) {
            existing.setStatus(request.getStatus());
        }

        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            existing.setPassword(PasswordUtil.hash(request.getPassword().trim()));
        }

        Employee saved = employeeRepository.update(existing);
        return toResponse(saved);
    }

    /**
     * 5. Soft delete employee by ID (DELETE /api/employee/:id).
     */
    public EmployeeResponse.DeleteResult deleteEmployee(String id, String currentUserId) {
        if (!isValidId(id)) {
            throw new BadRequestException("Mã nhân viên (ID) không hợp lệ hoặc để trống.");
        }

        // Tìm nhân viên cần xóa (hỗ trợ case-insensitive)
        Employee existing = employeeRepository.findByIdIgnoreCase(id.trim())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy nhân viên với mã: " + id.trim()));

        // Ràng buộc 1: Không được tự xóa tài khoản của chính mình (kiểm tra cả employeeId và username)
        if (currentUserId != null && (currentUserId.trim().equalsIgnoreCase(existing.getEmployeeId()) ||
                currentUserId.trim().equalsIgnoreCase(existing.getUsername()))) {
            throw new BadRequestException("Bạn không thể tự xóa hoặc vô hiệu hóa tài khoản của chính mình.");
        }

        // Ràng buộc 2: Tài khoản đã bị vô hiệu hóa trước đó
        if (Boolean.FALSE.equals(existing.getStatus())) {
            throw new ConflictException("Nhân viên có mã '" + existing.getEmployeeId() + "' đã bị vô hiệu hóa trước đó.");
        }

        // Ràng buộc 3: Không thể vô hiệu hóa STORE_OWNER duy nhất đang hoạt động
        if ("STORE_OWNER".equalsIgnoreCase(existing.getPosition())) {
            long activeOwners = employeeRepository.countActiveStoreOwners();
            if (activeOwners <= 1) {
                throw new BusinessException("Không thể vô hiệu hóa Quản trị viên (STORE_OWNER) duy nhất đang hoạt động trong hệ thống.");
            }
        }

        // Thực hiện cập nhật trạng thái xóa mềm
        boolean success = employeeRepository.softDelete(existing.getEmployeeId());
        if (!success) {
            throw new BusinessException("Không thể cập nhật trạng thái vô hiệu hóa nhân viên trong cơ sở dữ liệu.");
        }

        return new EmployeeResponse.DeleteResult(existing.getEmployeeId(), Boolean.FALSE);
    }

    private EmployeeResponse toResponse(Employee saved) {
        if (saved == null) {
            return null;
        }
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
