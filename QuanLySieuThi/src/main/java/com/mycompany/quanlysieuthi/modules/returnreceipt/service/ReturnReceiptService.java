package com.mycompany.quanlysieuthi.modules.returnreceipt.service;

import com.mycompany.quanlysieuthi.modules.customer.Customer;
import com.mycompany.quanlysieuthi.modules.customer.Invoice;
import com.mycompany.quanlysieuthi.modules.returnreceipt.dao.ReturnReceiptDao;
import com.mycompany.quanlysieuthi.modules.returnreceipt.dto.request.ReturnReceiptItemRequest;
import com.mycompany.quanlysieuthi.modules.returnreceipt.dto.request.ReturnReceiptRequest;
import com.mycompany.quanlysieuthi.modules.returnreceipt.dto.response.ReturnReceiptCustomerDto;
import com.mycompany.quanlysieuthi.modules.returnreceipt.dto.response.ReturnReceiptDetailResponse;
import com.mycompany.quanlysieuthi.modules.returnreceipt.dto.response.ReturnReceiptEmployeeDto;
import com.mycompany.quanlysieuthi.modules.returnreceipt.dto.response.ReturnReceiptListItemDto;
import com.mycompany.quanlysieuthi.modules.returnreceipt.dto.response.ReturnReceiptResponse;
import com.mycompany.quanlysieuthi.modules.returnreceipt.entity.ReturnReceipt;
import com.mycompany.quanlysieuthi.util.NotFoundException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service handling business logic for Return Receipt operations.
 */
public class ReturnReceiptService {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    private final ReturnReceiptDao returnReceiptDao;

    public ReturnReceiptService() {
        this.returnReceiptDao = new ReturnReceiptDao();
    }

    public ReturnReceiptService(ReturnReceiptDao returnReceiptDao) {
        this.returnReceiptDao = returnReceiptDao;
    }

