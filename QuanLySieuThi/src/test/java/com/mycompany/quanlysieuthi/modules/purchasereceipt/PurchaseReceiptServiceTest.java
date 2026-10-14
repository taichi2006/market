package com.mycompany.quanlysieuthi.modules.purchasereceipt;

import com.mycompany.quanlysieuthi.entity.Product;
import com.mycompany.quanlysieuthi.entity.PurchaseReceipt;
import com.mycompany.quanlysieuthi.entity.Voucher;
import com.mycompany.quanlysieuthi.common.exception.AppException.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test cho PurchaseReceiptService")
class PurchaseReceiptServiceTest {

    @Mock
    private PurchaseReceiptRepository purchaseReceiptRepository;

    private PurchaseReceiptService purchaseReceiptService;

    @BeforeEach
    void setUp() {
        purchaseReceiptService = new PurchaseReceiptService(purchaseReceiptRepository);
    }

    // =========================================================================
    // 1. CREATE PURCHASE RECEIPT (TẠO PHIẾU NHẬP HÀNG)
    // =========================================================================
    @Nested
    @DisplayName("Kiểm thử createPurchaseReceipt()")
    class CreatePurchaseReceiptTests {

        @Test
        @DisplayName("Ném ngoại lệ khi request là null")
        void testCreatePurchaseReceipt_NullRequest_ThrowsIllegalArgumentException() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    purchaseReceiptService.createPurchaseReceipt(null, "EMP001", "Nguyễn Văn A")
            );
            assertEquals("Dữ liệu phiếu nhập hàng không được để trống", ex.getMessage());
        }

        @Test
        @DisplayName("Ném ngoại lệ khi supplierName bị null hoặc để trống")
        void testCreatePurchaseReceipt_BlankSupplierName_ThrowsIllegalArgumentException() {
            PurchaseReceiptRequest request = new PurchaseReceiptRequest("   ", List.of(new PurchaseReceiptRequest.Item()));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    purchaseReceiptService.createPurchaseReceipt(request, "EMP001", "Nguyễn Văn A")
            );
            assertEquals("Tên nhà cung cấp (supplierName) không được để trống", ex.getMessage());
        }

        @Test
        @DisplayName("Ném ngoại lệ khi danh sách items là null hoặc rỗng")
        void testCreatePurchaseReceipt_EmptyItems_ThrowsIllegalArgumentException() {
            PurchaseReceiptRequest request = new PurchaseReceiptRequest("Acecook VN", Collections.emptyList());

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    purchaseReceiptService.createPurchaseReceipt(request, "EMP001", "Nguyễn Văn A")
            );
            assertEquals("Danh sách mặt hàng nhập kho (items) không được để trống", ex.getMessage());
        }

        @Test
        @DisplayName("Ném ngoại lệ khi tên sản phẩm bị để trống")
        void testCreatePurchaseReceipt_BlankProductName_ThrowsIllegalArgumentException() {
            PurchaseReceiptRequest.Item item = new PurchaseReceiptRequest.Item("PROD001", "  ", "CAT01",
                    new BigDecimal("5000"), 10, new BigDecimal("4000"), "2027-12-31");
            PurchaseReceiptRequest request = new PurchaseReceiptRequest("Acecook VN", List.of(item));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    purchaseReceiptService.createPurchaseReceipt(request, "EMP001", "Nguyễn Văn A")
            );
            assertEquals("Tên sản phẩm không được để trống", ex.getMessage());
        }

        @Test
        @DisplayName("Ném ngoại lệ khi categoryId bị để trống")
        void testCreatePurchaseReceipt_BlankCategoryId_ThrowsIllegalArgumentException() {
            PurchaseReceiptRequest.Item item = new PurchaseReceiptRequest.Item("PROD001", "Mì Hảo Hảo", "  ",
                    new BigDecimal("5000"), 10, new BigDecimal("4000"), "2027-12-31");
            PurchaseReceiptRequest request = new PurchaseReceiptRequest("Acecook VN", List.of(item));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    purchaseReceiptService.createPurchaseReceipt(request, "EMP001", "Nguyễn Văn A")
            );
            assertEquals("Mã ngành hàng (categoryId) không được để trống", ex.getMessage());
        }

        @Test
        @DisplayName("Ném ngoại lệ NotFoundException khi categoryId không tồn tại trong hệ thống")
        void testCreatePurchaseReceipt_CategoryNotFound_ThrowsNotFoundException() {
            PurchaseReceiptRequest.Item item = new PurchaseReceiptRequest.Item("PROD001", "Mì Hảo Hảo", "CAT_UNKNOWN",
                    new BigDecimal("5000"), 10, new BigDecimal("4000"), "2027-12-31");
            PurchaseReceiptRequest request = new PurchaseReceiptRequest("Acecook VN", List.of(item));

            when(purchaseReceiptRepository.existsCategory("CAT_UNKNOWN")).thenReturn(false);

            NotFoundException ex = assertThrows(NotFoundException.class, () ->
                    purchaseReceiptService.createPurchaseReceipt(request, "EMP001", "Nguyễn Văn A")
            );
            assertTrue(ex.getMessage().contains("Mã ngành hàng không tồn tại: CAT_UNKNOWN"));
        }

        @Test
        @DisplayName("Ném ngoại lệ khi unitPrice (giá bán) < 0")
        void testCreatePurchaseReceipt_NegativeUnitPrice_ThrowsIllegalArgumentException() {
            PurchaseReceiptRequest.Item item = new PurchaseReceiptRequest.Item("PROD001", "Mì Hảo Hảo", "CAT01",
                    new BigDecimal("-100"), 10, new BigDecimal("4000"), "2027-12-31");
            PurchaseReceiptRequest request = new PurchaseReceiptRequest("Acecook VN", List.of(item));

            when(purchaseReceiptRepository.existsCategory("CAT01")).thenReturn(true);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    purchaseReceiptService.createPurchaseReceipt(request, "EMP001", "Nguyễn Văn A")
            );
            assertEquals("Giá bán lẻ (unitPrice) phải >= 0", ex.getMessage());
        }

        @Test
        @DisplayName("Ném ngoại lệ khi số lượng nhập (purchasedQuantity) <= 0")
        void testCreatePurchaseReceipt_InvalidQuantity_ThrowsIllegalArgumentException() {
            PurchaseReceiptRequest.Item item = new PurchaseReceiptRequest.Item("PROD001", "Mì Hảo Hảo", "CAT01",
                    new BigDecimal("5000"), 0, new BigDecimal("4000"), "2027-12-31");
            PurchaseReceiptRequest request = new PurchaseReceiptRequest("Acecook VN", List.of(item));

            when(purchaseReceiptRepository.existsCategory("CAT01")).thenReturn(true);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    purchaseReceiptService.createPurchaseReceipt(request, "EMP001", "Nguyễn Văn A")
            );
            assertEquals("Số lượng nhập (purchasedQuantity) phải là số nguyên > 0", ex.getMessage());
        }

        @Test
        @DisplayName("Ném ngoại lệ khi giá nhập (purchasePrice) <= 0")
        void testCreatePurchaseReceipt_InvalidPurchasePrice_ThrowsIllegalArgumentException() {
            PurchaseReceiptRequest.Item item = new PurchaseReceiptRequest.Item("PROD001", "Mì Hảo Hảo", "CAT01",
                    new BigDecimal("5000"), 10, BigDecimal.ZERO, "2027-12-31");
            PurchaseReceiptRequest request = new PurchaseReceiptRequest("Acecook VN", List.of(item));

            when(purchaseReceiptRepository.existsCategory("CAT01")).thenReturn(true);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    purchaseReceiptService.createPurchaseReceipt(request, "EMP001", "Nguyễn Văn A")
            );
            assertEquals("Giá nhập (purchasePrice) phải > 0", ex.getMessage());
        }

        @Test
        @DisplayName("Ném ngoại lệ khi hạn sử dụng bị để trống")
        void testCreatePurchaseReceipt_BlankExpirationDate_ThrowsIllegalArgumentException() {
            PurchaseReceiptRequest.Item item = new PurchaseReceiptRequest.Item("PROD001", "Mì Hảo Hảo", "CAT01",
                    new BigDecimal("5000"), 10, new BigDecimal("4000"), "   ");
            PurchaseReceiptRequest request = new PurchaseReceiptRequest("Acecook VN", List.of(item));

            when(purchaseReceiptRepository.existsCategory("CAT01")).thenReturn(true);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    purchaseReceiptService.createPurchaseReceipt(request, "EMP001", "Nguyễn Văn A")
            );
            assertEquals("Hạn sử dụng (expirationDate) không được để trống", ex.getMessage());
        }

        @Test
        @DisplayName("Ném ngoại lệ khi hạn sử dụng sai định dạng ngày tháng")
        void testCreatePurchaseReceipt_InvalidDateFormat_ThrowsIllegalArgumentException() {
            PurchaseReceiptRequest.Item item = new PurchaseReceiptRequest.Item("PROD001", "Mì Hảo Hảo", "CAT01",
                    new BigDecimal("5000"), 10, new BigDecimal("4000"), "31/12/2027");
            PurchaseReceiptRequest request = new PurchaseReceiptRequest("Acecook VN", List.of(item));

            when(purchaseReceiptRepository.existsCategory("CAT01")).thenReturn(true);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    purchaseReceiptService.createPurchaseReceipt(request, "EMP001", "Nguyễn Văn A")
            );
            assertEquals("Hạn sử dụng không đúng định dạng (YYYY-MM-DD hoặc ISO-8601)", ex.getMessage());
        }

        @Test
        @DisplayName("Ném ngoại lệ khi hạn sử dụng nằm trong quá khứ")
        void testCreatePurchaseReceipt_PastExpirationDate_ThrowsIllegalArgumentException() {
            PurchaseReceiptRequest.Item item = new PurchaseReceiptRequest.Item("PROD001", "Mì Hảo Hảo", "CAT01",
                    new BigDecimal("5000"), 10, new BigDecimal("4000"), "2020-01-01");
            PurchaseReceiptRequest request = new PurchaseReceiptRequest("Acecook VN", List.of(item));

            when(purchaseReceiptRepository.existsCategory("CAT01")).thenReturn(true);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    purchaseReceiptService.createPurchaseReceipt(request, "EMP001", "Nguyễn Văn A")
            );
            assertEquals("Hạn sử dụng phải lớn hơn thời điểm hiện tại", ex.getMessage());
        }

        @Test
        @DisplayName("Tạo phiếu nhập hàng thành công: tính đúng tổng tiền và lưu dữ liệu")
        void testCreatePurchaseReceipt_Success() {
            String futureExpDate = LocalDate.now().plusYears(1).toString(); // YYYY-MM-DD
            PurchaseReceiptRequest.Item item1 = new PurchaseReceiptRequest.Item("PROD001", "Mì Hảo Hảo Sa Tế", "CAT01",
                    new BigDecimal("5000"), 100, new BigDecimal("3500"), futureExpDate); // 100 * 3500 = 350,000
            PurchaseReceiptRequest.Item item2 = new PurchaseReceiptRequest.Item(null, "Mì Đệ Nhất", "CAT01",
                    new BigDecimal("7000"), 50, new BigDecimal("5000"), futureExpDate); // 50 * 5000 = 250,000 -> Total: 600,000

            PurchaseReceiptRequest request = new PurchaseReceiptRequest("Acecook VN", List.of(item1, item2));

            when(purchaseReceiptRepository.existsCategory("CAT01")).thenReturn(true);

            PurchaseReceiptResponse response = purchaseReceiptService.createPurchaseReceipt(request, "EMP001", "Nguyễn Văn A");

            assertNotNull(response);
            assertNotNull(response.getVoucherId());
            assertEquals("Acecook VN", response.getSupplierName());
            assertEquals(new BigDecimal("600000"), response.getTotalAmount());
            assertEquals("EMP001", response.getCreatedBy().getEmployeeId());
            assertEquals("Nguyễn Văn A", response.getCreatedBy().getFullName());
            assertEquals(2, response.getCreatedProducts().size());

            // Kiểm tra item không truyền productId thì hệ thống tự sinh mã
            assertNotNull(response.getCreatedProducts().get(1).getProductId());

            verify(purchaseReceiptRepository, times(1)).savePurchaseReceiptTransaction(
                    any(Voucher.class), any(PurchaseReceipt.class), anyList(), anyList()
            );
        }
    }

    // =========================================================================
    // 2. GET PURCHASE RECEIPTS (DANH SÁCH PHIẾU NHẬP)
    // =========================================================================
    @Nested
    @DisplayName("Kiểm thử getPurchaseReceipts()")
    class GetPurchaseReceiptsTests {

        @Test
        @DisplayName("Ném ngoại lệ khi page <= 0")
        void testGetPurchaseReceipts_InvalidPage_ThrowsIllegalArgumentException() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    purchaseReceiptService.getPurchaseReceipts(0, 10, null, null, null, null)
            );
            assertEquals("Tham số page phải lớn hơn 0", ex.getMessage());
        }

        @Test
        @DisplayName("Ném ngoại lệ khi limit <= 0")
        void testGetPurchaseReceipts_InvalidLimit_ThrowsIllegalArgumentException() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    purchaseReceiptService.getPurchaseReceipts(1, -5, null, null, null, null)
            );
            assertEquals("Tham số limit phải lớn hơn 0", ex.getMessage());
        }

        @Test
        @DisplayName("Ném ngoại lệ khi fromDate không đúng định dạng YYYY-MM-DD")
        void testGetPurchaseReceipts_InvalidFromDate_ThrowsIllegalArgumentException() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    purchaseReceiptService.getPurchaseReceipts(1, 10, null, null, "2026/10/10", null)
            );
            assertEquals("Định dạng ngày tháng fromDate không hợp lệ (yêu cầu YYYY-MM-DD)", ex.getMessage());
        }

        @Test
        @DisplayName("Ném ngoại lệ khi toDate không đúng định dạng YYYY-MM-DD")
        void testGetPurchaseReceipts_InvalidToDate_ThrowsIllegalArgumentException() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    purchaseReceiptService.getPurchaseReceipts(1, 10, null, null, null, "10-10-2026")
            );
            assertEquals("Định dạng ngày tháng toDate không hợp lệ (yêu cầu YYYY-MM-DD)", ex.getMessage());
        }

        @Test
        @DisplayName("Ném ngoại lệ khi fromDate sau toDate")
        void testGetPurchaseReceipts_FromDateAfterToDate_ThrowsIllegalArgumentException() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    purchaseReceiptService.getPurchaseReceipts(1, 10, null, null, "2026-10-20", "2026-10-10")
            );
            assertEquals("fromDate không được sau toDate", ex.getMessage());
        }

        @Test
        @DisplayName("Lấy danh sách phiếu nhập thành công")
        void testGetPurchaseReceipts_Success() {
            List<PurchaseReceiptResponse.ListItemDto> mockList = List.of(
                    new PurchaseReceiptResponse.ListItemDto("VOU001", "Acecook VN", new BigDecimal("5000000"),
                            "2026-10-14T10:00:00.000Z", new PurchaseReceiptResponse.EmployeeDto("EMP001", "Nguyễn Văn A"))
            );

            when(purchaseReceiptRepository.findPurchaseReceipts(eq(1), eq(10), eq("Acecook"), isNull(), isNull(), isNull()))
                    .thenReturn(mockList);

            List<PurchaseReceiptResponse.ListItemDto> result = purchaseReceiptService.getPurchaseReceipts(
                    1, 10, "Acecook", null, null, null
            );

            assertEquals(1, result.size());
            assertEquals("VOU001", result.get(0).getVoucherId());
            assertEquals("Acecook VN", result.get(0).getSupplierName());
        }
    }

    // =========================================================================
    // 3. GET PURCHASE RECEIPT DETAIL (CHI TIẾT PHIẾU NHẬP)
    // =========================================================================
    @Nested
    @DisplayName("Kiểm thử getPurchaseReceiptDetail()")
    class GetPurchaseReceiptDetailTests {

        @Test
        @DisplayName("Ném ngoại lệ khi voucherId để trống")
        void testGetPurchaseReceiptDetail_BlankId_ThrowsIllegalArgumentException() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    purchaseReceiptService.getPurchaseReceiptDetail("   ")
            );
            assertEquals("Mã phiếu nhập hàng không được để trống", ex.getMessage());
        }

        @Test
        @DisplayName("Ném ngoại lệ NotFoundException khi voucherId không tồn tại")
        void testGetPurchaseReceiptDetail_NotFound_ThrowsNotFoundException() {
            when(purchaseReceiptRepository.findPurchaseReceiptDetailById("VOU_UNKNOWN")).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(NotFoundException.class, () ->
                    purchaseReceiptService.getPurchaseReceiptDetail("VOU_UNKNOWN")
            );
            assertEquals("Không tìm thấy phiếu nhập hàng với ID tương ứng", ex.getMessage());
        }

        @Test
        @DisplayName("Lấy chi tiết phiếu nhập thành công")
        void testGetPurchaseReceiptDetail_Success() {
            PurchaseReceiptResponse.DetailResponse mockDetail = new PurchaseReceiptResponse.DetailResponse(
                    "VOU001", "Acecook VN", new BigDecimal("5000000"), "2026-10-14T10:00:00.000Z",
                    new PurchaseReceiptResponse.EmployeeDto("EMP001", "Nguyễn Văn A"), Collections.emptyList()
            );

            when(purchaseReceiptRepository.findPurchaseReceiptDetailById("VOU001")).thenReturn(Optional.of(mockDetail));

            PurchaseReceiptResponse.DetailResponse response = purchaseReceiptService.getPurchaseReceiptDetail("VOU001");

            assertNotNull(response);
            assertEquals("VOU001", response.getVoucherId());
            assertEquals("Acecook VN", response.getSupplierName());
        }
    }
}
