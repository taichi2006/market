package com.mycompany.quanlysieuthi.employee;

/**
 * DTO for sending employee data safely to Frontend (excludes sensitive fields like password).
 */
public class EmployeeResponse {

    private String id;
    private String positionId;
    private String positionName;
    private String fullName;
    private String username;
    private String phone;
    private Boolean status;

    public EmployeeResponse() {
    }

    public EmployeeResponse(String id, String fullName, String username, String phone,
                            Boolean status, String positionName) {
        this.id = id;
        this.fullName = fullName;
        this.username = username;
        this.phone = phone;
        this.status = status;
        this.positionName = positionName;
    }

    public EmployeeResponse(String id, String positionId, String positionName, String fullName,
                            String username, String phone, Boolean status) {
        this.id = id;
        this.positionId = positionId;
        this.positionName = positionName;
        this.fullName = fullName;
        this.username = username;
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

    public String getPositionName() {
        return positionName;
    }

    public void setPositionName(String positionName) {
        this.positionName = positionName;
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
