package com.mycompany.quanlysieuthi.modules.purchasereceipt.dto.response;

import java.io.Serializable;

public class PurchaseReceiptEmployeeDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String employeeId;
    private String fullName;
    private String username;

    public PurchaseReceiptEmployeeDto() {
    }

    public PurchaseReceiptEmployeeDto(String employeeId, String fullName, String username) {
        this.employeeId = employeeId;
        this.fullName = fullName;
        this.username = username;
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
}
