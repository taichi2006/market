package com.mycompany.quanlysieuthi.auth;

public class EmployeeAuthDto {

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
