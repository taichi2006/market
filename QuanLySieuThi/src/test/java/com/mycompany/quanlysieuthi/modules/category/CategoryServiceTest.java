package com.mycompany.quanlysieuthi.modules.category;

import com.mycompany.quanlysieuthi.common.exception.AppException.*;
import com.mycompany.quanlysieuthi.entity.ProductCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test cho CategoryService")
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        categoryService = new CategoryService(categoryRepository);
    }

    @Nested
    @DisplayName("Kiểm thử getCategories() & getAllCategories() với View")
    class GetCategoriesTests {

        @Test
        @DisplayName("Lấy danh sách phân trang thành công từ View vw_ProductCategoryInventorySummary")
        void testGetCategories_Success() {
            CategoryResponse resp = new CategoryResponse("LSP001", "Bánh kẹo", 10L);
            resp.setTotalStockQuantity(150);
            resp.setTotalInventoryValue(new BigDecimal("3000000.00"));

            when(categoryRepository.findCategorySummaries(1, 10, "Bánh"))
                    .thenReturn(List.of(resp));
            when(categoryRepository.countCategories("Bánh")).thenReturn(1L);

            Map<String, Object> result = categoryService.getCategories(1, 10, "Bánh");

            assertNotNull(result);
            assertTrue(result.containsKey("data"));
            assertTrue(result.containsKey("meta"));
            List<?> data = (List<?>) result.get("data");
            assertEquals(1, data.size());
            verify(categoryRepository).findCategorySummaries(1, 10, "Bánh");
        }

        @Test
        @DisplayName("Lấy tất cả danh mục thành công từ View")
        void testGetAllCategories_Success() {
            CategoryResponse resp = new CategoryResponse("LSP001", "Đồ uống", 5L);
            when(categoryRepository.findAllCategorySummaries()).thenReturn(List.of(resp));

            List<CategoryResponse> list = categoryService.getAllCategories();

            assertNotNull(list);
            assertEquals(1, list.size());
            assertEquals("Đồ uống", list.get(0).getCategoryName());
            verify(categoryRepository).findAllCategorySummaries();
        }
    }

    @Nested
    @DisplayName("Kiểm thử deleteCategory() thông qua Trigger trg_PreventDeleteCategoryWithProducts")
    class DeleteCategoryTests {

        @Test
        @DisplayName("Xóa thành công khi trigger cho phép (gỡ category_id cho SP ngừng kinh doanh)")
        void testDeleteCategory_Success() {
            doNothing().when(categoryRepository).deleteCategoryWithTrigger("LSP001");

            assertDoesNotThrow(() -> categoryService.deleteCategory("LSP001"));
            verify(categoryRepository).deleteCategoryWithTrigger("LSP001");
        }

        @Test
        @DisplayName("Ném ConflictException khi trigger phát hiện sản phẩm đang kinh doanh hoặc còn tồn kho")
        void testDeleteCategory_TriggerConflict_ThrowsException() {
            doThrow(new ConflictException("Không thể xóa danh mục này vì vẫn còn sản phẩm đang kinh doanh hoặc còn tồn kho!"))
                    .when(categoryRepository).deleteCategoryWithTrigger("LSP001");

            ConflictException ex = assertThrows(ConflictException.class, () ->
                    categoryService.deleteCategory("LSP001")
            );
            assertTrue(ex.getMessage().contains("Không thể xóa danh mục này"));
        }

        @Test
        @DisplayName("Ném ngoại lệ khi categoryId bị null hoặc rỗng")
        void testDeleteCategory_InvalidId_ThrowsException() {
            assertThrows(IllegalArgumentException.class, () ->
                    categoryService.deleteCategory(null)
            );
            assertThrows(IllegalArgumentException.class, () ->
                    categoryService.deleteCategory("   ")
            );
        }
    }

    @Nested
    @DisplayName("Kiểm thử mergeCategories() qua Stored Procedure sp_MergeProductCategories")
    class MergeCategoriesTests {

        @Test
        @DisplayName("Gộp danh mục thành công")
        void testMergeCategories_Success() {
            doNothing().when(categoryRepository).mergeCategories("LSP001", "LSP002");

            assertDoesNotThrow(() -> categoryService.mergeCategories("LSP001", "LSP002"));
            verify(categoryRepository).mergeCategories("LSP001", "LSP002");
        }

        @Test
        @DisplayName("Ném ConflictException khi 2 danh mục trùng nhau")
        void testMergeCategories_SameId_ThrowsException() {
            assertThrows(ConflictException.class, () ->
                    categoryService.mergeCategories("LSP001", "LSP001")
            );
        }

        @Test
        @DisplayName("Ném IllegalArgumentException khi thiếu mã danh mục")
        void testMergeCategories_NullId_ThrowsException() {
            assertThrows(IllegalArgumentException.class, () ->
                    categoryService.mergeCategories(null, "LSP002")
            );
            assertThrows(IllegalArgumentException.class, () ->
                    categoryService.mergeCategories("LSP001", "")
            );
        }
    }
}
