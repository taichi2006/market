package com.mycompany.quanlysieuthi.modules.invoice;

import com.mycompany.quanlysieuthi.common.base.BaseController;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Controller handling Invoice endpoints:
 * POST   /api/invoice           - Tạo hóa đơn mới
 * GET    /api/invoice           - Lấy danh sách hóa đơn
 * GET    /api/invoice/:id       - Lấy chi tiết hóa đơn
 * DELETE /api/invoice/:id       - Hủy hóa đơn
 */
@WebServlet(name = "InvoiceController", urlPatterns = {"/api/invoice", "/api/invoice/*"})
public class InvoiceController extends BaseController {

    private final InvoiceService invoiceService = new InvoiceService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo != null && !pathInfo.equals("/") && !pathInfo.isEmpty()) {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found: " + pathInfo);
            return;
        }

        handle(resp, HttpServletResponse.SC_CREATED, () -> {
            InvoiceRequest.Create request = parseBody(req, InvoiceRequest.Create.class);
            if (request == null) {
                throw new IllegalArgumentException("Request body không được để trống");
            }

            String employeeId = getCurrentEmployeeId(req);
            String employeeFullName = getCurrentFullName(req);

            return invoiceService.createInvoice(request, employeeId, employeeFullName);
        }, "Tạo hóa đơn thành công", null);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String id = extractPathId(req);
        if (id == null || id.isEmpty() || id.equalsIgnoreCase("invoice")) {
            handleGetInvoices(req, resp);
        } else {
            handleGetInvoiceDetail(id, resp);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String id = extractPathId(req);
        if (id == null || id.isEmpty() || id.equalsIgnoreCase("invoice")) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Thiếu ID hóa đơn trên URL");
            return;
        }

        handle(resp, () -> invoiceService.cancelInvoice(id), "Hủy hóa đơn thành công");
    }

    private void handleGetInvoices(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handle(resp, () -> {
            int page = parsePage(req);
            int limit = parseLimit(req);
            String status = req.getParameter("status");
            String fromDate = req.getParameter("fromDate");
            String toDate = req.getParameter("toDate");
            String customerId = req.getParameter("customerId");
            String employeeId = req.getParameter("employeeId");

            return invoiceService.getInvoices(
                    page, limit, status, fromDate, toDate, customerId, employeeId
            );
        }, "Lấy danh sách hóa đơn thành công");
    }

    private void handleGetInvoiceDetail(String id, HttpServletResponse resp) throws IOException {
        handle(resp, () -> invoiceService.getInvoiceDetail(id), "Lấy chi tiết hóa đơn thành công");
    }
}
