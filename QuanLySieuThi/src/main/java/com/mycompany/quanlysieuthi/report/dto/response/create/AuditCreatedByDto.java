package com.mycompany.quanlysieuthi.report.dto.response.create;

import java.io.Serializable;

/**
 * DTO đại diện cho nhân viên tạo biên bản kiểm kê trong response tạo mới.
 */
public class AuditCreatedByDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String employeeId;
    private String fullName;

    public AuditCreatedByDto() {
    }

    public AuditCreatedByDto(String employeeId, String fullName) {
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
