package com.mycompany.quanlysieuthi.modules.report;

import com.google.gson.JsonSyntaxException;
import com.mycompany.quanlysieuthi.common.util.RequestUtil;
import com.mycompany.quanlysieuthi.util.NotFoundException;
import com.mycompany.quanlysieuthi.util.ResponseUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Controller handling report endpoints:
 * POST /api/report/stock-discrepancy
 * GET  /api/report/stock-discrepancy
 * GET  /api/report/stock-discrepancy/:id
 */
@WebServlet(name = "ReportController", urlPatterns = {"/api/report/*", "/api/report"})
public class ReportController extends HttpServlet {

    private final ReportService reportService = new ReportService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            return;
        }

        if (pathInfo.equals("/stock-discrepancy") || pathInfo.equals("/stock-discrepancy/")) {
            handleGetStockDiscrepancyList(req, resp);
        } else if (pathInfo.startsWith("/stock-discrepancy/")) {
            String id = pathInfo.substring("/stock-discrepancy/".length()).trim();
            if (id.isEmpty()) {
                handleGetStockDiscrepancyList(req, resp);
            } else {
                handleGetStockDiscrepancyDetail(id, resp);
            }
        } else {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found: " + pathInfo);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo != null && (pathInfo.equals("/stock-discrepancy") || pathInfo.equals("/stock-discrepancy/"))) {
            handleCreateStockAudit(req, resp);
        } else {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found: " + pathInfo);
        }
    }

    private void handleCreateStockAudit(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            ReportRequest.StockAudit requestDto = RequestUtil.parseJson(req, ReportRequest.StockAudit.class);
            if (requestDto == null) {
                ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Request body không được để trống");
                return;
            }

            Claims claims = (Claims) req.getAttribute("currentUser");
            String employeeId = claims != null ? claims.get("employeeId", String.class) : "";
            String fullName = claims != null ? claims.get("fullName", String.class) : "";

            ReportResponse.StockAuditResponse responseDto = reportService.createStockAuditReport(
                    requestDto,
                    employeeId,
                    fullName
            );

            ResponseUtil.sendSuccess(resp, HttpServletResponse.SC_CREATED,
                    "Tạo báo cáo kiểm kê kho thành công, đã gửi yêu cầu xử lý đến Chủ cửa hàng", responseDto);

        } catch (JsonSyntaxException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Malformed JSON payload: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (NotFoundException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi hệ thống khi tạo biên bản kiểm kê kho: " + e.getMessage());
        }
    }

    private void handleGetStockDiscrepancyList(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            int page = RequestUtil.parsePage(req);
            int limit = RequestUtil.parseLimit(req);

            String employeeId = req.getParameter("employeeId");
            String fromDate = req.getParameter("fromDate");
            String toDate = req.getParameter("toDate");

            List<ReportResponse.ListItemDto> data = reportService.getStockDiscrepancyReports(
                    page, limit, employeeId, fromDate, toDate
            );

            ResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, "Lấy danh sách báo cáo kiểm kê kho thành công", data);

        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi hệ thống khi lấy danh sách báo cáo kiểm kê kho: " + e.getMessage());
        }
    }

    private void handleGetStockDiscrepancyDetail(String id, HttpServletResponse resp) throws IOException {
        try {
            ReportResponse.DetailResponse data = reportService.getStockDiscrepancyDetail(id);
            ResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, "Lấy chi tiết báo cáo kiểm kê kho thành công", data);

        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (NotFoundException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi hệ thống khi lấy chi tiết báo cáo kiểm kê kho: " + e.getMessage());
        }
    }
}
