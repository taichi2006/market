package com.mycompany.quanlysieuthi.modules.payment;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.mycompany.quanlysieuthi.common.util.RequestUtil;
import com.mycompany.quanlysieuthi.util.ConflictException;
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
 * Controller handling payment endpoints:
 * 1. POST /api/payment/process
 * 2. GET  /api/payment/vnpay-ipn
 * 3. GET  /api/payment/vnpay-return
 * 4. GET  /api/payment/transactions
 * 5. GET  /api/payment/summary
 * 6. GET  /api/payment/history/:invoiceId
 */
@WebServlet(name = "PaymentController", urlPatterns = {"/api/payment/*", "/api/payment"})
public class PaymentController extends HttpServlet {

    private final PaymentService paymentService = new PaymentService();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            return;
        }

        if ("/process".equalsIgnoreCase(pathInfo)) {
            handleProcessPayment(req, resp);
        } else {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found: " + pathInfo);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            return;
        }

        if ("/vnpay-ipn".equalsIgnoreCase(pathInfo)) {
            handleVnPayIpn(req, resp);
        } else if ("/vnpay-return".equalsIgnoreCase(pathInfo)) {
            handleVnPayReturn(req, resp);
        } else if ("/transactions".equalsIgnoreCase(pathInfo)) {
            handleGetTransactions(req, resp);
        } else if ("/summary".equalsIgnoreCase(pathInfo)) {
            handleGetSummary(req, resp);
        } else if (pathInfo.startsWith("/history/")) {
            String invoiceId = pathInfo.substring("/history/".length()).trim();
            handleGetHistory(invoiceId, resp);
        } else {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found: " + pathInfo);
        }
    }

    private void handleProcessPayment(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            PaymentRequest.Process requestDto = RequestUtil.parseJson(req, PaymentRequest.Process.class);
            if (requestDto == null) {
                ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Request body không được để trống");
                return;
            }

            String clientIp = RequestUtil.extractClientIp(req);
            Object result = paymentService.processPayment(requestDto, clientIp);

            ResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, "Khởi tạo thanh toán thành công", result);

        } catch (JsonSyntaxException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Malformed JSON payload: " + e.getMessage());
        } catch (ConflictException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_CONFLICT, e.getMessage());
        } catch (NotFoundException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi hệ thống khi xử lý thanh toán: " + e.getMessage());
        }
    }

    private void handleVnPayIpn(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        PaymentResponse.IpnResponse ipnResponse = paymentService.handleVnPayIpn(req.getParameterMap());
        resp.getWriter().write(gson.toJson(ipnResponse));
    }

    private void handleVnPayReturn(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        boolean success = paymentService.handleVnPayReturn(req.getParameterMap());
        String transactionId = req.getParameter("vnp_TxnRef");
        String responseCode = req.getParameter("vnp_ResponseCode");

        if (success) {
            ResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK,
                    "Thanh toán trực tuyến VNPay thành công (TxnRef: " + transactionId + ")");
        } else {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "Giao dịch thanh toán không thành công hoặc chữ ký không hợp lệ (Mã phản hồi: " + responseCode + ")");
        }
    }

    private void handleGetTransactions(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            int page = RequestUtil.parsePage(req);
            int limit = RequestUtil.parseLimit(req);
            String paymentMethod = req.getParameter("paymentMethod");
            String status = req.getParameter("status");
            String fromDate = req.getParameter("fromDate");
            String toDate = req.getParameter("toDate");

            Claims claims = (Claims) req.getAttribute("currentUser");
            String currentEmployeeId = (claims != null) ? claims.get("employeeId", String.class) : null;
            String currentRole = (claims != null) ? claims.get("position", String.class) : null;

            List<PaymentResponse.TransactionDto> list = paymentService.getTransactions(
                    page, limit, paymentMethod, status, fromDate, toDate, currentEmployeeId, currentRole
            );

            ResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, "Lấy danh sách giao dịch thành công", list);

        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi hệ thống khi lấy danh sách giao dịch: " + e.getMessage());
        }
    }

    private void handleGetSummary(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String fromDate = req.getParameter("fromDate");
            String toDate = req.getParameter("toDate");

            Claims claims = (Claims) req.getAttribute("currentUser");
            String currentEmployeeId = (claims != null) ? claims.get("employeeId", String.class) : null;
            String currentRole = (claims != null) ? claims.get("position", String.class) : null;

            PaymentResponse.SummaryDto summary = paymentService.getSummary(
                    fromDate, toDate, currentEmployeeId, currentRole
            );

            ResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, "Lấy thông tin tổng kết doanh thu thành công", summary);

        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi hệ thống khi tính tổng kết doanh thu: " + e.getMessage());
        }
    }

    private void handleGetHistory(String invoiceId, HttpServletResponse resp) throws IOException {
        try {
            PaymentResponse.HistoryResponse history = paymentService.getInvoicePaymentHistory(invoiceId);
            ResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, "Lấy lịch sử thanh toán hóa đơn thành công", history);

        } catch (NotFoundException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi hệ thống khi lấy lịch sử thanh toán hóa đơn: " + e.getMessage());
        }
    }
}
