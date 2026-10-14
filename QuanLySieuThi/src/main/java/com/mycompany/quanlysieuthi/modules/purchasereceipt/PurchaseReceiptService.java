package com.mycompany.quanlysieuthi.modules.purchasereceipt;

import com.mycompany.quanlysieuthi.entity.Product;
import com.mycompany.quanlysieuthi.entity.PurchaseReceipt;
import com.mycompany.quanlysieuthi.entity.PurchaseReceiptDetail;
import com.mycompany.quanlysieuthi.entity.Voucher;
import com.mycompany.quanlysieuthi.common.exception.AppException.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.mycompany.quanlysieuthi.util.IdGenerator;

/**
 * Service handling business logic for Purchase Receipt.
 */
public class PurchaseReceiptService {

    private final PurchaseReceiptRepository purchaseReceiptRepository;

    public PurchaseReceiptService() {
        this.purchaseReceiptRepository = new PurchaseReceiptRepository();
    }

    public PurchaseReceiptService(PurchaseReceiptRepository purchaseReceiptRepository) {
        this.purchaseReceiptRepository = purchaseReceiptRepository;
    }

    public PurchaseReceiptResponse createPurchaseReceipt(PurchaseReceiptRequest request,
                                                          String currentEmployeeId,
                                                          String currentFullName) {
        if (request == null) {
            throw new IllegalArgumentException("Dữ liệu phiếu nhập hàng không được để trống");
        }
        if (request.getSupplierName() == null || request.getSupplierName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên nhà cung cấp (supplierName) không được để trống");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Danh sách mặt hàng nhập kho (items) không được để trống");
        }

        String voucherId = IdGenerator.nextVoucherId();
        int newProductCount = 0;
        for (PurchaseReceiptRequest.Item it : request.getItems()) {
            if (it.getProductId() == null || it.getProductId().trim().isEmpty()) {
                newProductCount++;
            }
        }
        List<String> generatedProductIds = IdGenerator.nextProductIds(newProductCount);
        int newProdIdx = 0;
        List<String> detailIds = IdGenerator.nextPurchaseDetailIds(request.getItems().size());
        int detailIdx = 0;
        LocalDateTime now = LocalDateTime.now();
        BigDecimal totalAmount = BigDecimal.ZERO;

        List<Product> productsToPersist = new ArrayList<>();
        List<PurchaseReceiptDetail> detailsToPersist = new ArrayList<>();
        List<PurchaseReceiptResponse.CreatedProductDto> createdProductDtos = new ArrayList<>();

