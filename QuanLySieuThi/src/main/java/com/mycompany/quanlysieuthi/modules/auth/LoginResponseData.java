package com.mycompany.quanlysieuthi.modules.auth;

public class LoginResponseData {

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
