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
            throw new IllegalArgumentException("Request body khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng");
        }
        if (request.getAuditTitle() == null || request.getAuditTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("TiÃªu Ä‘á» kiá»ƒm kÃª (auditTitle) khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Danh sÃ¡ch máº·t hÃ ng kiá»ƒm kÃª (items) khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng");
        }

        String reportId = IdGenerator.nextReportId();
        LocalDateTime now = LocalDateTime.now();

        int totalDiscrepancyItems = 0;
        List<InventoryReportDetail> details = new ArrayList<>();
        List<ReportResponse.DiscrepancyItemDto> discrepancyDtos = new ArrayList<>();

        for (ReportRequest.Item item : request.getItems()) {
            if (item.getProductId() == null || item.getProductId().trim().isEmpty()) {
                throw new IllegalArgumentException("MÃ£ sáº£n pháº©m (productId) khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng");
            }
            if (item.getActualQuantity() == null || item.getActualQuantity() < 0) {
                throw new IllegalArgumentException("Sá»‘ lÆ°á»£ng thá»±c táº¿ (actualQuantity) pháº£i lÃ  sá»‘ nguyÃªn >= 0");
            }

            Product product = reportRepository.findProductById(item.getProductId().trim())
                    .orElseThrow(() -> new NotFoundException(
                            "CÃ³ productId trong danh sÃ¡ch khÃ´ng tá»“n táº¡i trong há»‡ thá»‘ng: " + item.getProductId().trim()));

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
                if (item.getReason() == null || item.getReason().trim().isEmpty()) {
                    throw new IllegalArgumentException(
                            "Thiáº¿u lÃ½ do giáº£i trÃ¬nh cho máº·t hÃ ng bá»‹ chÃªnh lá»‡ch: " + product.getProductName());
                }
            }

            String reason = item.getReason() != null ? item.getReason().trim() : null;

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
                request.getAuditTitle().trim(),
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
            throw new IllegalArgumentException("Tham sá»‘ page pháº£i lá»›n hÆ¡n 0");
        }
        if (limit < 0) {
            throw new IllegalArgumentException("Tham sá»‘ limit pháº£i lá»›n hÆ¡n hoáº·c báº±ng 0");
        }
        if (limit == 0 && page != 1) {
            throw new IllegalArgumentException("Khi limit = 0, page pháº£i lÃ  1 Ä‘á»ƒ láº¥y toÃ n bá»™ danh sÃ¡ch");
        }

        LocalDateTime fromDateTime = null;
        LocalDateTime toDateTime = null;

        if (fromDateStr != null && !fromDateStr.trim().isEmpty()) {
            try {
                LocalDate fromDate = LocalDate.parse(fromDateStr.trim());
                fromDateTime = fromDate.atStartOfDay();
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Äá»‹nh dáº¡ng ngÃ y thÃ¡ng fromDate khÃ´ng há»£p lá»‡ (yÃªu cáº§u YYYY-MM-DD)");
            }
        }

        if (toDateStr != null && !toDateStr.trim().isEmpty()) {
            try {
                LocalDate toDate = LocalDate.parse(toDateStr.trim());
                toDateTime = toDate.atTime(LocalTime.MAX);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Äá»‹nh dáº¡ng ngÃ y thÃ¡ng toDate khÃ´ng há»£p lá»‡ (yÃªu cáº§u YYYY-MM-DD)");
            }
        }

        if (fromDateTime != null && toDateTime != null && fromDateTime.isAfter(toDateTime)) {
            throw new IllegalArgumentException("fromDate khÃ´ng Ä‘Æ°á»£c sau toDate");
        }

        return reportRepository.findAuditReports(page, limit, employeeId, fromDateTime, toDateTime);
    }

    public ReportResponse.DetailResponse getStockDiscrepancyDetail(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID biÃªn báº£n kiá»ƒm kÃª khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng");
        }


        return reportRepository.findAuditReportDetailById(id.trim())
                .orElseThrow(() -> new NotFoundException("KhÃ´ng tÃ¬m tháº¥y biÃªn báº£n kiá»ƒm kÃª vá»›i ID tÆ°Æ¡ng á»©ng: " + id.trim()));
    }

    private String formatIso(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        return dateTime.atOffset(ZoneOffset.UTC).format(formatter);
    }
}
