package com.mycompany.quanlysieuthi.modules.invoice;

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
 * Controller handling Invoice endpoints:
 * POST   /api/invoice           - Tạo hóa đơn mới
 * GET    /api/invoice           - Lấy danh sách hóa đơn
 * GET    /api/invoice/:id       - Lấy chi tiết hóa đơn
 * DELETE /api/invoice/:id       - Hủy hóa đơn
 */
@WebServlet(name = "InvoiceController", urlPatterns = {"/api/invoice", "/api/invoice/*"})
public class InvoiceController extends HttpServlet {

    private final InvoiceService invoiceService = new InvoiceService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo != null && !pathInfo.equals("/") && !pathInfo.isEmpty()) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found: " + pathInfo);
            return;
        }

        try {
            InvoiceRequest.Create request = RequestUtil.parseJson(req, InvoiceRequest.Create.class);
            if (request == null) {
                ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Request body không được để trống");
                return;
            }

            Claims claims = (Claims) req.getAttribute("currentUser");
            String employeeId = claims != null ? claims.get("employeeId", String.class) : "";
            String employeeFullName = claims != null ? claims.get("fullName", String.class) : "";

            InvoiceResponse.CreateResponse responseData = invoiceService.createInvoice(request, employeeId, employeeFullName);
            ResponseUtil.sendSuccess(resp, HttpServletResponse.SC_CREATED, "Tạo hóa đơn thành công", responseData);

        } catch (JsonSyntaxException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Malformed JSON payload: " + e.getMessage());
        } catch (NotFoundException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi hệ thống khi tạo hóa đơn: " + e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/") || pathInfo.isEmpty()) {
            handleGetInvoices(req, resp);
        } else {
            String id = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
            if (id.endsWith("/")) {
                id = id.substring(0, id.length() - 1);
            }
            if (id.isEmpty()) {
                handleGetInvoices(req, resp);
            } else {
                handleGetInvoiceDetail(id, resp);
            }
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/") || pathInfo.isEmpty()) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Thiếu ID hóa đơn trên URL");
            return;
        }

        String id = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
        if (id.endsWith("/")) {
            id = id.substring(0, id.length() - 1);
        }
        if (id.isEmpty()) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Thiếu ID hóa đơn trên URL");
            return;
        }

        try {
            InvoiceResponse.CancelResponse result = invoiceService.cancelInvoice(id);
            ResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, "Hủy hóa đơn thành công", result);

        } catch (NotFoundException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (IllegalStateException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi hệ thống khi hủy hóa đơn: " + e.getMessage());
        }
    }

    private void handleGetInvoices(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            int page = RequestUtil.parsePage(req);
            int limit = RequestUtil.parseLimit(req);
            String status = req.getParameter("status");
            String fromDate = req.getParameter("fromDate");
            String toDate = req.getParameter("toDate");
            String customerId = req.getParameter("customerId");
            String employeeId = req.getParameter("employeeId");

            List<InvoiceResponse.ListItemDto> invoices = invoiceService.getInvoices(
                    page, limit, status, fromDate, toDate, customerId, employeeId
            );

            ResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, "Lấy danh sách hóa đơn thành công", invoices);

        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi hệ thống khi lấy danh sách hóa đơn: " + e.getMessage());
        }
    }

    private void handleGetInvoiceDetail(String id, HttpServletResponse resp) throws IOException {
        try {
            InvoiceResponse.DetailResponse detail = invoiceService.getInvoiceDetail(id);
            ResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, "Lấy chi tiết hóa đơn thành công", detail);

        } catch (NotFoundException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi hệ thống khi lấy chi tiết hóa đơn: " + e.getMessage());
        }
    }
}
