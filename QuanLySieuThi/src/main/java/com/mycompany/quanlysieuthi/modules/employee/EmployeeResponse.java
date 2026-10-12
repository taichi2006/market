package com.mycompany.quanlysieuthi.modules.employee;

/**
 * Response DTO for created employee.
 * Excludes sensitive fields like password and refreshToken.
 */
public class EmployeeResponse {

    private String employeeId;
    private String fullName;
    private String username;
    private String phoneNumber;
    private String position;
    private Boolean status;

    public EmployeeResponse() {
    }

    public EmployeeResponse(String employeeId, String fullName, String username,
                            String phoneNumber, String position, Boolean status) {
        this.employeeId = employeeId;
        this.fullName = fullName;
        this.username = username;
        this.phoneNumber = phoneNumber;
        this.position = position;
        this.status = status;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }
}
