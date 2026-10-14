package com.mycompany.quanlysieuthi.modules.payment;

import com.google.gson.Gson;
import com.mycompany.quanlysieuthi.common.base.BaseController;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

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
public class PaymentController extends BaseController {

    private final PaymentService paymentService = new PaymentService();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            return;
        }

        if ("/process".equalsIgnoreCase(pathInfo)) {
            handleProcessPayment(req, resp);
        } else {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found: " + pathInfo);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
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
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found: " + pathInfo);
        }
    }

    private void handleProcessPayment(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handle(resp, () -> {
            PaymentRequest.Process requestDto = parseBody(req, PaymentRequest.Process.class);
            if (requestDto == null) {
                throw new IllegalArgumentException("Request body không được để trống");
            }

            String clientIp = getClientIp(req);
            return paymentService.processPayment(requestDto, clientIp);
        }, "Khởi tạo thanh toán thành công");
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

        String invoiceId = "";
        try {
            Optional<com.mycompany.quanlysieuthi.entity.Payment> pOpt = paymentService.getPaymentRepository().findPaymentByTransactionId(transactionId);
            if (pOpt.isPresent()) {
                invoiceId = pOpt.get().getInvoiceId();
            }
        } catch (Exception ignored) {}

        String frontendHost = System.getenv("FRONTEND_URL");
        if (frontendHost == null || frontendHost.trim().isEmpty()) {
            frontendHost = "http://localhost:5173";
        }
        if (frontendHost.endsWith("/")) {
            frontendHost = frontendHost.substring(0, frontendHost.length() - 1);
        }

        String redirectUrl = frontendHost + "/cashier";
        if (success) {
            resp.sendRedirect(redirectUrl + "?vnpay_status=success&invoice_id=" + (invoiceId != null ? invoiceId : "") + "&txnRef=" + (transactionId != null ? transactionId : ""));
        } else {
            resp.sendRedirect(redirectUrl + "?vnpay_status=cancel&invoice_id=" + (invoiceId != null ? invoiceId : "") + "&txnRef=" + (transactionId != null ? transactionId : "") + "&code=" + (responseCode != null ? responseCode : ""));
        }
    }

    private void handleGetTransactions(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handle(resp, () -> {
            int page = parsePage(req);
            int limit = parseLimit(req);
            String paymentMethod = req.getParameter("paymentMethod");
            String status = req.getParameter("status");
            String fromDate = req.getParameter("fromDate");
            String toDate = req.getParameter("toDate");

            String currentEmployeeId = getCurrentEmployeeId(req);
            String currentRole = getCurrentPosition(req);

            return paymentService.getTransactions(
                    page, limit, paymentMethod, status, fromDate, toDate, currentEmployeeId, currentRole
            );
        }, "Lấy danh sách giao dịch thành công");
    }

    private void handleGetSummary(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handle(resp, () -> {
            String fromDate = req.getParameter("fromDate");
            String toDate = req.getParameter("toDate");

            String currentEmployeeId = getCurrentEmployeeId(req);
            String currentRole = getCurrentPosition(req);

            return paymentService.getSummary(
                    fromDate, toDate, currentEmployeeId, currentRole
            );
        }, "Lấy thông tin tổng kết doanh thu thành công");
    }

    private void handleGetHistory(String invoiceId, HttpServletResponse resp) throws IOException {
        handle(resp, () -> paymentService.getInvoicePaymentHistory(invoiceId), "Lấy lịch sử thanh toán hóa đơn thành công");
    }
}
