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
 * POST /api/customer/redeem-points - Đổi điểm thưởng trừ vào hóa đơn
 * GET  /api/customer - Lấy danh sách khách hàng (tìm kiếm & phân trang)
 * GET  /api/customer/summary - Lấy tổng hợp chi tiêu và phân hạng thành viên
 * GET  /api/customer/:id/invoices - Lấy lịch sử hóa đơn của khách hàng
 * GET  /api/customer/:id/transactions - Lấy lịch sử giao dịch thanh toán chi tiết của khách hàng
 */
@WebServlet(name = "CustomerController", urlPatterns = {"/api/customer", "/api/customer/*"})
public class CustomerController extends BaseController {

    private final CustomerService customerService = new CustomerService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/") || pathInfo.isEmpty()) {
            handleCreateCustomer(req, resp);
        } else if ("/redeem-points".equalsIgnoreCase(pathInfo)) {
            handleRedeemPoints(req, resp, null);
        } else if (pathInfo.matches("^/[^/]+/redeem-points/?$")) {
            String[] segments = pathInfo.split("/");
            String customerId = segments[1];
            handleRedeemPoints(req, resp, customerId);
        } else {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found: " + pathInfo);
        }
    }

    private void handleCreateCustomer(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handle(resp, HttpServletResponse.SC_CREATED, () -> {
            CustomerRequest.Create request = parseBody(req, CustomerRequest.Create.class);
            if (request == null) {
                throw new IllegalArgumentException("Request body không được để trống");
            }
            return customerService.createCustomer(request);
        }, "Tạo khách hàng thành công", null);
    }

    private void handleRedeemPoints(HttpServletRequest req, HttpServletResponse resp, String pathCustomerId) throws IOException {
        handle(resp, () -> {
            CustomerRequest.RedeemPoints request = parseBody(req, CustomerRequest.RedeemPoints.class);
            if (request == null) {
                throw new IllegalArgumentException("Request body không được để trống");
            }
            String custId = (pathCustomerId != null && !pathCustomerId.trim().isEmpty())
                    ? pathCustomerId.trim() : request.getCustomerId();
            CustomerRequest.RedeemPoints finalReq = new CustomerRequest.RedeemPoints(custId, request.getInvoiceId(), request.getPoints());
            return customerService.redeemLoyaltyPoints(finalReq);
        }, "Đổi điểm thưởng thành công");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/") || pathInfo.isEmpty()) {
            // GET /api/customer
            handleGetCustomers(req, resp);
        } else if ("/summary".equalsIgnoreCase(pathInfo)) {
            // GET /api/customer/summary
            handleGetCustomerSummaries(req, resp);
        } else if (pathInfo.matches("^/[^/]+/invoices/?$")) {
            // GET /api/customer/:id/invoices
            String[] segments = pathInfo.split("/");
            String customerId = segments[1];
            handleGetCustomerInvoices(customerId, req, resp);
        } else if (pathInfo.matches("^/[^/]+/transactions/?$")) {
            // GET /api/customer/:id/transactions
            String[] segments = pathInfo.split("/");
            String customerId = segments[1];
            handleGetCustomerTransactions(customerId, req, resp);
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

    private void handleGetCustomerSummaries(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handle(resp, () -> {
            int page = parsePage(req);
            int limit = parseLimit(req);
            String keyword = req.getParameter("keyword");

            return customerService.getCustomerSummaries(page, limit, keyword);
        }, "Lấy danh sách tổng hợp chi tiêu khách hàng thành công");
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

    private void handleGetCustomerTransactions(String customerId, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handle(resp, () -> {
            String fromDate = req.getParameter("fromDate");
            String toDate = req.getParameter("toDate");

            return customerService.getCustomerTransactions(customerId, fromDate, toDate);
        }, "Lấy lịch sử giao dịch khách hàng thành công");
    }
}

