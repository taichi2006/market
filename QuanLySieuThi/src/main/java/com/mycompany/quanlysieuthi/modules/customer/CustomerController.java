package com.mycompany.quanlysieuthi.modules.customer;

import com.mycompany.quanlysieuthi.common.base.BaseController;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Controller handling customer endpoints:
 * POST /api/customer - Tạo khách hàng mới
 * GET  /api/customer - Lấy danh sách khách hàng (tìm kiếm & phân trang)
 * GET  /api/customer/:id/invoices - Lấy lịch sử hóa đơn của khách hàng
 */
@WebServlet(name = "CustomerController", urlPatterns = {"/api/customer", "/api/customer/*"})
public class CustomerController extends BaseController {

    private final CustomerService customerService = new CustomerService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo != null && !pathInfo.equals("/") && !pathInfo.isEmpty()) {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found: " + pathInfo);
            return;
        }

        handle(resp, HttpServletResponse.SC_CREATED, () -> {
            CustomerRequest.Create request = parseBody(req, CustomerRequest.Create.class);
            if (request == null) {
                throw new IllegalArgumentException("Request body không được để trống");
            }
            return customerService.createCustomer(request);
        }, "Tạo khách hàng thành công", null);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/") || pathInfo.isEmpty()) {
            // GET /api/customer
            handleGetCustomers(req, resp);
        } else if (pathInfo.matches("^/[^/]+/invoices/?$")) {
            // GET /api/customer/:id/invoices
            String[] segments = pathInfo.split("/");
            String customerId = segments[1];
            handleGetCustomerInvoices(customerId, req, resp);
        } else {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found: " + pathInfo);
        }
    }

    private void handleGetCustomers(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handle(resp, () -> {
            int page = parsePage(req);
            int limit = parseLimit(req);
            String keyword = req.getParameter("keyword");

            return customerService.getCustomers(page, limit, keyword);
        }, "Lấy danh sách khách hàng thành công");
    }

    private void handleGetCustomerInvoices(String customerId, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handle(resp, () -> {
            int page = parsePage(req);
            int limit = parseLimit(req);
            String status = req.getParameter("status");
            String fromDate = req.getParameter("fromDate");
            String toDate = req.getParameter("toDate");

            return customerService.getCustomerInvoices(customerId, page, limit, status, fromDate, toDate);
        }, "Lấy lịch sử mua hàng thành công");
    }
}
