package com.mycompany.quanlysieuthi.modules.payment;

import com.mycompany.quanlysieuthi.modules.customer.Invoice;
import com.mycompany.quanlysieuthi.modules.payment.dto.*;
import com.mycompany.quanlysieuthi.util.NotFoundException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Lớp xử lý nghiệp vụ thanh toán (Service) hỗ trợ tiền mặt (CASH) và cổng VNPay Sandbox.
 */
public class PaymentService {

    private final PaymentDao paymentDao;

    public PaymentService() {
        this.paymentDao = new PaymentDao();
    }

    public PaymentService(PaymentDao paymentDao) {
        this.paymentDao = paymentDao;
    }

    /**
     * Xử lý yêu cầu thanh toán hóa đơn:
     * - Tiền mặt (CASH): Lưu bản ghi thanh toán PAID và cập nhật trạng thái hóa đơn PAID.
     * - VNPay (QR / BANK_TRANSFER): Lưu bản ghi UNPAID và sinh đường link thanh toán VNPay Sandbox.
     *
     * @param request  Dữ liệu yêu cầu thanh toán (invoiceId, paymentMethod, amount)
     * @param clientIp Địa chỉ IP của máy khách gửi yêu cầu
     * @return PaymentCashResponseData (nếu tiền mặt) hoặc PaymentVnpayResponseData (nếu VNPay)
     */
    public Object processPayment(PaymentProcessRequest request, String clientIp) {
        if (request == null) {
            throw new IllegalArgumentException("Request body không được để trống");
        }
        if (request.getInvoiceId() == null || request.getInvoiceId().trim().isEmpty()) {
            throw new IllegalArgumentException("invoiceId không được để trống");
        }
        if (request.getPaymentMethod() == null || request.getPaymentMethod().trim().isEmpty()) {
            throw new IllegalArgumentException("paymentMethod không được để trống");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("amount phải lớn hơn hoặc bằng 0");
        }

        String invoiceId = request.getInvoiceId().trim();
        Invoice invoice = paymentDao.findInvoiceById(invoiceId)
                .orElseThrow(() -> new NotFoundException("invoiceId không tồn tại."));

        // Hóa đơn bắt buộc phải ở trạng thái chờ thanh toán (PENDING)
        if (!"PENDING".equalsIgnoreCase(invoice.getStatus())) {
            throw new IllegalArgumentException("Hóa đơn không ở trạng thái PENDING.");
        }

        // Kiểm tra số tiền gửi lên phải khớp chính xác với finalAmount của hóa đơn
        BigDecimal originalTotal = invoice.getOriginalTotal() != null ? invoice.getOriginalTotal() : BigDecimal.ZERO;
        BigDecimal discountAmount = invoice.getDiscountAmount() != null ? invoice.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal finalAmount = originalTotal.subtract(discountAmount);

        if (request.getAmount().compareTo(finalAmount) != 0) {
            throw new IllegalArgumentException("Số tiền thanh toán không khớp với finalAmount của hóa đơn (" + finalAmount + ").");
        }

        String method = request.getPaymentMethod().trim().toUpperCase();
        LocalDateTime now = LocalDateTime.now();

        if ("CASH".equalsIgnoreCase(method)) {
            // Trường hợp thanh toán Tiền mặt (CASH)
            String transactionId = UUID.randomUUID().toString();
            Payment payment = new Payment(
                    transactionId,
                    invoice.getInvoiceId(),
                    request.getAmount(),
                    "CASH",
                    "PAID",
                    now
            );

            invoice.setStatus("PAID");
            paymentDao.saveCashPaymentTransaction(payment, invoice);

            return new PaymentCashResponseData(
                    transactionId,
                    invoice.getInvoiceId(),
                    "CASH",
                    "PAID",
                    request.getAmount(),
                    formatIso(now)
            );

        } else if ("QR".equalsIgnoreCase(method) || "BANK_TRANSFER".equalsIgnoreCase(method)) {
            // Trường hợp thanh toán VNPay Sandbox (QR hoặc chuyển khoản ngân hàng)
            String transactionId = UUID.randomUUID().toString();
            Payment payment = new Payment(
                    transactionId,
                    invoice.getInvoiceId(),
                    request.getAmount(),
                    method,
                    "UNPAID",
                    now
            );

            paymentDao.savePayment(payment);

            // Sinh URL thanh toán có kèm mã băm chữ ký HMAC-SHA512
            String paymentUrl = VnPayUtil.createPaymentUrl(transactionId, invoice.getInvoiceId(), request.getAmount(), clientIp);
            return new PaymentVnpayResponseData(method, paymentUrl);

        } else {
            throw new IllegalArgumentException("Phương thức thanh toán không hợp lệ (chỉ hỗ trợ CASH, QR, BANK_TRANSFER).");
        }
    }

