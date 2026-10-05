package com.mycompany.quanlysieuthi.employee;

/**
 * Request payload for creating a new employee.
 */
public class EmployeeRequest {

    private String fullName;
    private String username;
    private String password;
    private String phoneNumber;
    private String position;
    private Boolean status;

    public EmployeeRequest() {
    }

    public EmployeeRequest(String fullName, String username, String password, String phoneNumber, String position) {
        this(fullName, username, password, phoneNumber, position, Boolean.TRUE);
    }

    public EmployeeRequest(String fullName, String username, String password, String phoneNumber, String position, Boolean status) {
        this.fullName = fullName;
        this.username = username;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.position = position;
        this.status = status;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
