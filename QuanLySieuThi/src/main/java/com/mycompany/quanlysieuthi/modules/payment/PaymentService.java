package com.mycompany.quanlysieuthi.modules.payment;

import com.mycompany.quanlysieuthi.entity.Invoice;
import com.mycompany.quanlysieuthi.entity.Payment;
import com.mycompany.quanlysieuthi.common.exception.AppException.*;
import com.mycompany.quanlysieuthi.util.VnPayUtil;
import com.mycompany.quanlysieuthi.util.IdGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Service handling business logic for Payment operations.
 */
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService() {
        this.paymentRepository = new PaymentRepository();
    }

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public Object processPayment(PaymentRequest.Process request, String clientIp) {
        if (request == null) {
            throw new IllegalArgumentException("Dữ liệu thanh toán không được để trống");
        }
        if (request.getInvoiceId() == null || request.getInvoiceId().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã hóa đơn (invoiceId) không được để trống");
        }
        if (request.getPaymentMethod() == null || request.getPaymentMethod().trim().isEmpty()) {
            throw new IllegalArgumentException("Phương thức thanh toán (paymentMethod) không được để trống");
        }

        String invoiceId = request.getInvoiceId().trim();
        String method = request.getPaymentMethod().trim().toUpperCase();

        if (!method.equals("CASH") && !method.equals("QR") && !method.equals("BANK_TRANSFER")) {
            throw new IllegalArgumentException("Phương thức thanh toán không hợp lệ (hỗ trợ CASH, QR, BANK_TRANSFER)");
        }

        Invoice invoice = paymentRepository.findInvoiceById(invoiceId)
                .orElseThrow(() -> new NotFoundException("Hóa đơn không tồn tại: " + invoiceId));

        if ("PAID".equalsIgnoreCase(invoice.getStatus())) {
            throw new ConflictException("Hóa đơn này đã được thanh toán trước đó");
        }
        if ("CANCELLED".equalsIgnoreCase(invoice.getStatus())) {
            throw new IllegalArgumentException("Hóa đơn đã bị hủy, không thể thực hiện thanh toán");
        }

        BigDecimal originalTotal = invoice.getOriginalTotal() != null ? invoice.getOriginalTotal() : BigDecimal.ZERO;
        BigDecimal discountAmount = invoice.getDiscountAmount() != null ? invoice.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal finalAmount = originalTotal.subtract(discountAmount);

        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            finalAmount = BigDecimal.ZERO;
        }

        String transactionId = IdGenerator.nextTransactionId();
        LocalDateTime now = LocalDateTime.now();

        if ("CASH".equals(method)) {
            Payment payment = new Payment(transactionId, invoiceId, finalAmount, "CASH", "PAID", now);
            invoice.setStatus("PAID");
            paymentRepository.saveCashPaymentTransaction(payment, invoice);

            String timeStr = now.atZone(ZoneId.systemDefault()).toInstant().toString();
            return new PaymentResponse.CashResponse(transactionId, invoiceId, finalAmount, "CASH", "PAID", timeStr);
        } else {
            Payment payment = new Payment(transactionId, invoiceId, finalAmount, method, "UNPAID", now);
            paymentRepository.savePayment(payment);

            String paymentUrl = VnPayUtil.createPaymentUrl(transactionId, invoiceId, finalAmount, clientIp);
            return new PaymentResponse.VnpayResponse(transactionId, invoiceId, finalAmount, paymentUrl);
        }
    }

    public PaymentResponse.IpnResponse handleVnPayIpn(Map<String, String[]> parameterMap) {
        if (!VnPayUtil.validateSignature(parameterMap)) {
            return new PaymentResponse.IpnResponse("97", "Invalid Checksum");
        }

        String transactionId = getParam(parameterMap, "vnp_TxnRef");
        String responseCode = getParam(parameterMap, "vnp_ResponseCode");

        if (transactionId == null || transactionId.isEmpty()) {
            return new PaymentResponse.IpnResponse("01", "Order not found");
        }

        Optional<Payment> paymentOpt = paymentRepository.findPaymentByTransactionId(transactionId);
        if (paymentOpt.isEmpty()) {
            return new PaymentResponse.IpnResponse("01", "Order not found");
        }

        Payment payment = paymentOpt.get();

        if ("PAID".equalsIgnoreCase(payment.getStatus())) {
            return new PaymentResponse.IpnResponse("02", "Order already confirmed");
        }

        if ("00".equals(responseCode)) {
            paymentRepository.updatePaymentAndInvoiceStatus(transactionId, "PAID", "PAID");
            return new PaymentResponse.IpnResponse("00", "Confirm Success");
        } else {
            paymentRepository.updatePaymentAndInvoiceStatus(transactionId, "FAILED", "PENDING");
            return new PaymentResponse.IpnResponse("00", "Payment Failed Recorded");
        }
    }

    public boolean handleVnPayReturn(Map<String, String[]> parameterMap) {
        boolean valid = VnPayUtil.validateSignature(parameterMap);
        if (!valid) {
            return false;
        }

        String transactionId = getParam(parameterMap, "vnp_TxnRef");
        String responseCode = getParam(parameterMap, "vnp_ResponseCode");

        if (transactionId != null && !transactionId.isEmpty()) {
            if ("00".equals(responseCode)) {
                paymentRepository.updatePaymentAndInvoiceStatus(transactionId, "PAID", "PAID");
                return true;
            } else {
                paymentRepository.updatePaymentAndInvoiceStatus(transactionId, "FAILED", "PENDING");
                return false;
            }
        }
        return false;
    }

    public List<PaymentResponse.TransactionDto> getTransactions(int page, int limit,
                                                                String paymentMethod,
                                                                String status,
                                                                String fromDateStr,
                                                                String toDateStr,
                                                                String currentEmployeeId,
                                                                String currentRole) {
        if (page <= 0) {
            throw new IllegalArgumentException("Tham số page phải lớn hơn 0");
        }
        if (limit <= 0) {
            throw new IllegalArgumentException("Tham số limit phải lớn hơn 0");
        }

        LocalDateTime fromDateTime = parseFromDate(fromDateStr);
        LocalDateTime toDateTime = parseToDate(toDateStr);

        if (fromDateTime != null && toDateTime != null && fromDateTime.isAfter(toDateTime)) {
            throw new IllegalArgumentException("fromDate không được sau toDate");
        }

        String cashierIdFilter = null;
        if ("CASHIER".equalsIgnoreCase(currentRole)) {
            cashierIdFilter = currentEmployeeId;
        }

        return paymentRepository.findTransactions(page, limit, paymentMethod, status, fromDateTime, toDateTime, cashierIdFilter);
    }

    public PaymentResponse.SummaryDto getSummary(String fromDateStr,
                                                 String toDateStr,
                                                 String currentEmployeeId,
                                                 String currentRole) {
        LocalDateTime fromDateTime = parseFromDate(fromDateStr);
        LocalDateTime toDateTime = parseToDate(toDateStr);

        if (fromDateTime != null && toDateTime != null && fromDateTime.isAfter(toDateTime)) {
            throw new IllegalArgumentException("fromDate không được sau toDate");
        }

        String cashierIdFilter = null;
        if ("CASHIER".equalsIgnoreCase(currentRole)) {
            cashierIdFilter = currentEmployeeId;
        }

        return paymentRepository.getSummary(fromDateTime, toDateTime, cashierIdFilter);
    }

    public PaymentResponse.HistoryResponse getInvoicePaymentHistory(String invoiceId) {
        if (invoiceId == null || invoiceId.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã hóa đơn không được để trống");
        }

        Invoice invoice = paymentRepository.findInvoiceById(invoiceId.trim())
                .orElseThrow(() -> new NotFoundException("Hóa đơn không tồn tại: " + invoiceId));

        BigDecimal originalTotal = invoice.getOriginalTotal() != null ? invoice.getOriginalTotal() : BigDecimal.ZERO;
        BigDecimal discountAmount = invoice.getDiscountAmount() != null ? invoice.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal finalAmount = originalTotal.subtract(discountAmount);
        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            finalAmount = BigDecimal.ZERO;
        }

        List<PaymentResponse.TransactionDto> transactions = paymentRepository.findTransactions(
                1, 100, null, null, null, null, null
        );

        List<PaymentResponse.TransactionDto> invoiceTxs = new ArrayList<>();
        BigDecimal totalPaid = BigDecimal.ZERO;

        for (PaymentResponse.TransactionDto tx : transactions) {
            if (invoiceId.trim().equalsIgnoreCase(tx.getInvoiceId())) {
                invoiceTxs.add(tx);
                if ("PAID".equalsIgnoreCase(tx.getStatus())) {
                    totalPaid = totalPaid.add(tx.getAmount());
                }
            }
        }

        BigDecimal remaining = finalAmount.subtract(totalPaid);
        if (remaining.compareTo(BigDecimal.ZERO) < 0) {
            remaining = BigDecimal.ZERO;
        }

        PaymentResponse.SummaryDto summary = new PaymentResponse.SummaryDto(
                originalTotal, discountAmount, finalAmount, totalPaid, remaining
        );

        return new PaymentResponse.HistoryResponse(invoiceId, invoice.getStatus(), summary, invoiceTxs);
    }

    private static String getParam(Map<String, String[]> map, String key) {
        if (map == null) return null;
        String[] vals = map.get(key);
        return (vals != null && vals.length > 0) ? vals[0] : null;
    }

    private static LocalDateTime parseFromDate(String str) {
        if (str == null || str.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(str.trim()).atStartOfDay();
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Định dạng fromDate không hợp lệ (yêu cầu YYYY-MM-DD)");
        }
    }

    private static LocalDateTime parseToDate(String str) {
        if (str == null || str.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(str.trim()).atTime(LocalTime.MAX);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Định dạng toDate không hợp lệ (yêu cầu YYYY-MM-DD)");
        }
    }
}
