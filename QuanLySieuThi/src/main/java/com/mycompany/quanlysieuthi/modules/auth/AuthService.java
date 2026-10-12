package com.mycompany.quanlysieuthi.modules.auth;

import com.mycompany.quanlysieuthi.entity.Employee;
import com.mycompany.quanlysieuthi.util.JwtUtil;
import com.mycompany.quanlysieuthi.util.PasswordUtil;

import java.util.Optional;

public class AuthService {

    private final AuthRepository authRepository;

    public AuthService() {
        this.authRepository = new AuthRepository();
    }

    public AuthService(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    /**
     * Authenticate employee credentials and generate tokens.
     */
    public AuthResponse.LoginResult login(AuthRequest.Login request) {
        if (request == null ||
                request.getUsername() == null || request.getUsername().trim().isEmpty() ||
                request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("thiếu input");
        }

        String username = request.getUsername().trim();
        String password = request.getPassword();

        Employee employee = authRepository.findByUsername(username)
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
        authRepository.updateRefreshToken(employee.getEmployeeId(), refreshToken);

        AuthResponse.EmployeeAuthDto employeeDto = new AuthResponse.EmployeeAuthDto(
                employee.getEmployeeId(),
                employee.getUsername(),
                employee.getFullName(),
                employee.getPosition()
        );

        AuthResponse.LoginResponseData responseData = new AuthResponse.LoginResponseData(accessToken, employeeDto);
        return new AuthResponse.LoginResult(responseData, refreshToken);
    }

    /**
     * Clear refresh token in database on logout.
     */
    public void logout(String refreshToken) {
        if (refreshToken != null && !refreshToken.trim().isEmpty()) {
            Optional<Employee> empOpt = authRepository.findByRefreshToken(refreshToken.trim());
            empOpt.ifPresent(employee -> authRepository.updateRefreshToken(employee.getEmployeeId(), null));
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

        Employee employee = authRepository.findByRefreshToken(refreshToken.trim())
                .orElseThrow(() -> new SecurityException("refreshtoken hết hạn/ không hợp lệ"));

        if (Boolean.FALSE.equals(employee.getStatus())) {
            throw new SecurityException("refreshtoken hết hạn/ không hợp lệ");
        }

        return JwtUtil.generateAccessToken(
                employee.getEmployeeId(),
                employee.getUsername(),
                employee.getFullName(),
                employee.getPosition()
        );
    }
}
