package com.mycompany.quanlysieuthi.employee;

import com.mycompany.quanlysieuthi.util.ConflictException;
import com.mycompany.quanlysieuthi.util.PasswordUtil;

import java.util.UUID;

/**
 * Service handling business logic for Employee creation / registration.
 */
public class EmployeeService {

    private final EmployeeDao employeeDao;

    public EmployeeService() {
        this.employeeDao = new EmployeeDao();
    }

    public EmployeeService(EmployeeDao employeeDao) {
        this.employeeDao = employeeDao;
    }

    /**
     * Create/register a new employee account.
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
        if (request.getPosition() == null || request.getPosition().trim().isEmpty()) {
            throw new IllegalArgumentException("Chức vụ không được để trống");
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

        // Mặc định status = true
        Boolean defaultStatus = Boolean.TRUE;

        Employee employee = new Employee(
                employeeId,
                request.getFullName().trim(),
                username,
                hashedPassword,
                phoneNumber,
                request.getPosition().trim(),
                defaultStatus,
                null // initial refresh_token is null
        );

        Employee saved = employeeDao.save(employee);

        // Output tuyệt đối không trả về trường password
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
