package com.mycompany.quanlysieuthi.modules.disposalreceipt;

import com.mycompany.quanlysieuthi.common.exception.AppException.*;
import com.mycompany.quanlysieuthi.entity.DisposalReceipt;
import com.mycompany.quanlysieuthi.entity.DisposalReceiptDetail;
import com.mycompany.quanlysieuthi.entity.Product;
import com.mycompany.quanlysieuthi.entity.Voucher;
import com.mycompany.quanlysieuthi.util.IdGenerator;

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

/**
 * Service handling business logic for Disposal Receipt module.
 */
public class DisposalReceiptService {

    private final DisposalReceiptRepository disposalReceiptRepository;

    public DisposalReceiptService() {
        this.disposalReceiptRepository = new DisposalReceiptRepository();
    }

    public DisposalReceiptService(DisposalReceiptRepository disposalReceiptRepository) {
        this.disposalReceiptRepository = disposalReceiptRepository;
    }

    public DisposalReceiptResponse.CreateResponse createDisposalReceipt(DisposalReceiptRequest request,
                                                                        String currentEmployeeId,
                                                                        String currentFullName) {
        if (request == null) {
            throw new IllegalArgumentException("Dữ liệu phiếu hủy kho không được để trống");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Danh sách mặt hàng xuất hủy (items) không được để trống");
        }

        String voucherId = IdGenerator.nextDisposalVoucherId();
        List<String> detailIds = IdGenerator.nextDisposalDetailIds(request.getItems().size());
        int detailIdx = 0;
        LocalDateTime now = LocalDateTime.now();

        List<DisposalReceiptDetail> details = new ArrayList<>();
        Map<String, Integer> productDeductionMap = new HashMap<>();

        for (DisposalReceiptRequest.Item item : request.getItems()) {
            if (item.getProductId() == null || item.getProductId().trim().isEmpty()) {
                throw new IllegalArgumentException("Mã sản phẩm (productId) không được để trống");
            }
            if (item.getDisposedQuantity() == null || item.getDisposedQuantity() <= 0) {
                throw new IllegalArgumentException("Số lượng xuất hủy (disposedQuantity) phải là số nguyên > 0");
            }
            if (item.getDisposalReason() == null || item.getDisposalReason().trim().isEmpty()) {
                throw new IllegalArgumentException("Lý do xuất hủy (disposalReason) không được để trống");
            }

            String pId = item.getProductId().trim();
            Product product = disposalReceiptRepository.findProductById(pId)
                    .orElseThrow(() -> new NotFoundException("Sản phẩm có mã " + pId + " không tồn tại trong hệ thống"));

            int currentStock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
            int disposedQty = item.getDisposedQuantity();

            if (disposedQty > currentStock) {
                throw new IllegalArgumentException(String.format(
                        "Số lượng xuất hủy (%d) lớn hơn số lượng tồn kho hiện tại (%d) của sản phẩm: %s",
                        disposedQty, currentStock, product.getProductName()));
            }

            String detailId = detailIds.get(detailIdx++);
            details.add(new DisposalReceiptDetail(
                    detailId,
                    voucherId,
                    product.getProductId(),
                    disposedQty,
                    item.getDisposalReason().trim()));

            productDeductionMap.put(product.getProductId(),
                    productDeductionMap.getOrDefault(product.getProductId(), 0) + disposedQty);
        }

        Voucher voucher = new Voucher(
                voucherId,
                currentEmployeeId != null ? currentEmployeeId : "",
                BigDecimal.ZERO,
                now);

        DisposalReceipt disposalReceipt = new DisposalReceipt(voucherId);

        disposalReceiptRepository.saveDisposalReceiptTransaction(voucher, disposalReceipt, details, productDeductionMap);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        String formattedDate = now.atOffset(ZoneOffset.UTC).format(formatter);

        return new DisposalReceiptResponse.CreateResponse(
                voucherId,
                BigDecimal.ZERO,
                formattedDate,
                new DisposalReceiptResponse.EmployeeDto(currentEmployeeId, currentFullName),
                details.size());
    }

    public List<DisposalReceiptResponse.ListItemDto> getDisposalReceipts(int page, int limit,
                                                                        String employeeId,
                                                                        String fromDate,
                                                                        String toDate) {
        if (page < 1) page = 1;
        if (limit < 1) limit = 10;
        if (limit > 100) limit = 100;

        LocalDateTime fromDateTime = null;
        LocalDateTime toDateTime = null;

        if (fromDate != null && !fromDate.trim().isEmpty()) {
            try {
                fromDateTime = LocalDate.parse(fromDate.trim()).atStartOfDay();
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Định dạng fromDate không hợp lệ (chuẩn YYYY-MM-DD): " + fromDate);
            }
        }

        if (toDate != null && !toDate.trim().isEmpty()) {
            try {
                toDateTime = LocalDate.parse(toDate.trim()).atTime(LocalTime.MAX);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Định dạng toDate không hợp lệ (chuẩn YYYY-MM-DD): " + toDate);
            }
        }

        return disposalReceiptRepository.findDisposalReceipts(page, limit, employeeId, fromDateTime, toDateTime);
    }

    public DisposalReceiptResponse.DetailResponse getDisposalReceiptDetail(String voucherId) {
        if (voucherId == null || voucherId.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã phiếu hủy (voucherId) không được để trống");
        }

        return disposalReceiptRepository.findDisposalReceiptDetailById(voucherId.trim())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy phiếu hủy kho với mã: " + voucherId));
    }
}
