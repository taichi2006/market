package com.mycompany.quanlysieuthi.modules.employee;

import java.io.Serializable;

/**
 * Response DTO returned after soft deleting an employee.
 */
public class EmployeeDeleteResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private String employeeId;
    private Boolean status;

    public EmployeeDeleteResponse() {
    }

    public EmployeeDeleteResponse(String employeeId, Boolean status) {
        this.employeeId = employeeId;
        this.status = status;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }
}
