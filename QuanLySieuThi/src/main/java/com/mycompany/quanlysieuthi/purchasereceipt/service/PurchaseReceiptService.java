package com.mycompany.quanlysieuthi.purchasereceipt.service;

import com.mycompany.quanlysieuthi.purchasereceipt.dao.PurchaseReceiptDao;
import com.mycompany.quanlysieuthi.purchasereceipt.dto.request.PurchaseReceiptItemRequest;
import com.mycompany.quanlysieuthi.purchasereceipt.dto.request.PurchaseReceiptRequest;
import com.mycompany.quanlysieuthi.purchasereceipt.dto.response.CreatedEmployeeDto;
import com.mycompany.quanlysieuthi.purchasereceipt.dto.response.CreatedProductDto;
import com.mycompany.quanlysieuthi.purchasereceipt.dto.response.PurchaseReceiptDetailResponse;
import com.mycompany.quanlysieuthi.purchasereceipt.dto.response.PurchaseReceiptListItemDto;
import com.mycompany.quanlysieuthi.purchasereceipt.dto.response.PurchaseReceiptResponse;
import com.mycompany.quanlysieuthi.purchasereceipt.entity.Product;
import com.mycompany.quanlysieuthi.purchasereceipt.entity.PurchaseReceipt;
import com.mycompany.quanlysieuthi.purchasereceipt.entity.PurchaseReceiptDetail;
import com.mycompany.quanlysieuthi.purchasereceipt.entity.Voucher;
import com.mycompany.quanlysieuthi.util.NotFoundException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service handling business logic for Purchase Receipt creation.
 */
public class PurchaseReceiptService {

    private final PurchaseReceiptDao purchaseReceiptDao;

    public PurchaseReceiptService() {
        this.purchaseReceiptDao = new PurchaseReceiptDao();
    }

    public PurchaseReceiptService(PurchaseReceiptDao purchaseReceiptDao) {
        this.purchaseReceiptDao = purchaseReceiptDao;
    }

    public PurchaseReceiptResponse createPurchaseReceipt(PurchaseReceiptRequest request,
            String employeeId,
            String employeeFullName) {
        // 1. Validate payload
        if (request == null) {
            throw new IllegalArgumentException("Request body không được để trống");
        }
        if (request.getSupplierName() == null || request.getSupplierName().trim().isEmpty()) {
            throw new IllegalArgumentException("Thiếu thông tin nhà cung cấp (supplierName)");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Danh sách items không được để trống");
        }

        LocalDateTime now = LocalDateTime.now();
        BigDecimal totalAmount = BigDecimal.ZERO;

        List<Product> products = new ArrayList<>();
        List<PurchaseReceiptDetail> details = new ArrayList<>();
        List<CreatedProductDto> createdProductDtos = new ArrayList<>();

        String voucherId = UUID.randomUUID().toString();

        // 2. Validate and process each item
        for (PurchaseReceiptItemRequest item : request.getItems()) {
            if (item.getProductName() == null || item.getProductName().trim().isEmpty()) {
                throw new IllegalArgumentException("Thiếu tên sản phẩm (productName)");
            }
            if (item.getCategoryId() == null || item.getCategoryId().trim().isEmpty()) {
                throw new IllegalArgumentException("Thiếu mã danh mục (categoryId)");
            }
            if (item.getPurchasedQuantity() == null || item.getPurchasedQuantity() <= 0) {
                throw new IllegalArgumentException("purchasedQuantity phải lớn hơn 0");
            }
            if (item.getPurchasePrice() == null || item.getPurchasePrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("purchasePrice phải lớn hơn 0");
            }
            if (item.getExpirationDate() == null || item.getExpirationDate().trim().isEmpty()) {
                throw new IllegalArgumentException("Thiếu ngày hết hạn (expirationDate)");
            }

            LocalDateTime expDate = parseExpirationDate(item.getExpirationDate().trim());
            if (expDate.isBefore(now)) {
                throw new IllegalArgumentException("Ngày hết hạn expirationDate không được ở quá khứ");
            }

            // Verify category exists in DB (404 if not found)
            if (!purchaseReceiptDao.existsCategory(item.getCategoryId().trim())) {
                throw new NotFoundException("categoryId không tồn tại trong hệ thống: " + item.getCategoryId().trim());
            }

            BigDecimal itemTotal = item.getPurchasePrice().multiply(BigDecimal.valueOf(item.getPurchasedQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            String productId = UUID.randomUUID().toString();
            String purchaseDetailId = UUID.randomUUID().toString();

            Product product = new Product(
                    productId,
                    item.getCategoryId().trim(),
                    item.getProductName().trim(),
                    item.getPurchasedQuantity(),
                    expDate,
                    "ON_SALE");
            products.add(product);

            PurchaseReceiptDetail detail = new PurchaseReceiptDetail(
                    purchaseDetailId,
                    voucherId,
                    productId,
                    item.getPurchasedQuantity(),
                    expDate,
                    item.getPurchasePrice());
            details.add(detail);

            createdProductDtos.add(new CreatedProductDto(
                    productId,
                    product.getProductName(),
                    product.getStockQuantity(),
                    item.getPurchasePrice(),
                    formatIso(expDate)));
        }

        // 3. Create Voucher and PurchaseReceipt entities
        Voucher voucher = new Voucher(voucherId, employeeId, totalAmount, now);
        PurchaseReceipt purchaseReceipt = new PurchaseReceipt(voucherId, request.getSupplierName().trim());

        // 4. Execute atomic transaction in Database
        purchaseReceiptDao.savePurchaseReceiptTransaction(voucher, purchaseReceipt, products, details);

        // 5. Construct Response
        CreatedEmployeeDto createdEmployeeDto = new CreatedEmployeeDto(
                employeeId != null ? employeeId : "",
                employeeFullName != null ? employeeFullName : "");

        return new PurchaseReceiptResponse(
                voucherId,
                request.getSupplierName().trim(),
                totalAmount,
                formatIso(now),
                createdEmployeeDto,
                createdProductDtos);
    }

    private LocalDateTime parseExpirationDate(String dateStr) {
        try {
            // Support ISO-8601 with Z (e.g. 2027-10-15T00:00:00.000Z)
            Instant instant = Instant.parse(dateStr);
            return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        } catch (DateTimeParseException e1) {
            try {
                // Support yyyy-MM-dd'T'HH:mm:ss
                return LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (DateTimeParseException e2) {
                try {
                    // Support yyyy-MM-dd
                    LocalDate localDate = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
                    return localDate.atStartOfDay();
                } catch (DateTimeParseException e3) {
                    throw new IllegalArgumentException("Định dạng ngày hết hạn không hợp lệ: " + dateStr);
                }
            }
        }
    }

    private String formatIso(LocalDateTime dateTime) {
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toString();
    }

    public List<PurchaseReceiptListItemDto> getPurchaseReceipts(Integer pageParam,
                                                               Integer limitParam,
                                                               String supplierName,
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

        return purchaseReceiptDao.findPurchaseReceipts(page, limit, supplierName, employeeId, fromDateTime, toDateTime);
    }

    public PurchaseReceiptDetailResponse getPurchaseReceiptDetail(String voucherId) {
        if (voucherId == null || voucherId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID phiếu nhập không được để trống");
        }

        return purchaseReceiptDao.findPurchaseReceiptDetailById(voucherId.trim())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy phiếu nhập với ID tương ứng: " + voucherId.trim()));
    }
}
