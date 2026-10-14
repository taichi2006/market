package com.mycompany.quanlysieuthi.modules.auth;

import java.io.Serializable;

/**
 * Response payload definitions for Auth module.
 */
public class AuthResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    public record EmployeeAuthDto(String employeeId, String username, String fullName, String position) implements Serializable {}

    public record LoginResponseData(String accessToken, EmployeeAuthDto employee) implements Serializable {}

    public record LoginResult(LoginResponseData responseData, String refreshToken) implements Serializable {
        public LoginResponseData getResponseData() { return responseData; }
        public String getRefreshToken() { return refreshToken; }
    }

    public record RefreshResponseData(String accessToken) implements Serializable {}
}
