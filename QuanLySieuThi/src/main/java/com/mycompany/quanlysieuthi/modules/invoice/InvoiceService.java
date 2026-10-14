package com.mycompany.quanlysieuthi.modules.invoice;

import com.mycompany.quanlysieuthi.entity.Customer;
import com.mycompany.quanlysieuthi.entity.Invoice;
import com.mycompany.quanlysieuthi.entity.InvoiceDetail;
import com.mycompany.quanlysieuthi.entity.Product;
import com.mycompany.quanlysieuthi.common.exception.AppException.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.mycompany.quanlysieuthi.util.IdGenerator;

/**
 * Service handling business logic for Invoice operations.
 */
public class InvoiceService {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    private final InvoiceRepository invoiceRepository;

    public InvoiceService() {
        this.invoiceRepository = new InvoiceRepository();
    }

    public InvoiceService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    public InvoiceResponse.CreateResponse createInvoice(InvoiceRequest.Create request, String employeeId, String employeeFullName) {
        if (request == null) {
            throw new IllegalArgumentException("Dữ liệu hóa đơn không được để trống");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Danh sách sản phẩm không được rỗng");
        }

        Customer customer = null;
        if (request.getCustomerId() != null && !request.getCustomerId().trim().isEmpty()) {
            customer = invoiceRepository.findCustomerById(request.getCustomerId().trim())
                    .orElseThrow(() -> new NotFoundException("Khách hàng không tồn tại: " + request.getCustomerId()));
        }

        String invoiceId = IdGenerator.nextInvoiceId();
        List<String> detailIds = IdGenerator.nextInvoiceDetailIds(request.getItems().size());
        int detailIndex = 0;
        LocalDateTime now = LocalDateTime.now();

        BigDecimal originalTotal = BigDecimal.ZERO;
        List<InvoiceDetail> details = new ArrayList<>();
        List<InvoiceResponse.ItemDetailDto> itemDtos = new ArrayList<>();
        Map<String, Integer> stockDeductions = new LinkedHashMap<>();

        for (InvoiceRequest.Item itemReq : request.getItems()) {
            if (itemReq.getProductId() == null || itemReq.getProductId().trim().isEmpty()) {
                throw new IllegalArgumentException("Mã sản phẩm không được để trống");
            }
            if (itemReq.getQuantity() == null || itemReq.getQuantity() <= 0) {
                throw new IllegalArgumentException("Số lượng sản phẩm phải > 0");
            }

            Product product = invoiceRepository.findProductById(itemReq.getProductId().trim())
                    .orElseThrow(() -> new NotFoundException("Sản phẩm không tồn tại: " + itemReq.getProductId()));

            int neededQty = stockDeductions.getOrDefault(product.getProductId(), 0) + itemReq.getQuantity();
            if (product.getStockQuantity() < neededQty) {
                throw new IllegalArgumentException("Sản phẩm '" + product.getProductName() +
                        "' không đủ tồn kho (hiện có: " + product.getStockQuantity() + ")");
            }
            stockDeductions.put(product.getProductId(), neededQty);

            BigDecimal subtotal = product.getUnitPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            originalTotal = originalTotal.add(subtotal);

            String detailId = detailIds.get(detailIndex++);
            details.add(new InvoiceDetail(detailId, invoiceId, product.getProductId(),
                    itemReq.getQuantity(), product.getUnitPrice(), subtotal));

            itemDtos.add(new InvoiceResponse.ItemDetailDto(detailId, product.getProductId(),
                    product.getProductName(), itemReq.getQuantity(), product.getUnitPrice(), subtotal));
        }

        // Loyalty discount calculation
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (customer != null && Boolean.TRUE.equals(request.getUseLoyaltyPoints())) {
            int currentPoints = customer.getLoyaltyPoints() != null ? customer.getLoyaltyPoints() : 0;
            if (currentPoints >= 100000) {
                int pointsToUse = (currentPoints / 100000) * 100000;
                discountAmount = BigDecimal.valueOf((pointsToUse / 100000) * 1000L);
                if (discountAmount.compareTo(originalTotal) > 0) {
                    discountAmount = originalTotal;
                }
                customer.setLoyaltyPoints(currentPoints - pointsToUse);
            }
        }

        BigDecimal finalTotal = originalTotal.subtract(discountAmount);

        // Accumulate loyalty points if member and didn't use points for discount
        if (customer != null && discountAmount.compareTo(BigDecimal.ZERO) == 0) {
            int earnedPoints = (int) Math.floor(originalTotal.doubleValue() / 10000.0) * 100;
            int current = customer.getLoyaltyPoints() != null ? customer.getLoyaltyPoints() : 0;
            customer.setLoyaltyPoints(current + earnedPoints);
        }

        Invoice invoice = new Invoice(
                invoiceId,
                customer != null ? customer.getCustomerId() : null,
                employeeId != null ? employeeId : "",
                "PENDING",
                originalTotal,
                discountAmount,
                now
        );

        invoiceRepository.saveInvoice(invoice, details, customer, stockDeductions);

        InvoiceResponse.EmployeeDto empDto = new InvoiceResponse.EmployeeDto(
                employeeId != null ? employeeId : "",
                employeeFullName != null ? employeeFullName : ""
        );

        InvoiceResponse.CustomerDto custDto = customer != null ? new InvoiceResponse.CustomerDto(
                customer.getCustomerId(),
                customer.getFullName(),
                customer.getPhoneNumber(),
                customer.getLoyaltyPoints()
        ) : null;

        return new InvoiceResponse.CreateResponse(
                invoiceId,
                now.atOffset(ZoneOffset.UTC).format(ISO_FORMATTER),
                "PENDING",
                originalTotal,
                discountAmount,
                finalTotal,
                empDto,
                custDto,
                itemDtos
        );
    }

    public List<InvoiceResponse.ListItemDto> getInvoices(Integer page, Integer limit, String status,
                                                         String fromDate, String toDate,
                                                         String customerId, String employeeId) {
        return invoiceRepository.findInvoices(page, limit, status, fromDate, toDate, customerId, employeeId);
    }

    public InvoiceResponse.DetailResponse getInvoiceDetail(String invoiceId) {
        if (invoiceId == null || invoiceId.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã hóa đơn không được để trống");
        }
        return invoiceRepository.findInvoiceDetailById(invoiceId.trim())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy hóa đơn với ID tương ứng"));
    }

    public InvoiceResponse.CancelResponse cancelInvoice(String invoiceId) {
        if (invoiceId == null || invoiceId.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã hóa đơn không được để trống");
        }
        boolean cancelled = invoiceRepository.cancelInvoice(invoiceId.trim());
        if (!cancelled) {
            throw new NotFoundException("Không tìm thấy hóa đơn với ID: " + invoiceId);
        }
        return new InvoiceResponse.CancelResponse(invoiceId.trim(), "CANCELLED");
    }
}
