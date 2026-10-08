package com.mycompany.quanlysieuthi.modules.product.service;

import com.mycompany.quanlysieuthi.modules.product.dao.ProductDao;
import com.mycompany.quanlysieuthi.modules.product.dto.request.UpdateProductRequest;
import com.mycompany.quanlysieuthi.modules.product.dto.response.ProductCategoryDto;
import com.mycompany.quanlysieuthi.modules.product.dto.response.ProductResponseDto;
import com.mycompany.quanlysieuthi.modules.product.dto.response.UpdateProductResponseDto;
import com.mycompany.quanlysieuthi.modules.purchasereceipt.entity.Product;
import com.mycompany.quanlysieuthi.util.NotFoundException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service handling business logic for Product operations.
 */
public class ProductService {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    private final ProductDao productDao;

    public ProductService() {
        this.productDao = new ProductDao();
    }

    public ProductService(ProductDao productDao) {
        this.productDao = productDao;
    }

    public List<ProductResponseDto> getProducts(Integer pageParam, Integer limitParam,
                                                String keyword, String categoryId, String productStatusParam) {
        int page = (pageParam != null) ? pageParam : 1;
        int limit = (limitParam != null) ? limitParam : 10;

        if (page <= 0) {
            throw new IllegalArgumentException("Tham số page phải lớn hơn 0");
        }
        if (limit <= 0) {
            throw new IllegalArgumentException("Tham số limit phải lớn hơn 0");
        }

        String productStatus = productStatusParam;
        if (productStatus != null && !productStatus.trim().isEmpty()) {
            productStatus = productStatus.trim();
            if (!"ON_SALE".equalsIgnoreCase(productStatus) && !"DISCONTINUED".equalsIgnoreCase(productStatus)) {
                throw new IllegalArgumentException("productStatus không hợp lệ (chỉ chấp nhận ON_SALE hoặc DISCONTINUED)");
            }
            productStatus = productStatus.toUpperCase();
        } else {
            productStatus = "ON_SALE"; // Mặc định lọc ON_SALE nếu client không truyền param
        }

        String cleanKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        String cleanCategoryId = (categoryId != null && !categoryId.trim().isEmpty()) ? categoryId.trim() : null;

        List<Object[]> rows = productDao.findProducts(page, limit, cleanKeyword, cleanCategoryId, productStatus);
        List<ProductResponseDto> result = new ArrayList<>();

        for (Object[] row : rows) {
            Product p = (Product) row[0];
            String categoryName = (String) row[1];

            ProductCategoryDto categoryDto = new ProductCategoryDto(
                    p.getCategoryId(),
                    categoryName != null ? categoryName : ""
            );

            String formattedExpirationDate = formatIsoUtc(p.getExpirationDate());

            ProductResponseDto dto = new ProductResponseDto(
                    p.getProductId(),
                    p.getProductName(),
                    p.getUnitPrice(),
                    p.getStockQuantity(),
                    formattedExpirationDate,
                    p.getProductStatus(),
                    categoryDto
            );

            result.add(dto);
        }

        return result;
    }

    public ProductResponseDto getProductById(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID không đúng định dạng UUID.");
        }

        try {
            UUID.fromString(id.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("ID không đúng định dạng UUID.");
        }

        Optional<Object[]> productOpt = productDao.findByIdWithCategory(id.trim());
        if (productOpt.isEmpty()) {
            throw new NotFoundException("Không tìm thấy sản phẩm với ID tương ứng.");
        }

        Object[] row = productOpt.get();
        Product p = (Product) row[0];
        String categoryName = (String) row[1];

        ProductCategoryDto categoryDto = new ProductCategoryDto(
                p.getCategoryId(),
                categoryName != null ? categoryName : ""
        );

        String formattedExpirationDate = formatIsoUtc(p.getExpirationDate());

        return new ProductResponseDto(
                p.getProductId(),
                p.getProductName(),
                p.getUnitPrice(),
                p.getStockQuantity(),
                formattedExpirationDate,
                p.getProductStatus(),
                categoryDto
        );
    }

    public UpdateProductResponseDto updateProduct(String id, UpdateProductRequest request) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID không đúng định dạng UUID.");
        }

        try {
            UUID.fromString(id.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("ID không đúng định dạng UUID.");
        }

        if (request == null) {
            throw new IllegalArgumentException("Request body không được để trống");
        }

        if (request.getProductStatus() != null && !request.getProductStatus().trim().isEmpty()) {
            String status = request.getProductStatus().trim();
            if (!"ON_SALE".equalsIgnoreCase(status) && !"DISCONTINUED".equalsIgnoreCase(status)) {
                throw new IllegalArgumentException("productStatus không thuộc ('ON_SALE', 'DISCONTINUED').");
            }
        }

        if (request.getUnitPrice() != null) {
            if (request.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("unitPrice < 0.");
            }
        }

        Optional<Product> productOpt = productDao.findById(id.trim());
        if (productOpt.isEmpty()) {
            throw new NotFoundException("Không tìm thấy productId hoặc categoryId không tồn tại.");
        }

        Product product = productOpt.get();

        if (request.getCategoryId() != null && !request.getCategoryId().trim().isEmpty()) {
            String categoryId = request.getCategoryId().trim();
            try {
                UUID.fromString(categoryId);
            } catch (IllegalArgumentException e) {
                throw new NotFoundException("Không tìm thấy productId hoặc categoryId không tồn tại.");
            }

            if (!productDao.existsCategoryById(categoryId)) {
                throw new NotFoundException("Không tìm thấy productId hoặc categoryId không tồn tại.");
            }
            product.setCategoryId(categoryId);
        }

        if (request.getProductName() != null && !request.getProductName().trim().isEmpty()) {
            product.setProductName(request.getProductName().trim());
        }

        if (request.getUnitPrice() != null) {
            product.setUnitPrice(request.getUnitPrice());
        }

        if (request.getProductStatus() != null && !request.getProductStatus().trim().isEmpty()) {
            product.setProductStatus(request.getProductStatus().trim().toUpperCase());
        }

        Product updatedProduct = productDao.updateProduct(product);

        String formattedExpirationDate = formatIsoUtc(updatedProduct.getExpirationDate());

        return new UpdateProductResponseDto(
                updatedProduct.getProductId(),
                updatedProduct.getProductName(),
                updatedProduct.getCategoryId(),
                updatedProduct.getUnitPrice(),
                updatedProduct.getProductStatus(),
                updatedProduct.getStockQuantity(),
                formattedExpirationDate
        );
    }

    private String formatIsoUtc(LocalDateTime ldt) {
        if (ldt == null) {
            return null;
        }
        return ldt.atOffset(ZoneOffset.UTC).format(ISO_FORMATTER);
    }
}

