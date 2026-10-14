package com.mycompany.quanlysieuthi.modules.report;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO definitions for Report module.
 */
public class ReportResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    public record CreatedByDto(String employeeId, String fullName) implements Serializable {}

    public record EmployeeDto(String employeeId, String fullName, String username) implements Serializable {}

    public record DiscrepancyItemDto(String productId, String productName, Integer systemQuantity,
                                     Integer actualQuantity, Integer difference, String status, String reason) implements Serializable {}

    public record StockAuditResponse(String reportId, String auditTitle, String createdAt,
                                     CreatedByDto createdBy, int totalDiscrepancyItems, List<DiscrepancyItemDto> items) implements Serializable {}

    public record ListItemDto(String reportId, String auditTitle, String createdAt, int totalDiscrepancyItems,
                              EmployeeDto createdBy) implements Serializable {}

    public record DetailItemDto(String detailId, String productId, String productName, String categoryName,
                                Integer systemQuantity, Integer actualQuantity, Integer difference, String status, String reason) implements Serializable {}

    public record DetailResponse(String reportId, String auditTitle, String note, String createdAt,
                                 EmployeeDto createdBy, int totalDiscrepancyItems, List<DetailItemDto> items) implements Serializable {}

    public record FilterDto(String type, String date, Integer month, Integer year, String from, String to) implements Serializable {}

    public record PaymentMethodBreakdownDto(BigDecimal cash, BigDecimal online) implements Serializable {}

    public record RevenueSummaryDto(
        BigDecimal totalRevenue,
        BigDecimal originalTotal,
        BigDecimal totalDiscount,
        BigDecimal totalRefund,
        BigDecimal netRevenue,
        long totalInvoices,
        PaymentMethodBreakdownDto paymentMethodBreakdown
    ) implements Serializable {}

    public record RevenueTimelineItemDto(
        String timePoint,
        BigDecimal revenue,
        long invoicesCount,
        BigDecimal cashAmount,
        BigDecimal onlineAmount
    ) implements Serializable {}

    public record RevenueReportData(
        FilterDto filter,
        RevenueSummaryDto summary,
        List<RevenueTimelineItemDto> timeline
    ) implements Serializable {}
}