    /**
     * Tra cứu lịch sử thanh toán và thống kê doanh thu (summary).
     * Phân quyền: Thu ngân CASHIER chỉ xem ca của mình, Chủ cửa hàng STORE_OWNER xem toàn bộ.
     */
    public PaymentHistoryResponseData getPaymentHistory(Integer pageParam,
                                                        Integer limitParam,
                                                        String paymentMethod,
                                                        String status,
                                                        String fromDateStr,
                                                        String toDateStr,
                                                        String currentEmployeeId,
                                                        String currentPosition) {
        String cashierEmployeeId = null;
        if ("CASHIER".equalsIgnoreCase(currentPosition)) {
            cashierEmployeeId = currentEmployeeId;
        } else if (!"STORE_OWNER".equalsIgnoreCase(currentPosition)) {
            throw new SecurityException("Người dùng không có quyền truy cập (ví dụ INVENTORY_MANAGER).");
        }

        int page = (pageParam != null && pageParam > 0) ? pageParam : 1;
        int limit = (limitParam != null && limitParam > 0) ? limitParam : 10;

        LocalDateTime fromDateTime = parseFromDate(fromDateStr);
        LocalDateTime toDateTime = parseToDate(toDateStr);

        PaymentSummaryDto summary = paymentDao.getSummary(fromDateTime, toDateTime, cashierEmployeeId);
        List<PaymentTransactionDto> transactions = paymentDao.findTransactions(
                page, limit, paymentMethod, status, fromDateTime, toDateTime, cashierEmployeeId
        );

        return new PaymentHistoryResponseData(summary, transactions);
    }

