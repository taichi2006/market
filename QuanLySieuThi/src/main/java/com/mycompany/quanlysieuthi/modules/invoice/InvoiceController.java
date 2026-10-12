package com.mycompany.quanlysieuthi.modules.invoice;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.mycompany.quanlysieuthi.modules.invoice.dto.CancelInvoiceResponseData;
import com.mycompany.quanlysieuthi.modules.invoice.dto.CreateInvoiceRequest;
import com.mycompany.quanlysieuthi.modules.invoice.dto.CreateInvoiceResponseData;
import com.mycompany.quanlysieuthi.modules.invoice.dto.InvoiceDetailResponse;
import com.mycompany.quanlysieuthi.modules.invoice.dto.InvoiceListItemDto;
import com.mycompany.quanlysieuthi.util.NotFoundException;
import com.mycompany.quanlysieuthi.util.ResponseUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Controller handling Invoice endpoints:
 * GET   /api/invoice (or /api/invoice/)
 * POST  /api/invoice (or /api/invoice/)
 * GET   /api/invoice/:id
 * PATCH /api/invoice/:id/cancel
 */
@WebServlet(name = "InvoiceController", urlPatterns = {"/api/invoice", "/api/invoice/*"})
public class InvoiceController extends HttpServlet {

    private final InvoiceService invoiceService = new InvoiceService();
    private final Gson gson = new Gson();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("PATCH".equalsIgnoreCase(req.getMethod())) {
            doPatch(req, resp);
        } else {
            super.service(req, resp);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.isEmpty() || pathInfo.equals("/")) {
            handleGetList(req, resp);
            return;
        }

        String path = pathInfo.startsWith("/") ? pathInfo.substring(1).trim() : pathInfo.trim();
        String[] segments = path.split("/");

        if (segments.length == 1 && !segments[0].isEmpty()) {
            // GET /api/invoice/:id
            handleGetDetail(segments[0], resp);
        } else {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found: " + pathInfo);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.isEmpty() || pathInfo.equals("/")) {
            handleCreate(req, resp);
        } else {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found: " + pathInfo);
        }
    }

    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.isEmpty()) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            return;
        }

        String path = pathInfo.startsWith("/") ? pathInfo.substring(1).trim() : pathInfo.trim();
        String[] segments = path.split("/");

        if (segments.length == 2 && "cancel".equalsIgnoreCase(segments[1])) {
            // PATCH /api/invoice/:id/cancel
            handleCancel(segments[0], resp);
        } else {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found: " + pathInfo);
        }
    }

    private void handleCreate(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            CreateInvoiceRequest requestDto = gson.fromJson(req.getReader(), CreateInvoiceRequest.class);
            if (requestDto == null) {
                ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Request body không được để trống");
                return;
            }

            // Extract employeeId from JWT claims
            Claims claims = (Claims) req.getAttribute("currentUser");
            String employeeId = claims != null ? claims.get("employeeId", String.class) : null;
            if (employeeId == null || employeeId.trim().isEmpty()) {
                ResponseUtil.sendError(resp, HttpServletResponse.SC_UNAUTHORIZED,
                        "Không xác định được nhân viên đăng nhập từ token");
                return;
            }

            CreateInvoiceResponseData result = invoiceService.createInvoice(requestDto, employeeId);
            ResponseUtil.sendSuccess(resp, HttpServletResponse.SC_CREATED, "Tạo hóa đơn thành công", result);

        } catch (JsonSyntaxException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Định dạng JSON không hợp lệ: " + e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (NotFoundException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi hệ thống khi tạo hóa đơn: " + e.getMessage());
        }
    }

    private void handleGetList(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Integer page = null;
            Integer limit = null;

            String pageStr = req.getParameter("page");
            if (pageStr != null && !pageStr.trim().isEmpty()) {
                try {
                    page = Integer.parseInt(pageStr.trim());
                } catch (NumberFormatException e) {
                    ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "page phải là số nguyên");
                    return;
                }
            }

            String limitStr = req.getParameter("limit");
            if (limitStr != null && !limitStr.trim().isEmpty()) {
                try {
                    limit = Integer.parseInt(limitStr.trim());
                } catch (NumberFormatException e) {
                    ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "limit phải là số nguyên");
                    return;
                }
            }

            String status = req.getParameter("status");
            String fromDate = req.getParameter("fromDate");
            String toDate = req.getParameter("toDate");
            String customerId = req.getParameter("customerId");
            String employeeId = req.getParameter("employeeId");

            List<InvoiceListItemDto> list = invoiceService.getInvoices(page, limit, status, fromDate, toDate, customerId, employeeId);
            ResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, "Lấy danh sách hóa đơn thành công", list);

        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi hệ thống khi lấy danh sách hóa đơn: " + e.getMessage());
        }
    }

    private void handleGetDetail(String invoiceId, HttpServletResponse resp) throws IOException {
        try {
            InvoiceDetailResponse detail = invoiceService.getInvoiceDetail(invoiceId);
            ResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, "Lấy chi tiết hóa đơn thành công", detail);
        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (NotFoundException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi hệ thống khi lấy chi tiết hóa đơn: " + e.getMessage());
        }
    }

    private void handleCancel(String invoiceId, HttpServletResponse resp) throws IOException {
        try {
            CancelInvoiceResponseData result = invoiceService.cancelInvoice(invoiceId);
            ResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, "Hủy hóa đơn thành công", result);
        } catch (IllegalArgumentException | IllegalStateException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (NotFoundException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi hệ thống khi hủy hóa đơn: " + e.getMessage());
        }
    }
}
