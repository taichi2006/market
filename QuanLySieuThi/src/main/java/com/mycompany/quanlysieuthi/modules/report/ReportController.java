package com.mycompany.quanlysieuthi.modules.report;

import com.mycompany.quanlysieuthi.common.base.BaseController;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Controller handling report endpoints:
 * POST /api/report/stock-discrepancy
 * GET  /api/report/stock-discrepancy
 * GET  /api/report/stock-discrepancy/:id
 */
@WebServlet(name = "ReportController", urlPatterns = {"/api/report/*", "/api/report"})
public class ReportController extends BaseController {

    private final ReportService reportService = new ReportService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            return;
        }

        if (pathInfo.equals("/stock-discrepancy") || pathInfo.equals("/stock-discrepancy/")) {
            handleGetStockDiscrepancyList(req, resp);
        } else if (pathInfo.equals("/category-inventory") || pathInfo.equals("/category-inventory/")) {
            handle(resp, () -> reportService.getCategoryInventoryReport(), "Lấy báo cáo tổng quan kho theo danh mục thành công");
        } else if (pathInfo.startsWith("/stock-discrepancy/")) {
            String id = pathInfo.substring("/stock-discrepancy/".length()).trim();
            if (id.isEmpty()) {
                handleGetStockDiscrepancyList(req, resp);
            } else {
                handleGetStockDiscrepancyDetail(id, resp);
            }
        } else if (pathInfo.equals("/revenue") || pathInfo.equals("/revenue/")) {
            handleGetRevenueReport(req, resp);
        } else if (pathInfo.equals("/export-excel") || pathInfo.equals("/export-excel/") || pathInfo.equals("/excel") || pathInfo.equals("/excel/")) {
            handleExportExcel(req, resp);
        } else {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found: " + pathInfo);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo != null && (pathInfo.equals("/stock-discrepancy") || pathInfo.equals("/stock-discrepancy/"))) {
            handleCreateStockAudit(req, resp);
        } else if (pathInfo != null && pathInfo.equals("/stock-discrepancy/category-session")) {
            handle(resp, HttpServletResponse.SC_CREATED, () -> {
                ReportRequest.CreateAuditSession body = parseBody(req, ReportRequest.CreateAuditSession.class);
                String empId = getCurrentEmployeeId(req);
                String fullName = getCurrentFullName(req);
                return reportService.createAuditSessionByCategory(
                        body != null ? body.categoryId() : null,
                        empId != null ? empId : "",
                        fullName != null ? fullName : "",
                        body != null ? body.auditTitle() : null,
                        body != null ? body.note() : null
                );
            }, "Khởi tạo đợt kiểm kê theo danh mục thành công", null);
        } else if (pathInfo != null && pathInfo.contains("/finalize")) {
            String reportId = pathInfo.replace("/stock-discrepancy/", "").replace("/finalize", "").replace("/", "").trim();
            handle(resp, () -> {
                String empId = getCurrentEmployeeId(req);
                return reportService.finalizeAuditReport(reportId, empId != null ? empId : "");
            }, "Chốt đợt kiểm kê và tự động xuất hủy hàng hao hụt thành công");
        } else {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found: " + pathInfo);
        }
    }

    private void handleCreateStockAudit(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handle(resp, HttpServletResponse.SC_CREATED, () -> {
            ReportRequest.StockAudit requestDto = parseBody(req, ReportRequest.StockAudit.class);
            if (requestDto == null) {
                throw new IllegalArgumentException("Request body không được để trống");
            }

            String employeeId = getCurrentEmployeeId(req);
            String fullName = getCurrentFullName(req);

            return reportService.createStockAuditReport(
                    requestDto,
                    employeeId != null ? employeeId : "",
                    fullName != null ? fullName : ""
            );
        }, "Tạo báo cáo kiểm kê kho thành công, đã gửi yêu cầu xử lý đến Chủ cửa hàng", null);
    }

    private void handleGetStockDiscrepancyList(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handle(resp, () -> {
            int page = parsePage(req);
            int limit = parseLimit(req);

            String employeeId = req.getParameter("employeeId");
            String fromDate = req.getParameter("fromDate");
            String toDate = req.getParameter("toDate");

            return reportService.getStockDiscrepancyReports(
                    page, limit, employeeId, fromDate, toDate
            );
        }, "Lấy danh sách báo cáo kiểm kê kho thành công");
    }

    private void handleGetStockDiscrepancyDetail(String id, HttpServletResponse resp) throws IOException {
        handle(resp, () -> reportService.getStockDiscrepancyDetail(id), "Lấy chi tiết báo cáo kiểm kê kho thành công");
    }

    private void handleGetRevenueReport(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handle(resp, () -> {
            String type = req.getParameter("type");
            String date = req.getParameter("date");
            String monthStr = req.getParameter("month");
            String yearStr = req.getParameter("year");
            String fromDate = req.getParameter("fromDate");
            String toDate = req.getParameter("toDate");

            Integer month = null;
            if (monthStr != null && !monthStr.trim().isEmpty()) {
                try {
                    month = Integer.parseInt(monthStr.trim());
                } catch (NumberFormatException ignored) {}
            }

            Integer year = null;
            if (yearStr != null && !yearStr.trim().isEmpty()) {
                try {
                    year = Integer.parseInt(yearStr.trim());
                } catch (NumberFormatException ignored) {}
            }

            return reportService.getRevenueReport(type, date, month, year, fromDate, toDate);
        }, "Lấy báo cáo doanh thu thành công");
    }

    private void handleExportExcel(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String target = req.getParameter("target");
            if (target == null || target.trim().isEmpty()) {
                target = req.getParameter("receiptType");
            }
            if (target == null || target.trim().isEmpty()) {
                target = req.getParameter("type");
            }

            String receiptId = req.getParameter("receiptId");
            if (receiptId == null || receiptId.trim().isEmpty()) {
                receiptId = req.getParameter("id");
            }

            String filterType = req.getParameter("filterType");
            if (filterType == null || filterType.trim().isEmpty()) {
                filterType = req.getParameter("period");
            }

            String date = req.getParameter("date");
            String monthStr = req.getParameter("month");
            String yearStr = req.getParameter("year");
            String fromDate = req.getParameter("fromDate");
            String toDate = req.getParameter("toDate");

            Integer month = null;
            if (monthStr != null && !monthStr.trim().isEmpty()) {
                try {
                    month = Integer.parseInt(monthStr.trim());
                } catch (NumberFormatException ignored) {}
            }

            Integer year = null;
            if (yearStr != null && !yearStr.trim().isEmpty()) {
                try {
                    year = Integer.parseInt(yearStr.trim());
                } catch (NumberFormatException ignored) {}
            }

            resp.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            String filename = reportService.exportExcel(target, receiptId, filterType, date, month, year, fromDate, toDate, baos);

            resp.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
            resp.setContentLength(baos.size());
            resp.getOutputStream().write(baos.toByteArray());
            resp.getOutputStream().flush();
        } catch (Exception e) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi khi xuất file Excel: " + e.getMessage());
        }
    }
}
