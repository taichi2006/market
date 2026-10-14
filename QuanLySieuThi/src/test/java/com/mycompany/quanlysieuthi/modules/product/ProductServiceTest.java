package com.mycompany.quanlysieuthi.modules.product;

import com.mycompany.quanlysieuthi.entity.Product;
import com.mycompany.quanlysieuthi.common.exception.AppException.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test cho ProductService")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository);
    }

    // =========================================================================
    // 1. GET PRODUCTS (DANH SÁCH SẢN PHẨM)
    // =========================================================================
    @Nested
    @DisplayName("Kiểm thử getProducts()")
    class GetProductsTests {

        @Test
        @DisplayName("Ném ngoại lệ khi tham số page <= 0")
        void testGetProducts_InvalidPage_ThrowsIllegalArgumentException() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    productService.getProducts(0, 10, null, null, null)
            );
            assertEquals("Tham số page phải lớn hơn 0.", ex.getMessage());
        }

        @Test
        @DisplayName("Ném ngoại lệ khi tham số limit <= 0")
        void testGetProducts_InvalidLimit_ThrowsIllegalArgumentException() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    productService.getProducts(1, -5, null, null, null)
            );
            assertEquals("Tham số limit phải lớn hơn 0.", ex.getMessage());
        }

        @Test
        @DisplayName("Ném ngoại lệ khi productStatus không thuộc ON_SALE hoặc DISCONTINUED")
        void testGetProducts_InvalidStatus_ThrowsIllegalArgumentException() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    productService.getProducts(1, 10, null, null, "OUT_OF_STOCK")
            );
            assertEquals("productStatus không hợp lệ. Chỉ chấp nhận ON_SALE hoặc DISCONTINUED.", ex.getMessage());
        }

        @Test
        @DisplayName("Lấy danh sách thành công khi truyền tham số hợp lệ")
        void testGetProducts_Success() {
            // Arrange
            Product mockProduct = new Product(
                    "PRD-001",
                    "CAT-001",
                    "Coca Cola 330ml",
                    100,
                    LocalDateTime.of(2027, 12, 31, 0, 0),
                    "ON_SALE",
                    new BigDecimal("10000.00")
            );
            List<Object[]> mockRows = new ArrayList<>();
            mockRows.add(new Object[]{ mockProduct, "Beverages" });

            when(productRepository.findProducts(1, 10, "Coca", "CAT-001", "ON_SALE"))
                    .thenReturn(mockRows);

            // Act
            List<ProductResponse> result = productService.getProducts(1, 10, "Coca", "CAT-001", "ON_SALE");

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            ProductResponse item = result.get(0);
            assertEquals("PRD-001", item.getProductId());
            assertEquals("Coca Cola 330ml", item.getProductName());
            assertEquals(new BigDecimal("10000.00"), item.getUnitPrice());
            assertEquals("Beverages", item.getCategory().getCategoryName());
            assertEquals("CAT-001", item.getCategory().getCategoryId());
            verify(productRepository, times(1)).findProducts(1, 10, "Coca", "CAT-001", "ON_SALE");
        }

        @Test
        @DisplayName("Dùng giá trị mặc định (page=1, limit=10) khi truyền null")
        void testGetProducts_DefaultPagination() {
            when(productRepository.findProducts(1, 10, null, null, null))
                    .thenReturn(Collections.emptyList());

            List<ProductResponse> result = productService.getProducts(null, null, null, null, null);

            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(productRepository, times(1)).findProducts(1, 10, null, null, null);
        }
    }

    // =========================================================================
    // 2. GET PRODUCT BY ID (CHI TIẾT SẢN PHẨM)
    // =========================================================================
    @Nested
    @DisplayName("Kiểm thử getProductById()")
    class GetProductByIdTests {

        @Test
        @DisplayName("Ném ngoại lệ khi ID bị null hoặc rỗng")
        void testGetProductById_EmptyId_ThrowsIllegalArgumentException() {
            IllegalArgumentException exNull = assertThrows(IllegalArgumentException.class, () ->
                    productService.getProductById(null)
            );
            assertEquals("ID sản phẩm không được để trống.", exNull.getMessage());

            IllegalArgumentException exEmpty = assertThrows(IllegalArgumentException.class, () ->
                    productService.getProductById("   ")
            );
            assertEquals("ID sản phẩm không được để trống.", exEmpty.getMessage());
        }

        @Test
        @DisplayName("Ném NotFoundException khi không tìm thấy sản phẩm trong CSDL")
        void testGetProductById_NotFound_ThrowsNotFoundException() {
            when(productRepository.findByIdWithCategory("PRD-999")).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(NotFoundException.class, () ->
                    productService.getProductById("PRD-999")
            );
            assertEquals("Không tìm thấy sản phẩm với ID tương ứng.", ex.getMessage());
        }

        @Test
        @DisplayName("Lấy chi tiết thành công và map đúng DTO")
        void testGetProductById_Success() {
            Product mockProduct = new Product(
                    "PRD-005",
                    "CAT-002",
                    "Alpenliebe Candy",
                    150,
                    LocalDateTime.of(2027, 12, 31, 0, 0),
                    "ON_SALE",
                    new BigDecimal("12000.00")
            );
            Object[] row = new Object[]{ mockProduct, "Confectionery" };

            when(productRepository.findByIdWithCategory("PRD-005")).thenReturn(Optional.of(row));

            ProductResponse response = productService.getProductById("PRD-005");

            assertNotNull(response);
            assertEquals("PRD-005", response.getProductId());
            assertEquals("Alpenliebe Candy", response.getProductName());
            assertEquals(150, response.getStockQuantity());
            assertEquals(new BigDecimal("12000.00"), response.getUnitPrice());
            assertEquals("ON_SALE", response.getProductStatus());
            assertEquals("CAT-002", response.getCategory().getCategoryId());
            assertEquals("Confectionery", response.getCategory().getCategoryName());
        }
    }

    // =========================================================================
    // 3. UPDATE PRODUCT (CẬP NHẬT SẢN PHẨM)
    // =========================================================================
    @Nested
    @DisplayName("Kiểm thử updateProduct()")
    class UpdateProductTests {

        @Test
        @DisplayName("Ném ngoại lệ khi ID sản phẩm hoặc request null")
        void testUpdateProduct_NullInputs_ThrowsException() {
            ProductRequest.Update validRequest = new ProductRequest.Update("Tên mới", "CAT-001", BigDecimal.TEN, "ON_SALE");

            assertThrows(IllegalArgumentException.class, () ->
                    productService.updateProduct(null, validRequest)
            );

            assertThrows(IllegalArgumentException.class, () ->
                    productService.updateProduct("PRD-001", null)
            );
        }

        @Test
        @DisplayName("Ném NotFoundException khi sản phẩm cần cập nhật không tồn tại")
        void testUpdateProduct_ProductNotFound_ThrowsException() {
            ProductRequest.Update request = new ProductRequest.Update("Tên mới", "CAT-001", BigDecimal.TEN, "ON_SALE");
            when(productRepository.findById("PRD-999")).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () ->
                    productService.updateProduct("PRD-999", request)
            );
        }

        @Test
        @DisplayName("Ném ngoại lệ khi tên sản phẩm được truyền nhưng bị chuỗi rỗng")
        void testUpdateProduct_EmptyName_ThrowsException() {
            Product existingProduct = new Product("PRD-001", "CAT-001", "Coca", 100, LocalDateTime.now(), "ON_SALE", BigDecimal.TEN);
            when(productRepository.findById("PRD-001")).thenReturn(Optional.of(existingProduct));

            ProductRequest.Update request = new ProductRequest.Update("   ", null, null, null);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    productService.updateProduct("PRD-001", request)
            );
            assertEquals("Tên sản phẩm không được rỗng.", ex.getMessage());
        }

        @Test
        @DisplayName("Ném ngoại lệ khi đơn giá cập nhật âm")
        void testUpdateProduct_NegativeUnitPrice_ThrowsException() {
            Product existingProduct = new Product("PRD-001", "CAT-001", "Coca", 100, LocalDateTime.now(), "ON_SALE", BigDecimal.TEN);
            when(productRepository.findById("PRD-001")).thenReturn(Optional.of(existingProduct));

            ProductRequest.Update request = new ProductRequest.Update(null, null, new BigDecimal("-1000.00"), null);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    productService.updateProduct("PRD-001", request)
            );
            assertEquals("Đơn giá sản phẩm không được âm.", ex.getMessage());
        }

        @Test
        @DisplayName("Ném ngoại lệ khi trạng thái productStatus không hợp lệ")
        void testUpdateProduct_InvalidStatus_ThrowsException() {
            Product existingProduct = new Product("PRD-001", "CAT-001", "Coca", 100, LocalDateTime.now(), "ON_SALE", BigDecimal.TEN);
            when(productRepository.findById("PRD-001")).thenReturn(Optional.of(existingProduct));

            ProductRequest.Update request = new ProductRequest.Update(null, null, null, "DELETED");

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    productService.updateProduct("PRD-001", request)
            );
            assertEquals("productStatus không hợp lệ. Chỉ chấp nhận ON_SALE hoặc DISCONTINUED.", ex.getMessage());
        }

        @Test
        @DisplayName("Ném NotFoundException khi cập nhật categoryId không tồn tại")
        void testUpdateProduct_CategoryNotFound_ThrowsException() {
            Product existingProduct = new Product("PRD-001", "CAT-001", "Coca", 100, LocalDateTime.now(), "ON_SALE", BigDecimal.TEN);
            when(productRepository.findById("PRD-001")).thenReturn(Optional.of(existingProduct));
            when(productRepository.existsCategoryById("CAT-999")).thenReturn(false);

            ProductRequest.Update request = new ProductRequest.Update(null, "CAT-999", null, null);

            NotFoundException ex = assertThrows(NotFoundException.class, () ->
                    productService.updateProduct("PRD-001", request)
            );
            assertEquals("Loại sản phẩm (categoryId) không tồn tại.", ex.getMessage());
        }

        @Test
        @DisplayName("Cập nhật sản phẩm thành công với đầy đủ các trường hợp lệ")
        void testUpdateProduct_Success() {
            // Arrange
            Product existingProduct = new Product(
                    "PRD-001",
                    "CAT-001",
                    "Coca Cola Cũ",
                    50,
                    LocalDateTime.of(2027, 12, 31, 0, 0),
                    "ON_SALE",
                    new BigDecimal("9000.00")
            );

            when(productRepository.findById("PRD-001")).thenReturn(Optional.of(existingProduct));
            when(productRepository.existsCategoryById("CAT-005")).thenReturn(true);
            when(productRepository.updateProduct(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Object[] categoryRow = new Object[]{ existingProduct, "Seasoning" };
            when(productRepository.findProductWithCategoryById("PRD-001")).thenReturn(Optional.of(categoryRow));

            ProductRequest.Update request = new ProductRequest.Update(
                    "Coca Cola Mới",
                    "CAT-005",
                    new BigDecimal("12000.00"),
                    "DISCONTINUED"
            );

            // Act
            ProductResponse.UpdateResponseDto result = productService.updateProduct("PRD-001", request);

            // Assert
            assertNotNull(result);
            assertEquals("PRD-001", result.getProductId());
            assertEquals("Coca Cola Mới", result.getProductName());
            assertEquals(new BigDecimal("12000.00"), result.getUnitPrice());
            assertEquals("DISCONTINUED", result.getProductStatus());
            assertEquals("CAT-005", result.getCategory().getCategoryId());
            assertEquals("Seasoning", result.getCategory().getCategoryName());

            ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
            verify(productRepository).updateProduct(captor.capture());
            Product updated = captor.getValue();
            assertEquals("Coca Cola Mới", updated.getProductName());
            assertEquals("CAT-005", updated.getCategoryId());
            assertEquals("DISCONTINUED", updated.getProductStatus());
        }
    }

    // =========================================================================
    // 4. LOW STOCK ALERTS (CẢNH BÁO TỒN KHO THẤP)
    // =========================================================================
    @Nested
    @DisplayName("Kiểm thử getLowStockAlerts() và countLowStockAlerts()")
    class LowStockTests {

        @Test
        @DisplayName("Ném ngoại lệ khi threshold < 0")
        void testGetLowStockAlerts_NegativeThreshold_ThrowsException() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    productService.getLowStockAlerts(-1, null, 1, 10)
            );
            assertEquals("Ngưỡng tồn kho (threshold) phải lớn hơn hoặc bằng 0.", ex.getMessage());
        }

        @Test
        @DisplayName("Lấy danh sách cảnh báo tồn kho thành công")
        void testGetLowStockAlerts_Success() {
            Product lowStockProd = new Product(
                    "PRD-013",
                    "CAT-007",
                    "Pork Belly 500g",
                    20,
                    LocalDateTime.of(2027, 10, 5, 0, 0),
                    "ON_SALE",
                    new BigDecimal("120000.00")
            );
            List<Object[]> mockRows = new ArrayList<>();
            mockRows.add(new Object[]{ lowStockProd, "Meat and Seafood" });

            when(productRepository.findLowStockProducts(25, "CAT-007", 1, 10))
                    .thenReturn(mockRows);

            List<ProductResponse.LowStockDto> result = productService.getLowStockAlerts(25, "CAT-007", 1, 10);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("PRD-013", result.get(0).getProductId());
            assertEquals("Pork Belly 500g", result.get(0).getProductName());
            assertEquals(20, result.get(0).getStockQuantity());
            assertEquals("CAT-007", result.get(0).getCategory().getCategoryId());
            assertEquals("Meat and Seafood", result.get(0).getCategory().getCategoryName());
        }

        @Test
        @DisplayName("Đếm số lượng cảnh báo tồn kho thành công")
        void testCountLowStockAlerts_Success() {
            when(productRepository.countLowStockProducts(10, null)).thenReturn(5L);

            long count = productService.countLowStockAlerts(10, null);

            assertEquals(5L, count);
            verify(productRepository).countLowStockProducts(10, null);
        }

        @Test
        @DisplayName("Ném ngoại lệ khi countLowStockAlerts có threshold âm")
        void testCountLowStockAlerts_NegativeThreshold_ThrowsException() {
            assertThrows(IllegalArgumentException.class, () ->
                    productService.countLowStockAlerts(-1, null)
            );
        }
    }
}
