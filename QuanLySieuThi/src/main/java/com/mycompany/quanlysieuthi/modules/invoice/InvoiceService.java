package com.mycompany.quanlysieuthi.modules.invoice;

import com.mycompany.quanlysieuthi.modules.customer.Customer;
import com.mycompany.quanlysieuthi.modules.invoice.dto.CancelInvoiceResponseData;
import com.mycompany.quanlysieuthi.modules.invoice.dto.CreateInvoiceRequest;
import com.mycompany.quanlysieuthi.modules.invoice.dto.CreateInvoiceResponseData;
import com.mycompany.quanlysieuthi.modules.invoice.dto.InvoiceDetailResponse;
import com.mycompany.quanlysieuthi.modules.invoice.dto.InvoiceItemRequest;
import com.mycompany.quanlysieuthi.modules.invoice.dto.InvoiceListItemDto;
import com.mycompany.quanlysieuthi.modules.purchasereceipt.entity.Product;
import com.mycompany.quanlysieuthi.util.NotFoundException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Service handling business logic for invoice operations.
 */
public class InvoiceService {

    private static final Pattern UUID_PATTERN = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
    );
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    private final InvoiceDao invoiceDao;

    public InvoiceService() {
        this.invoiceDao = new InvoiceDao();
    }

    public InvoiceService(InvoiceDao invoiceDao) {
        this.invoiceDao = invoiceDao;
    }

    /**
     * Create an in-store counter invoice (POST /api/invoice)
     */
    public CreateInvoiceResponseData createInvoice(CreateInvoiceRequest request, String employeeId) {
        if (request == null) {
            throw new IllegalArgumentException("Dữ liệu hóa đơn không được để trống");
        }

        if (employeeId == null || employeeId.trim().isEmpty()) {
            throw new IllegalArgumentException("Không xác định được nhân viên lập hóa đơn");
        }

        List<InvoiceItemRequest> items = request.getItems();
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Danh sách sản phẩm mua không được để trống");
        }

        // 1. Validate each item and aggregate required quantities
        Map<String, Integer> stockDeductions = new HashMap<>();
        BigDecimal originalTotal = BigDecimal.ZERO;

        for (InvoiceItemRequest item : items) {
            if (item.getProductId() == null || item.getProductId().trim().isEmpty()) {
                throw new IllegalArgumentException("productId trong danh sách items không được để trống");
            }
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Số lượng sản phẩm (quantity) phải lớn hơn 0");
            }
            if (item.getUnitPrice() == null || item.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Đơn giá sản phẩm (unitPrice) không hợp lệ");
            }

            String pId = item.getProductId().trim();
            stockDeductions.put(pId, stockDeductions.getOrDefault(pId, 0) + item.getQuantity());

            BigDecimal itemTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            originalTotal = originalTotal.add(itemTotal);
        }

        // 2. Validate product existence and inventory availability
        for (Map.Entry<String, Integer> entry : stockDeductions.entrySet()) {
            Product product = invoiceDao.findProductById(entry.getKey())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy sản phẩm với ID: " + entry.getKey()));

            if (product.getStockQuantity() < entry.getValue()) {
                throw new IllegalArgumentException("Sản phẩm '" + product.getProductName() +
                        "' không đủ tồn kho (còn " + product.getStockQuantity() +
                        ", yêu cầu " + entry.getValue() + ")");
            }
        }

        // 3. Handle member customer & loyalty points
        Customer customer = null;
        BigDecimal discountAmount = BigDecimal.ZERO;
        Integer pointsUsed = 0;
        Integer pointsEarned = 0;
        Integer customerRemainingPoints = null;

        String customerId = request.getCustomerId();
        if (customerId != null && !customerId.trim().isEmpty()) {
            customerId = customerId.trim();
            customer = invoiceDao.findCustomerById(customerId)
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy khách hàng với ID: " + request.getCustomerId()));

            int currentPoints = customer.getLoyaltyPoints() != null ? customer.getLoyaltyPoints() : 0;

            if (Boolean.TRUE.equals(request.getUsePoints())) {
                Integer pointsToUse = request.getPointsToUse();
                if (pointsToUse == null || pointsToUse <= 0) {
                    throw new IllegalArgumentException("pointsToUse phải lớn hơn 0 khi chọn usePoints = true");
                }
                if (pointsToUse > currentPoints) {
                    throw new IllegalArgumentException("Khách hàng không đủ điểm tích lũy (cần " +
                            pointsToUse + ", hiện có " + currentPoints + ")");
                }

                // 100,000 points = 1,000 VND discount (max 50,000 VND discount, and not exceeding originalTotal)
                double calcDiscount = (pointsToUse / 100000.0) * 1000.0;
                double maxAllowedDiscount = Math.min(50000.0, originalTotal.doubleValue());
                if (calcDiscount > maxAllowedDiscount) {
                    calcDiscount = maxAllowedDiscount;
                }
                discountAmount = BigDecimal.valueOf(calcDiscount).setScale(2, RoundingMode.HALF_UP);

                customer.setLoyaltyPoints(currentPoints - pointsToUse);
                pointsUsed = pointsToUse;
                pointsEarned = 0;
                customerRemainingPoints = customer.getLoyaltyPoints();
            } else {
                // Earn points: FLOOR(original_total / 10000) * 100
                pointsEarned = (int) Math.floor(originalTotal.doubleValue() / 10000.0) * 100;
                customer.setLoyaltyPoints(currentPoints + pointsEarned);
                pointsUsed = 0;
                discountAmount = BigDecimal.ZERO;
                customerRemainingPoints = customer.getLoyaltyPoints();
            }
        }

        BigDecimal finalAmount = originalTotal.subtract(discountAmount);
        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            finalAmount = BigDecimal.ZERO;
        }

        String invoiceId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();

        Invoice invoice = new Invoice(
                invoiceId,
                customerId,
                employeeId.trim(),
                "PENDING",
                originalTotal,
                discountAmount,
                now
        );

        List<InvoiceDetail> details = new ArrayList<>();
        for (InvoiceItemRequest item : items) {
            String detailId = UUID.randomUUID().toString();
            BigDecimal subtotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            details.add(new InvoiceDetail(
                    detailId,
                    invoiceId,
                    item.getProductId().trim(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    subtotal
            ));
        }

        // Persist transaction
        invoiceDao.saveInvoice(invoice, details, customer, stockDeductions);

        return new CreateInvoiceResponseData(
                invoiceId,
                now.format(ISO_FORMATTER),
                "PENDING",
                employeeId.trim(),
                customerId,
                originalTotal,
                discountAmount,
                finalAmount,
                pointsUsed,
                pointsEarned,
                customerRemainingPoints
        );
    }

    /**
     * Get invoice list with filtering and pagination (GET /api/invoice)
     */
    public List<InvoiceListItemDto> getInvoices(Integer page, Integer limit, String status,
                                               String fromDate, String toDate,
                                               String customerId, String employeeId) {
        if (status != null && !status.trim().isEmpty()) {
            String st = status.trim().toUpperCase();
            if (!"PENDING".equals(st) && !"PAID".equals(st) && !"CANCELLED".equals(st)) {
                throw new IllegalArgumentException("status không hợp lệ (phải là PENDING, PAID hoặc CANCELLED)");
            }
        }

        return invoiceDao.findInvoices(page, limit, status, fromDate, toDate, customerId, employeeId);
    }

    /**
     * Get invoice detail by ID (GET /api/invoice/:id)
     */
    public InvoiceDetailResponse getInvoiceDetail(String invoiceId) {
        validateId(invoiceId);
        return invoiceDao.findInvoiceDetailById(invoiceId.trim())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy hóa đơn với ID: " + invoiceId));
    }

    /**
     * Cancel invoice (PATCH /api/invoice/:id/cancel)
     */
    public CancelInvoiceResponseData cancelInvoice(String invoiceId) {
        validateId(invoiceId);
        boolean success = invoiceDao.cancelInvoice(invoiceId.trim());
        if (!success) {
            throw new NotFoundException("Không tìm thấy hóa đơn với ID: " + invoiceId);
        }
        return new CancelInvoiceResponseData(invoiceId.trim(), "CANCELLED");
    }

    private void validateId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID hóa đơn không được để trống");
        }
        if (!UUID_PATTERN.matcher(id.trim()).matches()) {
            throw new IllegalArgumentException("ID không đúng định dạng UUID");
        }
    }
}