        for (PurchaseReceiptRequest.Item item : request.getItems()) {
            if (item.getProductName() == null || item.getProductName().trim().isEmpty()) {
                throw new IllegalArgumentException("Tên sản phẩm không được để trống");
            }
            String catId = item.getCategoryId();
            if (catId == null || catId.trim().isEmpty()) {
                if (item.getProductId() != null && !item.getProductId().trim().isEmpty()) {
                    Optional<Product> existingProd = purchaseReceiptRepository.findProductById(item.getProductId().trim());
                    if (existingProd.isPresent() && existingProd.get().getCategoryId() != null) {
                        catId = existingProd.get().getCategoryId();
                    }
                }
                if (catId == null || catId.trim().isEmpty()) {
                    catId = purchaseReceiptRepository.findFirstCategoryId();
                }
            }
            if (catId == null || catId.trim().isEmpty()) {
                throw new IllegalArgumentException("Mã ngành hàng (categoryId) không được để trống");
            }
            if (!purchaseReceiptRepository.existsCategory(catId.trim())) {
                throw new NotFoundException("Mã ngành hàng không tồn tại: " + catId);
            }
            final String finalCatId = catId.trim();
            if (item.getUnitPrice() == null || item.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Giá bán lẻ (unitPrice) phải >= 0");
            }
            if (item.getPurchasedQuantity() == null || item.getPurchasedQuantity() <= 0) {
                throw new IllegalArgumentException("Số lượng nhập (purchasedQuantity) phải là số nguyên > 0");
            }
            if (item.getPurchasePrice() == null || item.getPurchasePrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Giá nhập (purchasePrice) phải > 0");
            }
            if (item.getExpirationDate() == null || item.getExpirationDate().trim().isEmpty()) {
                throw new IllegalArgumentException("Hạn sử dụng (expirationDate) không được để trống");
            }

            LocalDateTime expDateTime;
            try {
                String expStr = item.getExpirationDate().trim();
                if (expStr.contains("T")) {
                    expDateTime = LocalDateTime.parse(expStr, DateTimeFormatter.ISO_DATE_TIME);
                } else {
                    expDateTime = LocalDate.parse(expStr).atStartOfDay();
                }
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Hạn sử dụng không đúng định dạng (YYYY-MM-DD hoặc ISO-8601)");
            }

            if (expDateTime.isBefore(now)) {
                throw new IllegalArgumentException("Hạn sử dụng phải lớn hơn thời điểm hiện tại");
            }

            String productId = (item.getProductId() != null && !item.getProductId().trim().isEmpty())
                    ? item.getProductId().trim()
                    : generatedProductIds.get(newProdIdx++);

            String defaultStatus = "ON_SALE";

            Product product = new Product(
                    productId,
                    finalCatId,
                    item.getProductName().trim(),
                    item.getPurchasedQuantity(),
                    expDateTime,
                    defaultStatus,
                    item.getUnitPrice()
            );
            productsToPersist.add(product);

            String detailId = detailIds.get(detailIdx++);
            PurchaseReceiptDetail detail = new PurchaseReceiptDetail(
                    detailId,
                    voucherId,
                    productId,
                    item.getPurchasedQuantity(),
                    expDateTime,
                    item.getPurchasePrice()
            );
            detailsToPersist.add(detail);

            BigDecimal lineTotal = item.getPurchasePrice().multiply(BigDecimal.valueOf(item.getPurchasedQuantity()));
            totalAmount = totalAmount.add(lineTotal);

            createdProductDtos.add(new PurchaseReceiptResponse.CreatedProductDto(
                    productId,
                    item.getProductName().trim(),
                    finalCatId,
                    item.getUnitPrice(),
                    item.getPurchasedQuantity(),
                    formatIso(expDateTime),
                    defaultStatus
            ));
        }

        Voucher voucher = new Voucher(
                voucherId,
                currentEmployeeId != null ? currentEmployeeId : "",
                totalAmount,
                now
        );

        PurchaseReceipt purchaseReceipt = new PurchaseReceipt(
                voucherId,
                request.getSupplierName().trim()
        );

        purchaseReceiptRepository.savePurchaseReceiptTransaction(voucher, purchaseReceipt, productsToPersist, detailsToPersist);

        PurchaseReceiptResponse.EmployeeDto createdBy = new PurchaseReceiptResponse.EmployeeDto(
                currentEmployeeId != null ? currentEmployeeId : "",
                currentFullName != null ? currentFullName : ""
        );

        return new PurchaseReceiptResponse(
                voucherId,
                request.getSupplierName().trim(),
                totalAmount,
                formatIso(now),
                createdBy,
                createdProductDtos
        );
    }

    public List<PurchaseReceiptResponse.ListItemDto> getPurchaseReceipts(Integer pageParam,
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

        return purchaseReceiptRepository.findPurchaseReceipts(page, limit, supplierName, employeeId, fromDateTime, toDateTime);
    }

    public PurchaseReceiptResponse.DetailResponse getPurchaseReceiptDetail(String voucherId) {
        if (voucherId == null || voucherId.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã phiếu nhập hàng không được để trống");
        }

        return purchaseReceiptRepository.findPurchaseReceiptDetailById(voucherId.trim())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy phiếu nhập hàng với ID tương ứng"));
    }

    private String formatIso(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        return dateTime.atOffset(ZoneOffset.UTC).format(formatter);
    }
}
