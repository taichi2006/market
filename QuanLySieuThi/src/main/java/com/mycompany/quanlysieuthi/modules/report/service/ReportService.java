package com.mycompany.quanlysieuthi.modules.report.service;

import com.mycompany.quanlysieuthi.modules.purchasereceipt.entity.Product;
import com.mycompany.quanlysieuthi.modules.report.dao.ReportDao;
import com.mycompany.quanlysieuthi.modules.report.dto.request.StockAuditItemRequest;
import com.mycompany.quanlysieuthi.modules.report.dto.request.StockAuditRequest;
import com.mycompany.quanlysieuthi.modules.report.dto.response.create.AuditCreatedByDto;
import com.mycompany.quanlysieuthi.modules.report.dto.response.create.StockAuditResponse;
import com.mycompany.quanlysieuthi.modules.report.dto.response.create.StockDiscrepancyItemDto;
import com.mycompany.quanlysieuthi.modules.report.dto.response.detail.StockAuditDetailResponse;
import com.mycompany.quanlysieuthi.modules.report.dto.response.list.StockAuditListItemDto;
import com.mycompany.quanlysieuthi.modules.report.entity.InventoryReport;
import com.mycompany.quanlysieuthi.modules.report.entity.InventoryReportDetail;
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

/**
 * Service handling business logic for Report creation.
 */
public class ReportService {

    private final ReportDao reportDao;

    public ReportService() {
        this.reportDao = new ReportDao();
    }

    public ReportService(ReportDao reportDao) {
        this.reportDao = reportDao;
    }

    /**
     * Tạo biên bản kiểm kê kho định kỳ và tính toán chênh lệch tồn kho.
     */
    public StockAuditResponse createStockAuditReport(StockAuditRequest request,
            String employeeId,
            String employeeFullName) {
        // 1. Validate payload
        if (request == null) {
            throw new IllegalArgumentException("Request body không được để trống");
        }
        if (request.getAuditTitle() == null || request.getAuditTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Tiêu đề kiểm kê (auditTitle) không được để trống");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Danh sách mặt hàng kiểm kê (items) không được để trống");
        }

        String reportId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();

        int totalDiscrepancyItems = 0;
        List<InventoryReportDetail> details = new ArrayList<>();
        List<StockDiscrepancyItemDto> discrepancyDtos = new ArrayList<>();

        // 2. Validate and process each item
        for (StockAuditItemRequest item : request.getItems()) {
            if (item.getProductId() == null || item.getProductId().trim().isEmpty()) {
                throw new IllegalArgumentException("Mã sản phẩm (productId) không được để trống");
            }
            if (item.getActualQuantity() == null || item.getActualQuantity() < 0) {
                throw new IllegalArgumentException("Số lượng thực tế (actualQuantity) phải là số nguyên >= 0");
            }

            // Truy vấn thông tin sản phẩm và tồn kho hiện tại (systemQuantity)
            Product product = reportDao.findProductById(item.getProductId().trim())
                    .orElseThrow(() -> new NotFoundException(
                            "Có productId trong danh sách không tồn tại trong hệ thống: "
                                    + item.getProductId().trim()));

            int systemQuantity = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
            int actualQuantity = item.getActualQuantity();
            int difference = actualQuantity - systemQuantity;

            // Xác định trạng thái lệch: SHORTAGE, SURPLUS, MATCH
            String status;
            if (difference < 0) {
                status = "SHORTAGE";
            } else if (difference > 0) {
                status = "SURPLUS";
            } else {
                status = "MATCH";
            }

            // Nếu có chênh lệch thì yêu cầu phải có lý do giải trình
            if (difference != 0) {
                totalDiscrepancyItems++;
                if (item.getReason() == null || item.getReason().trim().isEmpty()) {
                    throw new IllegalArgumentException(
                            "Thiếu lý do giải trình cho mặt hàng bị chênh lệch: " + product.getProductName());
                }
            }

            String reason = item.getReason() != null ? item.getReason().trim() : null;

            InventoryReportDetail detail = new InventoryReportDetail(
                    reportId,
                    product.getProductId(),
                    systemQuantity,
                    actualQuantity);
            details.add(detail);

            discrepancyDtos.add(new StockDiscrepancyItemDto(
                    product.getProductId(),
                    product.getProductName(),
                    systemQuantity,
                    actualQuantity,
                    difference,
                    status,
                    reason));
        }

        // 3. Create InventoryReport entity theo chuẩn Neon DB
        InventoryReport report = new InventoryReport(
                reportId,
                employeeId != null ? employeeId : "",
                now);

        // 4. Execute atomic transaction in Database
        reportDao.saveAuditReportTransaction(report, details);

        // 5. Construct Response
        AuditCreatedByDto createdBy = new AuditCreatedByDto(
                employeeId != null ? employeeId : "",
                employeeFullName != null ? employeeFullName : "");

        return new StockAuditResponse(
                reportId,
                request.getAuditTitle().trim(),
                formatIso(now),
                createdBy,
                totalDiscrepancyItems,
                discrepancyDtos);
    }

    /**
     * Lấy danh sách các biên bản kiểm kê / báo cáo sai lệch kho (hỗ trợ phân trang,
     * lọc theo trạng thái, nhân viên, ngày tạo).
     */
    public List<StockAuditListItemDto> getStockDiscrepancyReports(Integer pageParam,
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

        return reportDao.findAuditReports(page, limit, employeeId, fromDateTime, toDateTime);
    }

    /**
     * Lấy thông tin chi tiết một biên bản kiểm kê kho theo reportId.
     */
    public StockAuditDetailResponse getStockDiscrepancyDetail(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID biên bản kiểm kê không được để trống");
        }

        try {
            UUID.fromString(id.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("ID không đúng định dạng UUID");
        }

        return reportDao.findAuditReportDetailById(id.trim())
                .orElseThrow(
                        () -> new NotFoundException("Không tìm thấy biên bản kiểm kê với ID tương ứng: " + id.trim()));
    }

    private String formatIso(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        return dateTime.atOffset(ZoneOffset.UTC).format(formatter);
    }

}
