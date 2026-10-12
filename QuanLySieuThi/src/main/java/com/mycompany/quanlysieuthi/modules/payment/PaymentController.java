package com.mycompany.quanlysieuthi.modules.payment;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.mycompany.quanlysieuthi.modules.payment.dto.*;
import com.mycompany.quanlysieuthi.util.NotFoundException;
import com.mycompany.quanlysieuthi.util.ResponseUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Controller xử lý các yêu cầu HTTP liên quan đến thanh toán hóa đơn:
 * POST /api/payment/process       - Thực hiện thanh toán (Tiền mặt hoặc sinh link VNPay)
 * GET  /api/payment               - Xem lịch sử giao dịch và thống kê doanh thu
 * GET  /api/payment/vnpay-return  - Nhận redirect trả về từ cổng VNPay cho trình duyệt khách hàng
 * GET  /api/payment/vnpay-ipn     - Webhook IPN nhận kết quả server-to-server từ VNPay
 */
@WebServlet(name = "PaymentController", urlPatterns = {"/api/payment", "/api/payment/*"})
public class PaymentController extends HttpServlet {

    private final PaymentService paymentService = new PaymentService();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo != null && (pathInfo.equals("/process") || pathInfo.equals("/process/"))) {
            xuLyThanhToan(req, resp);
        } else {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found: " + pathInfo);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.isEmpty() || pathInfo.equals("/")) {
            layLichSuThanhToan(req, resp);
        } else if (pathInfo.equals("/vnpay-return") || pathInfo.equals("/vnpay-return/")) {
            xuLyKetQuaVnPayReturn(req, resp);
        } else if (pathInfo.equals("/vnpay-ipn") || pathInfo.equals("/vnpay-ipn/")) {
            xuLyWebhookVnPayIpn(req, resp);
        } else {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found: " + pathInfo);
        }
    }

    /**
     * Tiếp nhận và xử lý yêu cầu thanh toán (POST /api/payment/process).
     */
    private void xuLyThanhToan(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            PaymentProcessRequest requestDto = gson.fromJson(req.getReader(), PaymentProcessRequest.class);
            if (requestDto == null) {
                ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Request body không được để trống");
                return;
            }

            // Lấy địa chỉ IP của client gửi yêu cầu
            String clientIp = req.getRemoteAddr();
            String forwardedFor = req.getHeader("X-Forwarded-For");
            if (forwardedFor != null && !forwardedFor.trim().isEmpty()) {
                clientIp = forwardedFor.split(",")[0].trim();
            }

            Object result = paymentService.processPayment(requestDto, clientIp);
            if (result instanceof PaymentCashResponseData) {
                ResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, "Thanh toán thành công", result);
            } else {
                ResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, "Tạo đường dẫn thanh toán VNPay thành công", result);
            }

        } catch (JsonSyntaxException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Malformed JSON payload: " + e.getMessage());
        } catch (NotFoundException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (SecurityException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi hệ thống khi xử lý thanh toán: " + e.getMessage());
        }
    }

    /**
     * Lấy lịch sử giao dịch thanh toán và thống kê doanh thu (GET /api/payment).
     */
    private void layLichSuThanhToan(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Integer page = null;
            Integer limit = null;

            String pageStr = req.getParameter("page");
            if (pageStr != null && !pageStr.trim().isEmpty()) {
                try {
                    page = Integer.parseInt(pageStr.trim());
                } catch (NumberFormatException e) {
                    ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Tham số page phải là số nguyên");
                    return;
                }
            }

            String limitStr = req.getParameter("limit");
            if (limitStr != null && !limitStr.trim().isEmpty()) {
                try {
                    limit = Integer.parseInt(limitStr.trim());
                } catch (NumberFormatException e) {
                    ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Tham số limit phải là số nguyên");
                    return;
                }
            }

            String paymentMethod = req.getParameter("paymentMethod");
            String status = req.getParameter("status");
            String fromDate = req.getParameter("fromDate");
            String toDate = req.getParameter("toDate");

            // Lấy thông tin tài khoản đang đăng nhập từ Claims token JWT
            Claims claims = (Claims) req.getAttribute("currentUser");
            String employeeId = claims != null ? claims.get("employeeId", String.class) : "";
            String position = claims != null ? claims.get("position", String.class) : "";

            PaymentHistoryResponseData data = paymentService.getPaymentHistory(
                    page, limit, paymentMethod, status, fromDate, toDate, employeeId, position
            );

            ResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, "Lấy lịch sử thanh toán thành công", data);

        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (SecurityException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi hệ thống khi lấy lịch sử thanh toán: " + e.getMessage());
        }
    }

    /**
     * Nhận callback redirect từ trình duyệt khi khách thanh toán xong trên VNPay (GET /api/payment/vnpay-return).
     */
    private void xuLyKetQuaVnPayReturn(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            PaymentCashResponseData result = paymentService.processVnPayReturn(req.getParameterMap());
            ResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, "Kết quả thanh toán VNPay", result);
        } catch (NotFoundException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (SecurityException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi hệ thống khi xử lý kết quả VNPay: " + e.getMessage());
        }
    }

    /**
     * Nhận IPN ngầm từ server VNPay gửi sang (GET /api/payment/vnpay-ipn).
     */
    private void xuLyWebhookVnPayIpn(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            VnPayIpnResponse ipnResponse = paymentService.processVnPayIpn(req.getParameterMap());
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(gson.toJson(ipnResponse));
            resp.getWriter().flush();
        } catch (Exception e) {
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(gson.toJson(new VnPayIpnResponse("99", "Lỗi không xác định: " + e.getMessage())));
            resp.getWriter().flush();
        }
    }
}