    /**
     * Xử lý webhook ngầm (IPN - Server to Server) từ VNPay gửi sang để cập nhật kết quả giao dịch.
     */
    public VnPayIpnResponse processVnPayIpn(Map<String, String[]> parameterMap) {
        // 1. Kiểm tra tính hợp lệ của chữ ký số
        boolean isValidSignature = VnPayUtil.validateSignature(parameterMap);
        if (!isValidSignature) {
            return new VnPayIpnResponse("97", "Invalid Checksum");
        }

        // 2. Kiểm tra mã giao dịch (vnp_TxnRef)
        String[] txnRefArr = parameterMap.get("vnp_TxnRef");
        if (txnRefArr == null || txnRefArr.length == 0 || txnRefArr[0].trim().isEmpty()) {
            return new VnPayIpnResponse("01", "Order not found");
        }
        String transactionId = txnRefArr[0].trim();

        Optional<Payment> paymentOpt = paymentDao.findPaymentByTransactionId(transactionId);
        if (paymentOpt.isEmpty()) {
            return new VnPayIpnResponse("01", "Order not found");
        }

        Payment payment = paymentOpt.get();

        // 3. Kiểm tra số tiền thanh toán (vnp_Amount)
        String[] amountArr = parameterMap.get("vnp_Amount");
        long receivedAmount = 0;
        try {
            if (amountArr != null && amountArr.length > 0) {
                receivedAmount = Long.parseLong(amountArr[0]);
            }
        } catch (NumberFormatException e) {
            return new VnPayIpnResponse("04", "Invalid Amount");
        }

        long expectedAmount = payment.getAmount().multiply(BigDecimal.valueOf(100)).longValue();
        if (receivedAmount != expectedAmount) {
            return new VnPayIpnResponse("04", "Invalid Amount");
        }

        // 4. Kiểm tra đơn hàng đã được chốt thanh toán trước đó chưa
        if ("PAID".equalsIgnoreCase(payment.getStatus())) {
            return new VnPayIpnResponse("02", "Order already confirmed");
        }

        // 5. Cập nhật trạng thái theo mã phản hồi VNPay ("00" là thành công)
        String[] respCodeArr = parameterMap.get("vnp_ResponseCode");
        String respCode = (respCodeArr != null && respCodeArr.length > 0) ? respCodeArr[0] : "";

        if ("00".equals(respCode)) {
            paymentDao.updatePaymentAndInvoiceStatus(transactionId, "PAID", "PAID");
        } else {
            paymentDao.updatePaymentAndInvoiceStatus(transactionId, "FAILED", "PENDING");
        }

        return new VnPayIpnResponse("00", "Confirm Success");
    }

    /**
     * Xử lý khi trình duyệt người dùng được redirect về trang Return URL sau khi hoàn tất thanh toán trên VNPay.
     */
    public PaymentCashResponseData processVnPayReturn(Map<String, String[]> parameterMap) {
        boolean isValidSignature = VnPayUtil.validateSignature(parameterMap);
        if (!isValidSignature) {
            throw new SecurityException("Chữ ký VNPay không hợp lệ (Checksum failed)");
        }

        String[] txnRefArr = parameterMap.get("vnp_TxnRef");
        if (txnRefArr == null || txnRefArr.length == 0 || txnRefArr[0].trim().isEmpty()) {
            throw new NotFoundException("Không tìm thấy mã giao dịch vnp_TxnRef.");
        }
        String transactionId = txnRefArr[0].trim();

        Payment payment = paymentDao.findPaymentByTransactionId(transactionId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy giao dịch: " + transactionId));

        String[] respCodeArr = parameterMap.get("vnp_ResponseCode");
        String respCode = (respCodeArr != null && respCodeArr.length > 0) ? respCodeArr[0] : "";

        if ("00".equals(respCode)) {
            if (!"PAID".equalsIgnoreCase(payment.getStatus())) {
                paymentDao.updatePaymentAndInvoiceStatus(transactionId, "PAID", "PAID");
                payment.setStatus("PAID");
            }
        } else {
            if (!"FAILED".equalsIgnoreCase(payment.getStatus())) {
                paymentDao.updatePaymentAndInvoiceStatus(transactionId, "FAILED", "PENDING");
                payment.setStatus("FAILED");
            }
        }

        LocalDateTime time = payment.getPaymentTime() != null ? payment.getPaymentTime() : LocalDateTime.now();
        return new PaymentCashResponseData(
                payment.getTransactionId(),
                payment.getInvoiceId(),
                payment.getPaymentMethod(),
                payment.getStatus(),
                payment.getAmount(),
                formatIso(time)
        );
    }

    private LocalDateTime parseFromDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            LocalDate date = LocalDate.parse(dateStr.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
            return date.atStartOfDay();
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Định dạng ngày bắt đầu không hợp lệ (YYYY-MM-DD): " + dateStr);
        }
    }

    private LocalDateTime parseToDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            LocalDate date = LocalDate.parse(dateStr.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
            return date.atTime(LocalTime.MAX);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Định dạng ngày kết thúc không hợp lệ (YYYY-MM-DD): " + dateStr);
        }
    }

    private String formatIso(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toString();
    }
}
