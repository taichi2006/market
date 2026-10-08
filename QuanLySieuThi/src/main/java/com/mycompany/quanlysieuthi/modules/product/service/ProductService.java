package com.mycompany.quanlysieuthi.modules.product.service;

import com.mycompany.quanlysieuthi.modules.product.dao.ProductDao;
import com.mycompany.quanlysieuthi.modules.product.dto.response.ProductCategoryDto;
import com.mycompany.quanlysieuthi.modules.product.dto.response.ProductResponseDto;
import com.mycompany.quanlysieuthi.modules.purchasereceipt.entity.Product;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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
                    p.getStockQuantity(),
                    formattedExpirationDate,
                    p.getProductStatus(),
                    categoryDto
            );

            result.add(dto);
        }

        return result;
    }

    private String formatIsoUtc(LocalDateTime ldt) {
        if (ldt == null) {
            return null;
        }
        return ldt.atOffset(ZoneOffset.UTC).format(ISO_FORMATTER);
    }
}