    /**
     * Create a new return receipt for a paid invoice (POST /api/return-receipt).
     */
    public ReturnReceiptResponse createReturnReceipt(ReturnReceiptRequest request,
            String employeeId,
            String employeeFullName) {
        // 1. Validate payload
        if (request == null) {
            throw new IllegalArgumentException("Request body không được để trống");
        }
        if (request.getInvoiceId() == null || request.getInvoiceId().trim().isEmpty()) {
            throw new IllegalArgumentException("Thiếu mã hóa đơn (invoiceId)");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Danh sách items không được để trống");
        }

        String invoiceId = request.getInvoiceId().trim();

        // 2. Fetch invoice and verify PAID status
        Invoice invoice = returnReceiptDao.findInvoiceById(invoiceId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy hóa đơn với ID: " + invoiceId));

        if (!"PAID".equalsIgnoreCase(invoice.getStatus())) {
            throw new IllegalArgumentException("Hóa đơn chưa thanh toán hoặc đã bị hủy không thể tạo phiếu trả hàng.");
        }

        // 3. Fetch original purchased items and previous return records for this
        // invoice
        List<Object[]> invoiceItemRows = returnReceiptDao.findInvoiceItems(invoiceId);
        Map<String, Integer> purchasedQuantityMap = new HashMap<>();
        Map<String, BigDecimal> purchasedUnitPriceMap = new HashMap<>();

        for (Object[] row : invoiceItemRows) {
            String prodId = row[0] != null ? row[0].toString() : null;
            int qty = (row[1] instanceof Number) ? ((Number) row[1]).intValue() : 0;
            BigDecimal price = BigDecimal.ZERO;
            if (row[2] instanceof BigDecimal) {
                price = (BigDecimal) row[2];
            } else if (row[2] instanceof Number) {
                price = BigDecimal.valueOf(((Number) row[2]).doubleValue());
            }

            if (prodId != null) {
                purchasedQuantityMap.put(prodId, qty);
                purchasedUnitPriceMap.put(prodId, price);
            }
        }

        // 3. Verify invoice not already returned
        if (returnReceiptDao.existsReturnReceiptByInvoiceId(invoiceId)) {
            throw new IllegalArgumentException("Hóa đơn (" + invoiceId + ") đã được tạo phiếu trả hàng trước đó");
        }

        String returnReceiptId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();

        BigDecimal refundAmount = BigDecimal.ZERO;
        Map<String, Integer> productRestockMap = new HashMap<>();

        // 4. Validate each returned item
        for (ReturnReceiptItemRequest item : request.getItems()) {
            if (item.getProductId() == null || item.getProductId().trim().isEmpty()) {
                throw new IllegalArgumentException("Thiếu mã sản phẩm");
            }
            if (item.getReturnQuantity() == null || item.getReturnQuantity() <= 0) {
                throw new IllegalArgumentException("Số lượng trả phải lớn hơn 0");
            }

            String productId = item.getProductId().trim();
            int returnQty = item.getReturnQuantity();

            // 4a. Check if product exists in system (404 Not Found if absent)
            returnReceiptDao.findProductById(productId)
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy sản phẩm với ID: " + productId));

            Integer purchasedQty = purchasedQuantityMap.get(productId);
            if (purchasedQty == null) {
                throw new IllegalArgumentException(
                        "Sản phẩm ID " + productId + " không nằm trong hóa đơn gốc (" + invoiceId + ")");
            }

            if (returnQty > purchasedQty) {
                throw new IllegalArgumentException("Số lượng trả của sản phẩm vượt quá số lượng đã mua");
            }

            BigDecimal unitPrice = purchasedUnitPriceMap.getOrDefault(productId, BigDecimal.ZERO);
            BigDecimal lineRefund = unitPrice.multiply(BigDecimal.valueOf(returnQty));
            refundAmount = refundAmount.add(lineRefund);

            productRestockMap.put(productId, productRestockMap.getOrDefault(productId, 0) + returnQty);
        }

        // 5. Handle customer loyalty points deduction
        String customerId = invoice.getCustomerId();
        int pointsDeducted = 0;
        ReturnReceiptCustomerDto customerDto = null;

        if (customerId != null && !customerId.trim().isEmpty()) {
            Customer customer = returnReceiptDao.findCustomerById(customerId.trim()).orElse(null);
            if (customer != null) {
                pointsDeducted = (int) (refundAmount.longValue() / 10000) * 100;
                int currentPoints = customer.getLoyaltyPoints() != null ? customer.getLoyaltyPoints() : 0;
                int remainingPoints = Math.max(0, currentPoints - pointsDeducted);

                customerDto = new ReturnReceiptCustomerDto(
                        customer.getCustomerId(),
                        customer.getFullName(),
                        remainingPoints);
            }
        }

        // 6. Execute atomic transaction in Database
        ReturnReceipt returnReceipt = new ReturnReceipt(
                returnReceiptId,
                invoiceId,
                employeeId != null ? employeeId : "",
                refundAmount,
                request.getReason() != null ? request.getReason().trim() : null,
                now);

        returnReceiptDao.saveReturnReceiptTransaction(
                returnReceipt,
                productRestockMap,
                customerId,
                pointsDeducted);

        // 7. Build Response DTO
        ReturnReceiptEmployeeDto employeeDto = new ReturnReceiptEmployeeDto(
                employeeId != null ? employeeId : "",
                employeeFullName != null ? employeeFullName : "");

        String formattedCreatedAt = now.atOffset(ZoneOffset.UTC).format(ISO_FORMATTER);

        return new ReturnReceiptResponse(
                returnReceiptId,
                invoiceId,
                formattedCreatedAt,
                refundAmount,
                pointsDeducted,
                employeeDto,
                customerDto);
    }

    /**
     * Get list of return receipts with pagination and filters (GET
     * /api/return-receipt).
     */
    public List<ReturnReceiptListItemDto> getReturnReceipts(Integer pageParam,
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
        if (page > limit) {
            throw new IllegalArgumentException("Tham số page không được lớn hơn limit");
        }

        LocalDateTime fromDateTime = null;
        LocalDateTime toDateTime = null;

        if (fromDateStr != null && !fromDateStr.trim().isEmpty()) {
            try {
                LocalDate fromDate = LocalDate.parse(fromDateStr.trim());
                fromDateTime = fromDate.atStartOfDay();
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Định dạng ngày tháng fromDate không hợp lệ");
            }
        }

        if (toDateStr != null && !toDateStr.trim().isEmpty()) {
            try {
                LocalDate toDate = LocalDate.parse(toDateStr.trim());
                toDateTime = toDate.atTime(LocalTime.MAX);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Định dạng ngày tháng toDate không hợp lệ");
            }
        }

        if (fromDateTime != null && toDateTime != null && fromDateTime.isAfter(toDateTime)) {
            throw new IllegalArgumentException("fromDate không được sau toDate");
        }

        List<Object[]> rawList = returnReceiptDao.findReturnReceipts(
                page,
                limit,
                (invoiceId != null && !invoiceId.trim().isEmpty()) ? invoiceId.trim() : null,
                (employeeId != null && !employeeId.trim().isEmpty()) ? employeeId.trim() : null,
                fromDateTime,
                toDateTime);

        List<ReturnReceiptListItemDto> result = new ArrayList<>();
        for (Object[] row : rawList) {
            String returnReceiptId = (String) row[0];
            String invId = (String) row[1];
            BigDecimal refundAmount = (BigDecimal) row[2];
            String reason = (String) row[3];
            LocalDateTime createdAt = (LocalDateTime) row[4];
            String empId = (String) row[5];
            String empFullName = (String) row[6];

            String formattedDate = createdAt != null
                    ? createdAt.atOffset(ZoneOffset.UTC).format(ISO_FORMATTER)
                    : null;

            ReturnReceiptEmployeeDto empDto = null;
            if (empId != null) {
                empDto = new ReturnReceiptEmployeeDto(empId, empFullName);
            }

            result.add(new ReturnReceiptListItemDto(
                    returnReceiptId,
                    invId,
                    refundAmount,
                    reason,
                    formattedDate,
                    empDto));
        }

        return result;
    }

    /**
     * Get detail of a return receipt by ID (GET /api/return-receipt/:id).
     */
    public ReturnReceiptDetailResponse getReturnReceiptDetail(String returnReceiptId) {
        if (returnReceiptId == null || returnReceiptId.trim().isEmpty() || !isValidUUID(returnReceiptId.trim())) {
            throw new IllegalArgumentException("ID không đúng định dạng UUID.");
        }

        String cleanId = returnReceiptId.trim();

        Object[] headerRow = returnReceiptDao.findReturnReceiptHeaderById(cleanId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy phiếu trả hàng với ID tương ứng."));

        String receiptId = (String) headerRow[0];
        String invoiceId = (String) headerRow[1];
        BigDecimal refundAmount = (BigDecimal) headerRow[2];
        String reason = (String) headerRow[3];
        LocalDateTime createdAt = (LocalDateTime) headerRow[4];
        String empId = (String) headerRow[5];
        String empFullName = (String) headerRow[6];
        String empUsername = (String) headerRow[7];
        String customerId = (String) headerRow[8];
        String customerFullName = (String) headerRow[9];
        String customerPhoneNumber = (String) headerRow[10];

        String formattedDate = createdAt != null
                ? createdAt.atOffset(ZoneOffset.UTC).format(ISO_FORMATTER)
                : null;

        ReturnReceiptEmployeeDto employeeDto = null;
        if (empId != null) {
            employeeDto = new ReturnReceiptEmployeeDto(empId, empFullName, empUsername);
        }

        ReturnReceiptCustomerDto customerDto = null;
        if (customerId != null && !customerId.trim().isEmpty()) {
            customerDto = new ReturnReceiptCustomerDto(customerId, customerFullName, customerPhoneNumber);
        }

        return new ReturnReceiptDetailResponse(
                receiptId,
                invoiceId,
                refundAmount,
                reason,
                formattedDate,
                employeeDto,
                customerDto);
    }

    private boolean isValidUUID(String uuid) {
        if (uuid == null) {
            return false;
        }
        try {
            UUID.fromString(uuid);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
