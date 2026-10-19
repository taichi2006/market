package com.mycompany.quanlysieuthi.modules.product;

import com.mycompany.quanlysieuthi.entity.Product;
import com.mycompany.quanlysieuthi.common.exception.AppException.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service handling business logic for Product operations.
 */
public class ProductService {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    private final ProductRepository productRepository;

    public ProductService() {
        this.productRepository = new ProductRepository();
    }

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductResponse> getProducts(Integer pageParam, Integer limitParam,
            String keyword, String categoryId, String productStatus) {
        int page = (pageParam != null) ? pageParam : 1;
        int limit = (limitParam != null) ? limitParam : 10;

        if (page <= 0) {
            throw new IllegalArgumentException("Tham số page phải lớn hơn 0.");
        }
        if (limit <= 0) {
            throw new IllegalArgumentException("Tham số limit phải lớn hơn 0.");
        }

        if (productStatus != null && !productStatus.trim().isEmpty()) {
            String status = productStatus.trim().toUpperCase();
            if (!status.equals("ON_SALE") && !status.equals("DISCONTINUED")) {
                throw new IllegalArgumentException(
                        "productStatus không hợp lệ. Chỉ chấp nhận ON_SALE hoặc DISCONTINUED.");
            }
        }

        List<Object[]> rows = productRepository.findProducts(page, limit, keyword, categoryId, productStatus);
        List<ProductResponse> result = new ArrayList<>();

        for (Object[] row : rows) {
            Product p = (Product) row[0];
            String categoryName = (String) row[1];

            ProductResponse.CategoryDto categoryDto = new ProductResponse.CategoryDto(
                    p.getCategoryId(),
                    categoryName != null ? categoryName : "");

            String formattedExpirationDate = formatIsoUtc(p.getExpirationDate());

            ProductResponse dto = new ProductResponse(
                    p.getProductId(),
                    p.getProductName(),
                    p.getUnitPrice(),
                    p.getStockQuantity(),
                    formattedExpirationDate,
                    p.getProductStatus(),
                    categoryDto);

            result.add(dto);
        }

        return result;
    }

    public ProductResponse getProductById(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID sản phẩm không được để trống.");
        }

        Optional<Object[]> productOpt = productRepository.findByIdWithCategory(id.trim());
        if (productOpt.isEmpty()) {
            throw new NotFoundException("Không tìm thấy sản phẩm với ID tương ứng.");
        }

        Object[] row = productOpt.get();
        Product p = (Product) row[0];
        String categoryName = (String) row[1];

        ProductResponse.CategoryDto categoryDto = new ProductResponse.CategoryDto(
                p.getCategoryId(),
                categoryName != null ? categoryName : "");

        String formattedExpirationDate = formatIsoUtc(p.getExpirationDate());

