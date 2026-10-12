package com.mycompany.quanlysieuthi.modules.report.dto.response.common;

import java.io.Serializable;

/**
 * DTO đại diện cho thông tin nhân viên kiểm kê dùng chung cho các response kiểm kê.
 */
public class StockAuditEmployeeDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String employeeId;
    private String fullName;
    private String username;

    public StockAuditEmployeeDto() {
    }

    public StockAuditEmployeeDto(String employeeId, String fullName, String username) {
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
