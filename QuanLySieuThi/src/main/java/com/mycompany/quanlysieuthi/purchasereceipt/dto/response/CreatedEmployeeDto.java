package com.mycompany.quanlysieuthi.purchasereceipt.dto.response;

import java.io.Serializable;

public class CreatedEmployeeDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String employeeId;
    private String fullName;

    public CreatedEmployeeDto() {
    }

    public CreatedEmployeeDto(String employeeId, String fullName) {
        this.employeeId = employeeId;
        this.fullName = fullName;
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
}
