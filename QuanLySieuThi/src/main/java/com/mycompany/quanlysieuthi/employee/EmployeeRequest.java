package com.mycompany.quanlysieuthi.employee;

/**
 * DTO for receiving employee creation/update requests.
 */
public class EmployeeRequest {

    private String id;
    private String positionId;
    private String fullName;
    private String username;
    private String password;
    private String phone;
    private Boolean status;

    public EmployeeRequest() {
    }

    public EmployeeRequest(String id, String positionId, String fullName, String username,
                           String password, String phone, Boolean status) {
        this.id = id;
        this.positionId = positionId;
        this.fullName = fullName;
        this.username = username;
        this.password = password;
        this.phone = phone;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPositionId() {
        return positionId;
    }

    public void setPositionId(String positionId) {
        this.positionId = positionId;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }
}
