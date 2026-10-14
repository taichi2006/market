package com.mycompany.quanlysieuthi.modules.returnreceipt;

import com.mycompany.quanlysieuthi.entity.Customer;
import com.mycompany.quanlysieuthi.entity.Invoice;
import com.mycompany.quanlysieuthi.entity.ReturnReceipt;
import com.mycompany.quanlysieuthi.common.exception.AppException.*;
import com.mycompany.quanlysieuthi.util.IdGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Service handling business logic for Return Receipt.
 */
public class ReturnReceiptService {

    private final ReturnReceiptRepository returnReceiptRepository;

    public ReturnReceiptService() {
        this.returnReceiptRepository = new ReturnReceiptRepository();
    }

    public ReturnReceiptService(ReturnReceiptRepository returnReceiptRepository) {
        this.returnReceiptRepository = returnReceiptRepository;
    }

    public ReturnReceiptResponse createReturnReceipt(ReturnReceiptRequest.Create request,
                                                      String currentEmployeeId,
                                                      String currentFullName) {
        if (request == null) {
            throw new IllegalArgumentException("Dữ liệu phiếu trả hàng không được để trống");
        }
        if (request.getInvoiceId() == null || request.getInvoiceId().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã hóa đơn gốc (invoiceId) không được để trống");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Danh sách mặt hàng trả lại (items) không được để trống");
        }

        String invoiceId = request.getInvoiceId().trim();

        Invoice invoice = returnReceiptRepository.findInvoiceById(invoiceId)
                .orElseThrow(() -> new NotFoundException("Hóa đơn không tồn tại trong hệ thống: " + invoiceId));

        if (!"PAID".equalsIgnoreCase(invoice.getStatus())) {
            throw new IllegalArgumentException("Chỉ được phép trả hàng cho hóa đơn đã hoàn tất thanh toán (PAID). Trạng thái hiện tại: " + invoice.getStatus());
        }

        if (returnReceiptRepository.existsReturnReceiptByInvoiceId(invoiceId)) {
            throw new ConflictException("Hóa đơn này đã được lập phiếu trả hàng trước đó");
        }

        List<Object[]> rawInvoiceItems = returnReceiptRepository.findInvoiceItems(invoiceId);
        if (rawInvoiceItems.isEmpty()) {
            throw new NotFoundException("Hóa đơn này không có danh sách mặt hàng hợp lệ");
        }

        Map<String, Integer> invoiceItemQuantities = new HashMap<>();
        Map<String, BigDecimal> invoiceItemUnitPrices = new HashMap<>();

        for (Object[] row : rawInvoiceItems) {
            String prodId = (String) row[0];
            Integer qty = (Integer) row[1];
            BigDecimal price = (BigDecimal) row[2];

            invoiceItemQuantities.put(prodId, qty != null ? qty : 0);
            invoiceItemUnitPrices.put(prodId, price != null ? price : BigDecimal.ZERO);
        }

        BigDecimal refundAmount = BigDecimal.ZERO;
        Map<String, Integer> productRestockMap = new HashMap<>();

        for (ReturnReceiptRequest.Item item : request.getItems()) {
            if (item.getProductId() == null || item.getProductId().trim().isEmpty()) {
                throw new IllegalArgumentException("Mã sản phẩm không được để trống");
            }
            if (item.getReturnedQuantity() == null || item.getReturnedQuantity() <= 0) {
                throw new IllegalArgumentException("Số lượng trả lại phải là số nguyên > 0");
            }

            String productId = item.getProductId().trim();
            int returnQty = item.getReturnedQuantity();

            if (!invoiceItemQuantities.containsKey(productId)) {
                throw new IllegalArgumentException("Mặt hàng " + productId + " không tồn tại trong hóa đơn " + invoiceId);
            }

            int purchasedQty = invoiceItemQuantities.get(productId);
            if (returnQty > purchasedQty) {
                throw new IllegalArgumentException("Số lượng trả lại (" + returnQty + ") vượt quá số lượng đã mua (" + purchasedQty + ") của mặt hàng " + productId);
            }

            BigDecimal unitPrice = invoiceItemUnitPrices.get(productId);
            BigDecimal itemRefund = unitPrice.multiply(BigDecimal.valueOf(returnQty));
            refundAmount = refundAmount.add(itemRefund);

            productRestockMap.put(productId, returnQty);
        }

        String returnReceiptId = IdGenerator.nextReturnReceiptId();
        LocalDateTime now = LocalDateTime.now();
        String reason = request.getReason() != null ? request.getReason().trim() : null;

        int pointsDeducted = 0;
        String customerId = invoice.getCustomerId();
        if (customerId != null && !customerId.trim().isEmpty()) {
            Optional<Customer> custOpt = returnReceiptRepository.findCustomerById(customerId.trim());
            if (custOpt.isPresent()) {
                Customer customer = custOpt.get();
                int currentPoints = customer.getLoyaltyPoints() != null ? customer.getLoyaltyPoints() : 0;
                pointsDeducted = (int) Math.floor(refundAmount.doubleValue() / 10000.0) * 100;
                if (pointsDeducted > currentPoints) {
                    pointsDeducted = currentPoints;
                }
            }
        }

