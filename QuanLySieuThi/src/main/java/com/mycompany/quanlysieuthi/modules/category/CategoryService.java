package com.mycompany.quanlysieuthi.modules.category;

import com.mycompany.quanlysieuthi.entity.ProductCategory;
import com.mycompany.quanlysieuthi.common.exception.AppException.*;
import com.mycompany.quanlysieuthi.util.IdGenerator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service handling business logic for ProductCategory.
 */
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService() {
        this.categoryRepository = new CategoryRepository();
    }

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public CategoryResponse createCategory(CategoryRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Dữ liệu loại sản phẩm không được để trống");
        }
        if (request.getCategoryName() == null || request.getCategoryName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên loại sản phẩm (categoryName) không được để trống");
        }

        String name = request.getCategoryName().trim();
        if (categoryRepository.existsByName(name, null)) {
            throw new ConflictException("Tên loại sản phẩm '" + name + "' đã tồn tại trong hệ thống");
        }

        // Mã loại sản phẩm luôn được hệ thống tự động sinh (LSP001, LSP002...)
        String categoryId = IdGenerator.nextCategoryId();

        ProductCategory category = new ProductCategory(categoryId, name);
        ProductCategory saved = categoryRepository.save(category);

        return new CategoryResponse(saved.getCategoryId(), saved.getCategoryName(), 0L);
    }

    public Map<String, Object> getCategories(int page, int limit, String keyword) {
        // Sử dụng trực tiếp View vw_ProductCategoryInventorySummary để loại bỏ N+1 query
        List<CategoryResponse> data = categoryRepository.findCategorySummaries(page, limit, keyword);
        long total = categoryRepository.countCategories(keyword);

        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("page", page);
        meta.put("limit", limit);
        meta.put("total", total);
        meta.put("totalPages", limit > 0 ? (int) Math.ceil((double) total / limit) : 1);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("data", data);
        result.put("meta", meta);
        return result;
    }

    public List<CategoryResponse> getAllCategories() {
        // Tối ưu hóa truy vấn thông qua View vw_ProductCategoryInventorySummary
        return categoryRepository.findAllCategorySummaries();
    }

    public CategoryResponse getCategoryById(String categoryId) {
        if (categoryId == null || categoryId.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã loại sản phẩm không hợp lệ");
        }
        return categoryRepository.findCategorySummaryById(categoryId.trim())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy loại sản phẩm với mã: " + categoryId));
    }

    public CategoryResponse updateCategory(String categoryId, CategoryRequest request) {
        if (categoryId == null || categoryId.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã loại sản phẩm không hợp lệ");
        }
        if (request == null || request.getCategoryName() == null || request.getCategoryName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên loại sản phẩm không được để trống");
        }

        ProductCategory existing = categoryRepository.findById(categoryId.trim())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy loại sản phẩm với mã: " + categoryId));

        String newName = request.getCategoryName().trim();
        if (categoryRepository.existsByName(newName, existing.getCategoryId())) {
            throw new ConflictException("Tên loại sản phẩm '" + newName + "' đã được sử dụng bởi loại sản phẩm khác");
        }

        existing.setCategoryName(newName);
        ProductCategory updated = categoryRepository.update(existing);
        return getCategoryById(updated.getCategoryId());
    }

    public void deleteCategory(String categoryId) {
        if (categoryId == null || categoryId.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã loại sản phẩm không hợp lệ");
        }
        // Nghiệp vụ kiểm tra danh mục có sản phẩm đang kinh doanh hoặc còn tồn kho
        // đã được chuyển giao hoàn toàn cho Trigger trg_PreventDeleteCategoryWithProducts trong database.
        // Trigger sẽ tự động gỡ liên kết category_id = NULL cho các sản phẩm đã ngừng kinh doanh/hết hàng
        // hoặc ROLLBACK và báo lỗi nếu sản phẩm vẫn đang kinh doanh/còn tồn kho.
        categoryRepository.deleteCategoryWithTrigger(categoryId.trim());
    }

    public void mergeCategories(String sourceCategoryId, String targetCategoryId) {
        if (sourceCategoryId == null || sourceCategoryId.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã danh mục nguồn không được để trống");
        }
        if (targetCategoryId == null || targetCategoryId.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã danh mục đích không được để trống");
        }
        if (sourceCategoryId.trim().equalsIgnoreCase(targetCategoryId.trim())) {
            throw new ConflictException("Danh mục nguồn và danh mục đích không được trùng nhau!");
        }
        // Gọi Stored Procedure sp_MergeProductCategories để chuyển giao dịch chuyển toàn bộ sản phẩm và xóa danh mục nguồn trong CSDL
        categoryRepository.mergeCategories(sourceCategoryId.trim(), targetCategoryId.trim());
    }
}
