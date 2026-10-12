package com.mycompany.quanlysieuthi.modules.report.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Entity ánh xạ chính xác bảng inventory_report
 */
@Entity
@Table(name = "inventory_report")
public class InventoryReport implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "report_id", length = 36, nullable = false)
    private String reportId;

    @Column(name = "employee_id", length = 36, nullable = false)
    private String employeeId;

    @Column(name = "report_date", nullable = false)
    private LocalDateTime reportDate = LocalDateTime.now();

    public InventoryReport() {
    }

    public InventoryReport(String reportId, String employeeId, LocalDateTime reportDate) {
        this.reportId = reportId;
        this.employeeId = employeeId;
        this.reportDate = reportDate;
    }

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public LocalDateTime getReportDate() {
        return reportDate;
    }

    public void setReportDate(LocalDateTime reportDate) {
        this.reportDate = reportDate;
    }
}