        ReturnReceipt receipt = new ReturnReceipt(
                returnReceiptId,
                currentEmployeeId != null ? currentEmployeeId : "",
                invoiceId,
                refundAmount,
                reason,
                now
        );

        returnReceiptRepository.saveReturnReceiptTransaction(receipt, productRestockMap, customerId, pointsDeducted);

        ReturnReceiptResponse.EmployeeDto createdBy = new ReturnReceiptResponse.EmployeeDto(
                currentEmployeeId != null ? currentEmployeeId : "",
                currentFullName != null ? currentFullName : ""
        );

        return new ReturnReceiptResponse(
                returnReceiptId,
                invoiceId,
                reason,
                refundAmount,
                formatIso(now),
                createdBy
        );
    }

    public List<ReturnReceiptResponse.ListItemDto> getReturnReceipts(Integer pageParam,
                                                                     Integer limitParam,
                                                                     String invoiceId,
                                                                     String employeeId,
                                                                     String fromDateStr,
                                                                     String toDateStr) {
        int page = (pageParam != null) ? pageParam : 1;
        int limit = (limitParam != null) ? limitParam : 10;

        if (page <= 0) {
            throw new IllegalArgumentException("Tham số page phải lớn hơn 0");
        }
        if (limit <= 0) {
            throw new IllegalArgumentException("Tham số limit phải lớn hơn 0");
        }

        LocalDateTime fromDateTime = null;
        LocalDateTime toDateTime = null;

        if (fromDateStr != null && !fromDateStr.trim().isEmpty()) {
            try {
                LocalDate fromDate = LocalDate.parse(fromDateStr.trim());
                fromDateTime = fromDate.atStartOfDay();
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Định dạng ngày tháng fromDate không hợp lệ (yêu cầu YYYY-MM-DD)");
            }
        }

        if (toDateStr != null && !toDateStr.trim().isEmpty()) {
            try {
                LocalDate toDate = LocalDate.parse(toDateStr.trim());
                toDateTime = toDate.atTime(LocalTime.MAX);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Định dạng ngày tháng toDate không hợp lệ (yêu cầu YYYY-MM-DD)");
            }
        }

        if (fromDateTime != null && toDateTime != null && fromDateTime.isAfter(toDateTime)) {
            throw new IllegalArgumentException("fromDate không được sau toDate");
        }

        List<Object[]> rows = returnReceiptRepository.findReturnReceipts(page, limit, invoiceId, employeeId, fromDateTime, toDateTime);
        List<ReturnReceiptResponse.ListItemDto> result = new ArrayList<>();

        for (Object[] row : rows) {
            String receiptId = (String) row[0];
            String invId = (String) row[1];
            BigDecimal refundAmt = (BigDecimal) row[2];
            String reason = (String) row[3];
            LocalDateTime createdAt = (LocalDateTime) row[4];
            String empId = (String) row[5];
            String empFullName = (String) row[6];

            ReturnReceiptResponse.EmployeeDto empDto = null;
            if (empId != null) {
                empDto = new ReturnReceiptResponse.EmployeeDto(empId, empFullName);
            }

            result.add(new ReturnReceiptResponse.ListItemDto(
                    receiptId,
                    invId,
                    refundAmt,
                    reason,
                    formatIso(createdAt),
                    empDto
            ));
        }

        return result;
    }

    public ReturnReceiptResponse.DetailResponse getReturnReceiptDetail(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã phiếu trả hàng không được để trống");
        }

        Object[] row = returnReceiptRepository.findReturnReceiptHeaderById(id.trim())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy phiếu trả hàng với ID tương ứng: " + id.trim()));

        String receiptId = (String) row[0];
        String invId = (String) row[1];
        BigDecimal refundAmt = (BigDecimal) row[2];
        String reason = (String) row[3];
        LocalDateTime createdAt = (LocalDateTime) row[4];

        String empId = (String) row[5];
        String empFullName = (String) row[6];
        String empUsername = (String) row[7];

        ReturnReceiptResponse.EmployeeDto empDto = null;
        if (empId != null) {
            empDto = new ReturnReceiptResponse.EmployeeDto(empId, empFullName, empUsername);
        }

        String custId = (String) row[8];
        String custFullName = (String) row[9];
        String custPhone = (String) row[10];

        ReturnReceiptResponse.CustomerDto custDto = null;
        if (custId != null) {
            custDto = new ReturnReceiptResponse.CustomerDto(custId, custFullName, custPhone);
        }

        return new ReturnReceiptResponse.DetailResponse(
                receiptId,
                invId,
                refundAmt,
                reason,
                formatIso(createdAt),
                empDto,
                custDto
        );
    }

    private String formatIso(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        return dateTime.atOffset(ZoneOffset.UTC).format(formatter);
    }
}
