package com.mycompany.quanlysieuthi.modules.customer;

import com.google.gson.JsonSyntaxException;
import com.mycompany.quanlysieuthi.common.util.RequestUtil;
import com.mycompany.quanlysieuthi.util.ConflictException;
import com.mycompany.quanlysieuthi.util.NotFoundException;
import com.mycompany.quanlysieuthi.util.ResponseUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
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
public class CustomerController extends HttpServlet {

    private final CustomerService customerService = new CustomerService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo != null && !pathInfo.equals("/") && !pathInfo.isEmpty()) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found: " + pathInfo);
            return;
        }

        try {
            CustomerRequest.Create request = RequestUtil.parseJson(req, CustomerRequest.Create.class);
            if (request == null) {
                ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Request body không được để trống");
                return;
            }

            CustomerResponse result = customerService.createCustomer(request);
            ResponseUtil.sendSuccess(resp, HttpServletResponse.SC_CREATED, "Tạo khách hàng thành công", result);

        } catch (JsonSyntaxException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Malformed JSON payload: " + e.getMessage());
        } catch (ConflictException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_CONFLICT, e.getMessage());
        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi hệ thống khi tạo khách hàng: " + e.getMessage());
        }
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
            ResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found: " + pathInfo);
        }
    }

    private void handleGetCustomers(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            int page = RequestUtil.parsePage(req);
            int limit = RequestUtil.parseLimit(req);
            String keyword = req.getParameter("keyword");

            List<CustomerResponse> customers = customerService.getCustomers(page, limit, keyword);
            ResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, "Lấy danh sách khách hàng thành công", customers);

        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi hệ thống khi lấy danh sách khách hàng: " + e.getMessage());
        }
    }

    private void handleGetCustomerInvoices(String customerId, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            int page = RequestUtil.parsePage(req);
            int limit = RequestUtil.parseLimit(req);
            String status = req.getParameter("status");
            String fromDate = req.getParameter("fromDate");
            String toDate = req.getParameter("toDate");

            List<CustomerResponse.InvoiceHistoryDto> invoices = customerService.getCustomerInvoices(
                    customerId, page, limit, status, fromDate, toDate
            );

            ResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, "Lấy lịch sử mua hàng thành công", invoices);

        } catch (NotFoundException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi hệ thống khi lấy lịch sử mua hàng: " + e.getMessage());
        }
    }
}
