package com.mycompany.quanlysieuthi.modules.customer;

/**
 * Employee information embedded in Invoice response.
 */
public class CustomerInvoiceEmployeeDto {

    private String employeeId;
    private String fullName;

    public CustomerInvoiceEmployeeDto() {
    }

    public CustomerInvoiceEmployeeDto(String employeeId, String fullName) {
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
