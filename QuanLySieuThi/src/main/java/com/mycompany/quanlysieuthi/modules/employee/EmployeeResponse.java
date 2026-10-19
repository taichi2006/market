package com.mycompany.quanlysieuthi.modules.employee;

import com.mycompany.quanlysieuthi.util.PageMeta;

import java.io.Serializable;
import java.util.List;

/**
 * Response DTO definitions for Employee module.
 * Excludes sensitive fields like password and refreshToken.
 */
public class EmployeeResponse implements Serializable {

    private static final long serialVersionUID = 1L;

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

    /**
     * Container for paginated employee responses.
     */
    public record PageResult(List<EmployeeResponse> data, PageMeta meta) implements Serializable {
        public List<EmployeeResponse> getData() { return data; }
        public PageMeta getMeta() { return meta; }
    }

    /**
     * Result returned after soft deleting an employee.
     */
    public record DeleteResult(String employeeId, Boolean status) implements Serializable {}

    /**
     * DTO đại diện báo cáo hiệu suất thu ngân theo ngày từ View v_hieu_suat_thu_ngan_theo_ngay.
     */
    public record CashierPerformanceDto(
            String employeeId,
            String cashierName,
            String shiftDate,
            long totalTransactions,
            java.math.BigDecimal totalCollected,
            java.math.BigDecimal cashAmount,
            java.math.BigDecimal onlineAmount
    ) implements Serializable {}
}
