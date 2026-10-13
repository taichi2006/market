package com.mycompany.quanlysieuthi.modules.category;

import com.mycompany.quanlysieuthi.entity.ProductCategory;
import com.mycompany.quanlysieuthi.util.ConflictException;
import com.mycompany.quanlysieuthi.util.IdGenerator;
import com.mycompany.quanlysieuthi.util.NotFoundException;

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
        List<ProductCategory> list = categoryRepository.findCategories(page, limit, keyword);
        long total = categoryRepository.countCategories(keyword);

        List<CategoryResponse> data = new ArrayList<>();
        for (ProductCategory c : list) {
            long pCount = categoryRepository.countProductsByCategoryId(c.getCategoryId());
            data.add(new CategoryResponse(c.getCategoryId(), c.getCategoryName(), pCount));
        }

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
        List<ProductCategory> list = categoryRepository.findAll();
        List<CategoryResponse> data = new ArrayList<>();
        for (ProductCategory c : list) {
            long pCount = categoryRepository.countProductsByCategoryId(c.getCategoryId());
            data.add(new CategoryResponse(c.getCategoryId(), c.getCategoryName(), pCount));
        }
        return data;
    }

    public CategoryResponse getCategoryById(String categoryId) {
        if (categoryId == null || categoryId.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã loại sản phẩm không hợp lệ");
        }
        ProductCategory category = categoryRepository.findById(categoryId.trim())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy loại sản phẩm với mã: " + categoryId));

        long pCount = categoryRepository.countProductsByCategoryId(category.getCategoryId());
        return new CategoryResponse(category.getCategoryId(), category.getCategoryName(), pCount);
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
        long pCount = categoryRepository.countProductsByCategoryId(updated.getCategoryId());
        return new CategoryResponse(updated.getCategoryId(), updated.getCategoryName(), pCount);
    }

    public void deleteCategory(String categoryId) {
        if (categoryId == null || categoryId.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã loại sản phẩm không hợp lệ");
        }
        ProductCategory existing = categoryRepository.findById(categoryId.trim())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy loại sản phẩm với mã: " + categoryId));

        long pCount = categoryRepository.countProductsByCategoryId(existing.getCategoryId());
        if (pCount > 0) {
            throw new ConflictException("Không thể xóa loại sản phẩm '" + existing.getCategoryName() +
                    "' vì đang có " + pCount + " sản phẩm thuộc loại này.");
        }

        categoryRepository.delete(existing);
    }
}
