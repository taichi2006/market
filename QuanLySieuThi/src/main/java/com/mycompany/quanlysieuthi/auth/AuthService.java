package com.mycompany.quanlysieuthi.auth;

import com.mycompany.quanlysieuthi.employee.Employee;
import com.mycompany.quanlysieuthi.employee.EmployeeDao;
import com.mycompany.quanlysieuthi.util.JwtUtil;
import com.mycompany.quanlysieuthi.util.PasswordUtil;
import io.jsonwebtoken.JwtException;

import java.util.Optional;

public class AuthService {

    private final EmployeeDao employeeDao;

    public AuthService() {
        this.employeeDao = new EmployeeDao();
    }

    public AuthService(EmployeeDao employeeDao) {
        this.employeeDao = employeeDao;
    }

    /**
     * Authenticate employee credentials and generate tokens.
     */
    public LoginResult login(LoginRequest request) {
        if (request == null ||
                request.getUsername() == null || request.getUsername().trim().isEmpty() ||
                request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("thiếu input");
        }

        String username = request.getUsername().trim();
        String password = request.getPassword();

        Employee employee = employeeDao.findByUsername(username)
                .orElseThrow(() -> new SecurityException("username/password không chính xác"));

        if (!PasswordUtil.check(password, employee.getPassword())) {
            throw new SecurityException("username/password không chính xác");
        }

        if (Boolean.FALSE.equals(employee.getStatus())) {
            throw new SecurityException("Tài khoản đã bị vô hiệu hóa");
        }

        // Generate Access Token (JWT)
        String accessToken = JwtUtil.generateAccessToken(
                employee.getEmployeeId(),
                employee.getUsername(),
                employee.getFullName(),
                employee.getPosition()
        );

        // Generate Refresh Token
        String refreshToken = JwtUtil.generateRefreshToken(
                employee.getEmployeeId(),
                employee.getUsername()
        );

        // Save refresh token to employee record in DB
        employeeDao.updateRefreshToken(employee.getEmployeeId(), refreshToken);

        EmployeeAuthDto employeeDto = new EmployeeAuthDto(
                employee.getEmployeeId(),
                employee.getUsername(),
                employee.getFullName(),
                employee.getPosition()
        );

        LoginResponseData responseData = new LoginResponseData(accessToken, employeeDto);
        return new LoginResult(responseData, refreshToken);
    }

    /**
     * Clear refresh token in database on logout.
     */
    public void logout(String refreshToken) {
        if (refreshToken != null && !refreshToken.trim().isEmpty()) {
            Optional<Employee> empOpt = employeeDao.findByRefreshToken(refreshToken.trim());
            empOpt.ifPresent(employee -> employeeDao.updateRefreshToken(employee.getEmployeeId(), null));
        }
    }

    /**
     * Refresh access token using valid refresh token from cookie.
     */
    public String refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            throw new IllegalArgumentException("không tìm thấy token trong cookie");
        }

        if (!JwtUtil.validateToken(refreshToken.trim())) {
            throw new SecurityException("refreshtoken hết hạn/ không hợp lệ");
        }

        Employee employee = employeeDao.findByRefreshToken(refreshToken.trim())
                .orElseThrow(() -> new SecurityException("refreshtoken hết hạn/ không hợp lệ"));

        if (Boolean.FALSE.equals(employee.getStatus())) {
            throw new SecurityException("refreshtoken hết hạn/ không hợp lệ");
        }

        // Return newly generated access token
        return JwtUtil.generateAccessToken(
                employee.getEmployeeId(),
                employee.getUsername(),
                employee.getFullName(),
                employee.getPosition()
        );
    }
}
