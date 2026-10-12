package com.mycompany.quanlysieuthi.modules.auth;

/**
 * Response payload definitions for Auth module.
 */
public class AuthResponse {

    public static class EmployeeAuthDto {
        private String employeeId;
        private String username;
        private String fullName;
        private String position;

        public EmployeeAuthDto() {
        }

        public EmployeeAuthDto(String employeeId, String username, String fullName, String position) {
            this.employeeId = employeeId;
            this.username = username;
            this.fullName = fullName;
            this.position = position;
        }

        public String getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(String employeeId) {
            this.employeeId = employeeId;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getPosition() {
            return position;
        }

        public void setPosition(String position) {
            this.position = position;
        }
    }

    public static class LoginResponseData {
        private String accessToken;
        private EmployeeAuthDto employee;

        public LoginResponseData() {
        }

        public LoginResponseData(String accessToken, EmployeeAuthDto employee) {
            this.accessToken = accessToken;
            this.employee = employee;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public EmployeeAuthDto getEmployee() {
            return employee;
        }

        public void setEmployee(EmployeeAuthDto employee) {
            this.employee = employee;
        }
    }

    public static class LoginResult {
        private LoginResponseData responseData;
        private String refreshToken;

        public LoginResult(LoginResponseData responseData, String refreshToken) {
            this.responseData = responseData;
            this.refreshToken = refreshToken;
        }

        public LoginResponseData getResponseData() {
            return responseData;
        }

        public String getRefreshToken() {
            return refreshToken;
        }
    }

    public static class RefreshResponseData {
        private String accessToken;

        public RefreshResponseData() {
        }

        public RefreshResponseData(String accessToken) {
            this.accessToken = accessToken;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }
    }
}
