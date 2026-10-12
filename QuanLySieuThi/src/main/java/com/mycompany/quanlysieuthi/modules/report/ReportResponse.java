package com.mycompany.quanlysieuthi.modules.report;

import java.io.Serializable;
import java.util.List;

/**
 * Response DTO definitions for Report module.
 */
public class ReportResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    public static class CreatedByDto implements Serializable {
        private static final long serialVersionUID = 1L;

        private String employeeId;
        private String fullName;

        public CreatedByDto() {
        }

        public CreatedByDto(String employeeId, String fullName) {
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

    public static class EmployeeDto implements Serializable {
        private static final long serialVersionUID = 1L;

        private String employeeId;
        private String fullName;
        private String username;

        public EmployeeDto() {
        }

        public EmployeeDto(String employeeId, String fullName, String username) {
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

    public static class DiscrepancyItemDto implements Serializable {
        private static final long serialVersionUID = 1L;

        private String productId;
        private String productName;
        private Integer systemQuantity;
        private Integer actualQuantity;
        private Integer difference;
        private String status;
        private String reason;

        public DiscrepancyItemDto() {
        }

        public DiscrepancyItemDto(String productId, String productName, Integer systemQuantity,
                                  Integer actualQuantity, Integer difference, String status, String reason) {
            this.productId = productId;
            this.productName = productName;
            this.systemQuantity = systemQuantity;
            this.actualQuantity = actualQuantity;
            this.difference = difference;
            this.status = status;
            this.reason = reason;
        }

        public String getProductId() {
            return productId;
        }

        public void setProductId(String productId) {
            this.productId = productId;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public Integer getSystemQuantity() {
            return systemQuantity;
        }

        public void setSystemQuantity(Integer systemQuantity) {
            this.systemQuantity = systemQuantity;
        }

        public Integer getActualQuantity() {
            return actualQuantity;
        }

        public void setActualQuantity(Integer actualQuantity) {
            this.actualQuantity = actualQuantity;
        }

        public Integer getDifference() {
            return difference;
        }

        public void setDifference(Integer difference) {
            this.difference = difference;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }

    public static class StockAuditResponse implements Serializable {
        private static final long serialVersionUID = 1L;

        private String reportId;
        private String auditTitle;
        private String createdAt;
        private CreatedByDto createdBy;
        private int totalDiscrepancyItems;
        private List<DiscrepancyItemDto> items;

        public StockAuditResponse() {
        }

        public StockAuditResponse(String reportId, String auditTitle, String createdAt,
                                  CreatedByDto createdBy, int totalDiscrepancyItems, List<DiscrepancyItemDto> items) {
            this.reportId = reportId;
            this.auditTitle = auditTitle;
            this.createdAt = createdAt;
            this.createdBy = createdBy;
            this.totalDiscrepancyItems = totalDiscrepancyItems;
            this.items = items;
        }

        public String getReportId() {
            return reportId;
        }

        public void setReportId(String reportId) {
            this.reportId = reportId;
        }

        public String getAuditTitle() {
            return auditTitle;
        }

        public void setAuditTitle(String auditTitle) {
            this.auditTitle = auditTitle;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public CreatedByDto getCreatedBy() {
            return createdBy;
        }

        public void setCreatedBy(CreatedByDto createdBy) {
            this.createdBy = createdBy;
        }

        public int getTotalDiscrepancyItems() {
            return totalDiscrepancyItems;
        }

        public void setTotalDiscrepancyItems(int totalDiscrepancyItems) {
            this.totalDiscrepancyItems = totalDiscrepancyItems;
        }

        public List<DiscrepancyItemDto> getItems() {
            return items;
        }

        public void setItems(List<DiscrepancyItemDto> items) {
            this.items = items;
        }
    }

    public static class ListItemDto implements Serializable {
        private static final long serialVersionUID = 1L;

        private String reportId;
        private String auditTitle;
        private String createdAt;
        private int totalDiscrepancyItems;
        private EmployeeDto createdBy;

        public ListItemDto() {
        }

        public ListItemDto(String reportId, String auditTitle, String createdAt, int totalDiscrepancyItems, EmployeeDto createdBy) {
            this.reportId = reportId;
            this.auditTitle = auditTitle;
            this.createdAt = createdAt;
            this.totalDiscrepancyItems = totalDiscrepancyItems;
            this.createdBy = createdBy;
        }

        public String getReportId() {
            return reportId;
        }

        public void setReportId(String reportId) {
            this.reportId = reportId;
        }

        public String getAuditTitle() {
            return auditTitle;
        }

        public void setAuditTitle(String auditTitle) {
            this.auditTitle = auditTitle;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public int getTotalDiscrepancyItems() {
            return totalDiscrepancyItems;
        }

        public void setTotalDiscrepancyItems(int totalDiscrepancyItems) {
            this.totalDiscrepancyItems = totalDiscrepancyItems;
        }

        public EmployeeDto getCreatedBy() {
            return createdBy;
        }

        public void setCreatedBy(EmployeeDto createdBy) {
            this.createdBy = createdBy;
        }
    }

    public static class DetailItemDto implements Serializable {
        private static final long serialVersionUID = 1L;

        private String detailId;
        private String productId;
        private String productName;
        private String categoryName;
        private Integer systemQuantity;
        private Integer actualQuantity;
        private Integer difference;
        private String status;
        private String reason;

        public DetailItemDto() {
        }

        public DetailItemDto(String detailId, String productId, String productName, String categoryName,
                             Integer systemQuantity, Integer actualQuantity, Integer difference, String status, String reason) {
            this.detailId = detailId;
            this.productId = productId;
            this.productName = productName;
            this.categoryName = categoryName;
            this.systemQuantity = systemQuantity;
            this.actualQuantity = actualQuantity;
            this.difference = difference;
            this.status = status;
            this.reason = reason;
        }

        public String getDetailId() {
            return detailId;
        }

        public void setDetailId(String detailId) {
            this.detailId = detailId;
        }

        public String getProductId() {
            return productId;
        }

        public void setProductId(String productId) {
            this.productId = productId;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }

        public Integer getSystemQuantity() {
            return systemQuantity;
        }

        public void setSystemQuantity(Integer systemQuantity) {
            this.systemQuantity = systemQuantity;
        }

        public Integer getActualQuantity() {
            return actualQuantity;
        }

        public void setActualQuantity(Integer actualQuantity) {
            this.actualQuantity = actualQuantity;
        }

        public Integer getDifference() {
            return difference;
        }

        public void setDifference(Integer difference) {
            this.difference = difference;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }

    public static class DetailResponse implements Serializable {
        private static final long serialVersionUID = 1L;

        private String reportId;
        private String auditTitle;
        private String note;
        private String createdAt;
        private EmployeeDto createdBy;
        private int totalDiscrepancyItems;
        private List<DetailItemDto> items;

        public DetailResponse() {
        }

        public DetailResponse(String reportId, String auditTitle, String note, String createdAt,
                              EmployeeDto createdBy, int totalDiscrepancyItems, List<DetailItemDto> items) {
            this.reportId = reportId;
            this.auditTitle = auditTitle;
            this.note = note;
            this.createdAt = createdAt;
            this.createdBy = createdBy;
            this.totalDiscrepancyItems = totalDiscrepancyItems;
            this.items = items;
        }

        public String getReportId() {
            return reportId;
        }

        public void setReportId(String reportId) {
            this.reportId = reportId;
        }

        public String getAuditTitle() {
            return auditTitle;
        }

        public void setAuditTitle(String auditTitle) {
            this.auditTitle = auditTitle;
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public EmployeeDto getCreatedBy() {
            return createdBy;
        }

        public void setCreatedBy(EmployeeDto createdBy) {
            this.createdBy = createdBy;
        }

        public int getTotalDiscrepancyItems() {
            return totalDiscrepancyItems;
        }

        public void setTotalDiscrepancyItems(int totalDiscrepancyItems) {
            this.totalDiscrepancyItems = totalDiscrepancyItems;
        }

        public List<DetailItemDto> getItems() {
            return items;
        }

        public void setItems(List<DetailItemDto> items) {
            this.items = items;
        }
    }
}
