package com.mycompany.quanlysieuthi.modules.report;

import com.mycompany.quanlysieuthi.entity.InventoryReport;
import com.mycompany.quanlysieuthi.entity.InventoryReportDetail;
import com.mycompany.quanlysieuthi.entity.Product;
import com.mycompany.quanlysieuthi.util.NotFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.mycompany.quanlysieuthi.util.IdGenerator;

/**
 * Service handling business logic for Report creation.
 */
public class ReportService {

    private final ReportRepository reportRepository;

    public ReportService() {
        this.reportRepository = new ReportRepository();
    }

    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public ReportResponse.StockAuditResponse createStockAuditReport(ReportRequest.StockAudit request,
                                                                    String employeeId,
                                                                    String employeeFullName) {
        if (request == null) {
            throw new IllegalArgumentException("Request body không được để trống");
        }

        String title = request.getAuditTitle();
        if (title == null || title.trim().isEmpty()) {
            title = request.getNote();
        }
        if (title == null || title.trim().isEmpty()) {
            title = "Kiểm kê định kỳ kho";
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Danh sách mặt hàng kiểm kê (items) không được để trống");
        }

        String reportId = IdGenerator.nextReportId();
        LocalDateTime now = LocalDateTime.now();

        int totalDiscrepancyItems = 0;
        List<InventoryReportDetail> details = new ArrayList<>();
        List<ReportResponse.DiscrepancyItemDto> discrepancyDtos = new ArrayList<>();

        for (ReportRequest.Item item : request.getItems()) {
            if (item.getProductId() == null || item.getProductId().trim().isEmpty()) {
                throw new IllegalArgumentException("Mã sản phẩm (productId) không được để trống");
            }
            if (item.getActualQuantity() == null || item.getActualQuantity() < 0) {
                throw new IllegalArgumentException("Số lượng thực tế (actualQuantity) phải là số nguyên >= 0");
            }

            Product product = reportRepository.findProductById(item.getProductId().trim())
                    .orElseThrow(() -> new NotFoundException(
                            "Có productId trong danh sách không tồn tại trong hệ thống: " + item.getProductId().trim()));

            int systemQuantity = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
            int actualQuantity = item.getActualQuantity();
            int difference = actualQuantity - systemQuantity;

            String status;
            if (difference < 0) {
                status = "SHORTAGE";
            } else if (difference > 0) {
                status = "SURPLUS";
            } else {
                status = "MATCH";
            }

            if (difference != 0) {
                totalDiscrepancyItems++;
            }

            String reason = item.getReason();
            if (reason == null || reason.trim().isEmpty()) {
                reason = difference != 0 ? "Chênh lệch kiểm kê kho" : null;
            } else {
                reason = reason.trim();
            }

            InventoryReportDetail detail = new InventoryReportDetail(
                    reportId,
                    product.getProductId(),
                    systemQuantity,
                    actualQuantity
            );
            details.add(detail);

            discrepancyDtos.add(new ReportResponse.DiscrepancyItemDto(
                    product.getProductId(),
                    product.getProductName(),
                    systemQuantity,
                    actualQuantity,
                    difference,
                    status,
                    reason
            ));
        }

        InventoryReport report = new InventoryReport(
                reportId,
                employeeId != null ? employeeId : "",
                now
        );

        reportRepository.saveAuditReportTransaction(report, details);

        ReportResponse.CreatedByDto createdBy = new ReportResponse.CreatedByDto(
                employeeId != null ? employeeId : "",
                employeeFullName != null ? employeeFullName : ""
        );

        return new ReportResponse.StockAuditResponse(
                reportId,
                title.trim(),
                formatIso(now),
                createdBy,
                totalDiscrepancyItems,
                discrepancyDtos
        );
    }

    public List<ReportResponse.ListItemDto> getStockDiscrepancyReports(Integer pageParam,
                                                                       Integer limitParam,
                                                                       String employeeId,
                                                                       String fromDateStr,
                                                                       String toDateStr) {
        int page = (pageParam != null) ? pageParam : 1;
        int limit = (limitParam != null) ? limitParam : 10;

        if (page <= 0) {
            throw new IllegalArgumentException("Tham số page phải lớn hơn 0");
        }
        if (limit < 0) {
            throw new IllegalArgumentException("Tham số limit phải lớn hơn hoặc bằng 0");
        }
        if (limit == 0 && page != 1) {
            throw new IllegalArgumentException("Khi limit = 0, page phải là 1 để lấy toàn bộ danh sách");
        }

        LocalDateTime fromDateTime = null;
        LocalDateTime toDateTime = null;

        if (fromDateStr != null && !fromDateStr.trim().isEmpty()) {
            try {
                LocalDate fromDate = LocalDate.parse(fromDateStr.trim());
                fromDateTime = fromDate.atStartOfDay();
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Định dạng ngày tháng fromDate không hợp lệ (yêu cầu YYYY-MM-DD)");
            }
        }

        if (toDateStr != null && !toDateStr.trim().isEmpty()) {
            try {
                LocalDate toDate = LocalDate.parse(toDateStr.trim());
                toDateTime = toDate.atTime(LocalTime.MAX);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Định dạng ngày tháng toDate không hợp lệ (yêu cầu YYYY-MM-DD)");
            }
        }

        if (fromDateTime != null && toDateTime != null && fromDateTime.isAfter(toDateTime)) {
            throw new IllegalArgumentException("fromDate không được sau toDate");
        }

        return reportRepository.findAuditReports(page, limit, employeeId, fromDateTime, toDateTime);
    }

    public ReportResponse.DetailResponse getStockDiscrepancyDetail(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID biên bản kiểm kê không được để trống");
        }

        return reportRepository.findAuditReportDetailById(id.trim())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy biên bản kiểm kê với ID tương ứng: " + id.trim()));
    }

    private String formatIso(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        return dateTime.atOffset(ZoneOffset.UTC).format(formatter);
    }
}