        return new ProductResponse(
                p.getProductId(),
                p.getProductName(),
                p.getUnitPrice(),
                p.getStockQuantity(),
                formattedExpirationDate,
                p.getProductStatus(),
                categoryDto);
    }

    public ProductResponse.UpdateResponseDto updateProduct(String id, ProductRequest.Update request) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID sản phẩm không được để trống.");
        }
        if (request == null) {
            throw new IllegalArgumentException("Dữ liệu cập nhật sản phẩm không được để trống.");
        }

        Optional<Product> productOpt = productRepository.findById(id.trim());
        if (productOpt.isEmpty()) {
            throw new NotFoundException("Không tìm thấy sản phẩm với ID tương ứng.");
        }

        Product product = productOpt.get();

        if (request.getProductName() != null) {
            if (request.getProductName().trim().isEmpty()) {
                throw new IllegalArgumentException("Tên sản phẩm không được rỗng.");
            }
            product.setProductName(request.getProductName().trim());
        }

        if (request.getUnitPrice() != null) {
            if (request.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Đơn giá sản phẩm không được âm.");
            }
            product.setUnitPrice(request.getUnitPrice());
        }

        if (request.getProductStatus() != null) {
            String status = request.getProductStatus().trim().toUpperCase();
            if (!status.equals("ON_SALE") && !status.equals("DISCONTINUED")) {
                throw new IllegalArgumentException(
                        "productStatus không hợp lệ. Chỉ chấp nhận ON_SALE hoặc DISCONTINUED.");
            }
            product.setProductStatus(status);
        }

        if (request.getCategoryId() != null) {
            String catId = request.getCategoryId().trim();
            if (catId.isEmpty()) {
                throw new IllegalArgumentException("Mã loại sản phẩm không được rỗng.");
            }
            if (!productRepository.existsCategoryById(catId)) {
                throw new NotFoundException("Loại sản phẩm (categoryId) không tồn tại.");
            }
            product.setCategoryId(catId);
        }

        Product updatedProduct = productRepository.updateProduct(product);

        String categoryName = "";
        Optional<Object[]> catRowOpt = productRepository.findProductWithCategoryById(updatedProduct.getProductId());
        if (catRowOpt.isPresent()) {
            categoryName = (String) catRowOpt.get()[1];
            if (categoryName == null)
                categoryName = "";
        }

        ProductResponse.CategoryDto categoryDto = new ProductResponse.CategoryDto(
                updatedProduct.getCategoryId(),
                categoryName);

        String formattedExpirationDate = formatIsoUtc(updatedProduct.getExpirationDate());

        return new ProductResponse.UpdateResponseDto(
                updatedProduct.getProductId(),
                updatedProduct.getProductName(),
                updatedProduct.getUnitPrice(),
                updatedProduct.getStockQuantity(),
                formattedExpirationDate,
                updatedProduct.getProductStatus(),
                categoryDto);
    }

    public List<ProductResponse.LowStockDto> getLowStockAlerts(Integer thresholdParam, String categoryId,
            Integer pageParam, Integer limitParam) {
        int threshold = (thresholdParam != null) ? thresholdParam : 10;
        int page = (pageParam != null) ? pageParam : 1;
        int limit = (limitParam != null) ? limitParam : 10;

        if (threshold < 0) {
            throw new IllegalArgumentException("Ngưỡng tồn kho (threshold) phải lớn hơn hoặc bằng 0.");
        }
        if (page <= 0) {
            throw new IllegalArgumentException("Tham số page phải lớn hơn 0.");
        }
        if (limit <= 0) {
            throw new IllegalArgumentException("Tham số limit phải lớn hơn 0.");
        }

        List<Object[]> rows = productRepository.findLowStockProducts(threshold, categoryId, page, limit);
        List<ProductResponse.LowStockDto> result = new ArrayList<>();

        for (Object[] row : rows) {
            Product p = (Product) row[0];
            String categoryName = (String) row[1];

            ProductResponse.CategoryDto catDto = new ProductResponse.CategoryDto(
                    p.getCategoryId(),
                    categoryName != null ? categoryName : "");

            result.add(new ProductResponse.LowStockDto(
                    p.getProductId(),
                    p.getProductName(),
                    p.getStockQuantity(),
                    catDto));
        }

        return result;
    }

    public long countLowStockAlerts(Integer thresholdParam, String categoryId) {
        int threshold = (thresholdParam != null) ? thresholdParam : 10;
        if (threshold < 0) {
            throw new IllegalArgumentException("Ngưỡng tồn kho (threshold) phải lớn hơn hoặc bằng 0.");
        }
        return productRepository.countLowStockProducts(threshold, categoryId);
    }

    /**
     * Đánh giá rủi ro kho của sản phẩm bằng Scalar Function
     * dbo.fn_EvaluateProductStockRisk.
     */
    public ProductResponse.RiskAssessmentDto evaluateProductStockRisk(String productId) {
        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID sản phẩm không được để trống.");
        }
        String risk = productRepository.evaluateProductStockRisk(productId.trim());
        return new ProductResponse.RiskAssessmentDto(productId.trim(), risk);
    }

    /**
     * Lấy danh sách sản phẩm sắp hết hạn và tính toán giá trị thiệt hại tiềm ẩn
     * bằng TVF dbo.fn_GetExpiringProductsByDays.
     */
    public List<ProductResponse.ExpiringProductDto> getExpiringProducts(Integer thresholdDays) {
        int threshold = (thresholdDays != null && thresholdDays > 0) ? thresholdDays : 30;
        return productRepository.findExpiringProducts(threshold);
    }

    /**
     * Điều chỉnh số lượng tồn kho sản phẩm có kiểm tra hợp lệ bằng Stored Procedure
     * dbo.sp_AdjustProductStockWithAudit.
     */
    public int adjustStock(String productId, Integer quantityChange, String reason) {
        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID sản phẩm không được để trống.");
        }
        if (quantityChange == null || quantityChange == 0) {
            throw new IllegalArgumentException("Số lượng điều chỉnh phải khác 0.");
        }
        return productRepository.adjustProductStockWithAudit(productId.trim(), quantityChange, reason);
    }

    /**
     * Trừ tồn kho an toàn chống xung đột tranh chấp (Pessimistic Lock) bằng Stored
     * Procedure dbo.sp_SafeDeductProductStock.
     */
    public void safeDeductStock(String productId, Integer deductQuantity) {
        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID sản phẩm không được để trống.");
        }
        if (deductQuantity == null || deductQuantity <= 0) {
            throw new IllegalArgumentException("Số lượng trừ kho phải lớn hơn 0.");
        }
        productRepository.safeDeductProductStock(productId.trim(), deductQuantity);
    }

    private String formatIsoUtc(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.atOffset(ZoneOffset.UTC).format(ISO_FORMATTER);
    }
}
