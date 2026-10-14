package com.mycompany.quanlysieuthi.modules.returnreceipt;

import com.mycompany.quanlysieuthi.entity.Customer;
import com.mycompany.quanlysieuthi.entity.Invoice;
import com.mycompany.quanlysieuthi.entity.ReturnReceipt;
import com.mycompany.quanlysieuthi.common.exception.AppException.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test cho ReturnReceiptService")
class ReturnReceiptServiceTest {

    @Mock
    private ReturnReceiptRepository returnReceiptRepository;

    private ReturnReceiptService returnReceiptService;

    @BeforeEach
    void setUp() {
        returnReceiptService = new ReturnReceiptService(returnReceiptRepository);
    }

    // =========================================================================
    // 1. CREATE RETURN RECEIPT (TẠO PHIẾU TRẢ HÀNG)
    // =========================================================================
    @Nested
    @DisplayName("Kiểm thử createReturnReceipt()")
    class CreateReturnReceiptTests {

        @Test
        @DisplayName("Ném ngoại lệ khi request là null")
        void testCreateReturnReceipt_NullRequest_ThrowsIllegalArgumentException() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    returnReceiptService.createReturnReceipt(null, "EMP001", "Nguyễn Văn A")
            );
            assertEquals("Dữ liệu phiếu trả hàng không được để trống", ex.getMessage());
        }

        @Test
        @DisplayName("Ném ngoại lệ khi invoiceId bị để trống")
        void testCreateReturnReceipt_BlankInvoiceId_ThrowsIllegalArgumentException() {
            ReturnReceiptRequest.Create request = new ReturnReceiptRequest.Create("  ", "Hàng lỗi", List.of(new ReturnReceiptRequest.Item("PROD01", 1)));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    returnReceiptService.createReturnReceipt(request, "EMP001", "Nguyễn Văn A")
            );
            assertEquals("Mã hóa đơn gốc (invoiceId) không được để trống", ex.getMessage());
        }

        @Test
        @DisplayName("Ném ngoại lệ khi danh sách items là null hoặc rỗng")
        void testCreateReturnReceipt_EmptyItems_ThrowsIllegalArgumentException() {
            ReturnReceiptRequest.Create request = new ReturnReceiptRequest.Create("INV001", "Hàng lỗi", Collections.emptyList());

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    returnReceiptService.createReturnReceipt(request, "EMP001", "Nguyễn Văn A")
            );
            assertEquals("Danh sách mặt hàng trả lại (items) không được để trống", ex.getMessage());
        }

        @Test
        @DisplayName("Ném ngoại lệ NotFoundException khi hóa đơn không tồn tại")
        void testCreateReturnReceipt_InvoiceNotFound_ThrowsNotFoundException() {
            ReturnReceiptRequest.Create request = new ReturnReceiptRequest.Create("INV_UNKNOWN", "Hàng lỗi",
                    List.of(new ReturnReceiptRequest.Item("PROD01", 1)));

            when(returnReceiptRepository.findInvoiceById("INV_UNKNOWN")).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(NotFoundException.class, () ->
                    returnReceiptService.createReturnReceipt(request, "EMP001", "Nguyễn Văn A")
            );
            assertTrue(ex.getMessage().contains("Hóa đơn không tồn tại trong hệ thống: INV_UNKNOWN"));
        }

        @Test
        @DisplayName("Ném ngoại lệ khi hóa đơn chưa thanh toán (khác PAID)")
        void testCreateReturnReceipt_InvoiceNotPaid_ThrowsIllegalArgumentException() {
            Invoice invoice = new Invoice("INV001", "CUST01", "EMP01", "PENDING", new BigDecimal("100000"), BigDecimal.ZERO, LocalDateTime.now());

            ReturnReceiptRequest.Create request = new ReturnReceiptRequest.Create("INV001", "Hàng lỗi",
                    List.of(new ReturnReceiptRequest.Item("PROD01", 1)));

            when(returnReceiptRepository.findInvoiceById("INV001")).thenReturn(Optional.of(invoice));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    returnReceiptService.createReturnReceipt(request, "EMP001", "Nguyễn Văn A")
            );
            assertTrue(ex.getMessage().contains("Chỉ được phép trả hàng cho hóa đơn đã hoàn tất thanh toán (PAID)"));
        }

        @Test
        @DisplayName("Ném ngoại lệ ConflictException khi hóa đơn đã được lập phiếu trả hàng trước đó")
        void testCreateReturnReceipt_AlreadyReturned_ThrowsConflictException() {
            Invoice invoice = new Invoice("INV001", "CUST01", "EMP01", "PAID", new BigDecimal("100000"), BigDecimal.ZERO, LocalDateTime.now());

            ReturnReceiptRequest.Create request = new ReturnReceiptRequest.Create("INV001", "Hàng lỗi",
                    List.of(new ReturnReceiptRequest.Item("PROD01", 1)));

            when(returnReceiptRepository.findInvoiceById("INV001")).thenReturn(Optional.of(invoice));
            when(returnReceiptRepository.existsReturnReceiptByInvoiceId("INV001")).thenReturn(true);

            ConflictException ex = assertThrows(ConflictException.class, () ->
                    returnReceiptService.createReturnReceipt(request, "EMP001", "Nguyễn Văn A")
            );
            assertEquals("Hóa đơn này đã được lập phiếu trả hàng trước đó", ex.getMessage());
        }

        @Test
        @DisplayName("Ném ngoại lệ NotFoundException khi hóa đơn không có chi tiết mặt hàng hợp lệ")
        void testCreateReturnReceipt_NoInvoiceItems_ThrowsNotFoundException() {
            Invoice invoice = new Invoice("INV001", "CUST01", "EMP01", "PAID", new BigDecimal("100000"), BigDecimal.ZERO, LocalDateTime.now());

            ReturnReceiptRequest.Create request = new ReturnReceiptRequest.Create("INV001", "Hàng lỗi",
                    List.of(new ReturnReceiptRequest.Item("PROD01", 1)));

            when(returnReceiptRepository.findInvoiceById("INV001")).thenReturn(Optional.of(invoice));
            when(returnReceiptRepository.existsReturnReceiptByInvoiceId("INV001")).thenReturn(false);
            when(returnReceiptRepository.findInvoiceItems("INV001")).thenReturn(Collections.emptyList());

            NotFoundException ex = assertThrows(NotFoundException.class, () ->
                    returnReceiptService.createReturnReceipt(request, "EMP001", "Nguyễn Văn A")
            );
            assertEquals("Hóa đơn này không có danh sách mặt hàng hợp lệ", ex.getMessage());
        }

        @Test
        @DisplayName("Ném ngoại lệ khi productId của item trả hàng bị để trống")
        void testCreateReturnReceipt_BlankProductId_ThrowsIllegalArgumentException() {
            Invoice invoice = new Invoice("INV001", "CUST01", "EMP01", "PAID", new BigDecimal("100000"), BigDecimal.ZERO, LocalDateTime.now());
            Object[] rawItem = new Object[]{"PROD01", 2, new BigDecimal("50000")};

            ReturnReceiptRequest.Create request = new ReturnReceiptRequest.Create("INV001", "Hàng lỗi",
                    List.of(new ReturnReceiptRequest.Item("   ", 1)));

            when(returnReceiptRepository.findInvoiceById("INV001")).thenReturn(Optional.of(invoice));
            when(returnReceiptRepository.existsReturnReceiptByInvoiceId("INV001")).thenReturn(false);
            when(returnReceiptRepository.findInvoiceItems("INV001")).thenReturn(Collections.singletonList(rawItem));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    returnReceiptService.createReturnReceipt(request, "EMP001", "Nguyễn Văn A")
            );
            assertEquals("Mã sản phẩm không được để trống", ex.getMessage());
        }

        @Test
        @DisplayName("Ném ngoại lệ khi returnedQuantity <= 0")
        void testCreateReturnReceipt_InvalidReturnedQuantity_ThrowsIllegalArgumentException() {
            Invoice invoice = new Invoice("INV001", "CUST01", "EMP01", "PAID", new BigDecimal("100000"), BigDecimal.ZERO, LocalDateTime.now());
            Object[] rawItem = new Object[]{"PROD01", 2, new BigDecimal("50000")};

            ReturnReceiptRequest.Create request = new ReturnReceiptRequest.Create("INV001", "Hàng lỗi",
                    List.of(new ReturnReceiptRequest.Item("PROD01", 0)));

            when(returnReceiptRepository.findInvoiceById("INV001")).thenReturn(Optional.of(invoice));
            when(returnReceiptRepository.existsReturnReceiptByInvoiceId("INV001")).thenReturn(false);
            when(returnReceiptRepository.findInvoiceItems("INV001")).thenReturn(Collections.singletonList(rawItem));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    returnReceiptService.createReturnReceipt(request, "EMP001", "Nguyễn Văn A")
            );
            assertEquals("Số lượng trả lại phải là số nguyên > 0", ex.getMessage());
        }

        @Test
        @DisplayName("Ném ngoại lệ khi mặt hàng trả lại không nằm trong hóa đơn")
        void testCreateReturnReceipt_ItemNotInInvoice_ThrowsIllegalArgumentException() {
            Invoice invoice = new Invoice("INV001", "CUST01", "EMP01", "PAID", new BigDecimal("100000"), BigDecimal.ZERO, LocalDateTime.now());
            Object[] rawItem = new Object[]{"PROD01", 2, new BigDecimal("50000")};

            ReturnReceiptRequest.Create request = new ReturnReceiptRequest.Create("INV001", "Hàng lỗi",
                    List.of(new ReturnReceiptRequest.Item("PROD02", 1)));

            when(returnReceiptRepository.findInvoiceById("INV001")).thenReturn(Optional.of(invoice));
            when(returnReceiptRepository.existsReturnReceiptByInvoiceId("INV001")).thenReturn(false);
            when(returnReceiptRepository.findInvoiceItems("INV001")).thenReturn(Collections.singletonList(rawItem));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    returnReceiptService.createReturnReceipt(request, "EMP001", "Nguyễn Văn A")
            );
            assertTrue(ex.getMessage().contains("không tồn tại trong hóa đơn INV001"));
        }

        @Test
        @DisplayName("Ném ngoại lệ khi số lượng trả lại vượt quá số lượng đã mua")
        void testCreateReturnReceipt_ReturnQtyExceedsPurchased_ThrowsIllegalArgumentException() {
            Invoice invoice = new Invoice("INV001", "CUST01", "EMP01", "PAID", new BigDecimal("100000"), BigDecimal.ZERO, LocalDateTime.now());
            Object[] rawItem = new Object[]{"PROD01", 2, new BigDecimal("50000")};

            ReturnReceiptRequest.Create request = new ReturnReceiptRequest.Create("INV001", "Hàng lỗi",
                    List.of(new ReturnReceiptRequest.Item("PROD01", 5))); // mua 2 đòi trả 5

            when(returnReceiptRepository.findInvoiceById("INV001")).thenReturn(Optional.of(invoice));
            when(returnReceiptRepository.existsReturnReceiptByInvoiceId("INV001")).thenReturn(false);
            when(returnReceiptRepository.findInvoiceItems("INV001")).thenReturn(Collections.singletonList(rawItem));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    returnReceiptService.createReturnReceipt(request, "EMP001", "Nguyễn Văn A")
            );
            assertTrue(ex.getMessage().contains("vượt quá số lượng đã mua (2)"));
        }

        @Test
        @DisplayName("Tạo phiếu trả hàng thành công cho khách vãng lai (không có customerId)")
        void testCreateReturnReceipt_GuestCustomer_Success() {
            Invoice invoice = new Invoice("INV001", null, "EMP01", "PAID", new BigDecimal("100000"), BigDecimal.ZERO, LocalDateTime.now());
            Object[] rawItem1 = new Object[]{"PROD01", 2, new BigDecimal("30000")};
            Object[] rawItem2 = new Object[]{"PROD02", 1, new BigDecimal("40000")};

            ReturnReceiptRequest.Create request = new ReturnReceiptRequest.Create("INV001", "Sản phẩm bị rách bao bì",
                    List.of(new ReturnReceiptRequest.Item("PROD01", 1))); // trả 1 sp PROD01 -> hoàn 30,000

            when(returnReceiptRepository.findInvoiceById("INV001")).thenReturn(Optional.of(invoice));
            when(returnReceiptRepository.existsReturnReceiptByInvoiceId("INV001")).thenReturn(false);
            when(returnReceiptRepository.findInvoiceItems("INV001")).thenReturn(List.<Object[]>of(rawItem1, rawItem2));

            ReturnReceiptResponse response = returnReceiptService.createReturnReceipt(request, "EMP001", "Nguyễn Văn A");

            assertNotNull(response);
            assertNotNull(response.getReturnReceiptId());
            assertEquals("INV001", response.getInvoiceId());
            assertEquals("Sản phẩm bị rách bao bì", response.getReason());
            assertEquals(new BigDecimal("30000"), response.getRefundAmount());
            assertEquals("EMP001", response.getCreatedBy().getEmployeeId());

            verify(returnReceiptRepository, times(1)).saveReturnReceiptTransaction(
                    any(ReturnReceipt.class), anyMap(), isNull(), eq(0)
            );
        }

        @Test
        @DisplayName("Tạo phiếu trả hàng thành công cho hội viên: khấu trừ điểm thưởng tương ứng")
        void testCreateReturnReceipt_MemberCustomer_DeductsLoyaltyPoints_Success() {
            Invoice invoice = new Invoice("INV001", "CUST01", "EMP01", "PAID", new BigDecimal("100000"), BigDecimal.ZERO, LocalDateTime.now());
            Customer customer = new Customer("CUST01", "Trần Văn B", "0909999888", "b@gmail.com", 2000, LocalDateTime.now());

            Object[] rawItem = new Object[]{"PROD01", 3, new BigDecimal("50000")};

            // Trả 2 sản phẩm: hoàn 100,000 VND -> Trừ floor(100000 / 10000) * 100 = 1000 điểm
            ReturnReceiptRequest.Create request = new ReturnReceiptRequest.Create("INV001", "Hết nhu cầu",
                    List.of(new ReturnReceiptRequest.Item("PROD01", 2)));

            when(returnReceiptRepository.findInvoiceById("INV001")).thenReturn(Optional.of(invoice));
            when(returnReceiptRepository.existsReturnReceiptByInvoiceId("INV001")).thenReturn(false);
            when(returnReceiptRepository.findInvoiceItems("INV001")).thenReturn(Collections.singletonList(rawItem));
            when(returnReceiptRepository.findCustomerById("CUST01")).thenReturn(Optional.of(customer));

            ReturnReceiptResponse response = returnReceiptService.createReturnReceipt(request, "EMP001", "Nguyễn Văn A");

            assertNotNull(response);
            assertEquals(new BigDecimal("100000"), response.getRefundAmount());

            // Điểm trừ: 1000 điểm (nhỏ hơn 2000 điểm hiện có)
            verify(returnReceiptRepository, times(1)).saveReturnReceiptTransaction(
                    any(ReturnReceipt.class), anyMap(), eq("CUST01"), eq(1000)
            );
        }

        @Test
        @DisplayName("Số điểm cần trừ lớn hơn điểm hiện có của khách: chỉ trừ tối đa bằng điểm hiện có")
        void testCreateReturnReceipt_DeductPointsCappedAtCurrentPoints() {
            Invoice invoice = new Invoice("INV001", "CUST01", "EMP01", "PAID", new BigDecimal("500000"), BigDecimal.ZERO, LocalDateTime.now());
            // Khách chỉ còn 300 điểm
            Customer customer = new Customer("CUST01", "Trần Văn B", "0909999888", "b@gmail.com", 300, LocalDateTime.now());

            Object[] rawItem = new Object[]{"PROD01", 1, new BigDecimal("200000")}; // Trả 200,000 -> tính ra trừ 2,000 điểm

            ReturnReceiptRequest.Create request = new ReturnReceiptRequest.Create("INV001", "Hỏng hóc",
                    List.of(new ReturnReceiptRequest.Item("PROD01", 1)));

            when(returnReceiptRepository.findInvoiceById("INV001")).thenReturn(Optional.of(invoice));
            when(returnReceiptRepository.existsReturnReceiptByInvoiceId("INV001")).thenReturn(false);
            when(returnReceiptRepository.findInvoiceItems("INV001")).thenReturn(Collections.singletonList(rawItem));
            when(returnReceiptRepository.findCustomerById("CUST01")).thenReturn(Optional.of(customer));

            ReturnReceiptResponse response = returnReceiptService.createReturnReceipt(request, "EMP001", "Nguyễn Văn A");

            assertNotNull(response);
            // 2000 > 300 -> điểm trừ bị chặn ở mức 300
            verify(returnReceiptRepository, times(1)).saveReturnReceiptTransaction(
                    any(ReturnReceipt.class), anyMap(), eq("CUST01"), eq(300)
            );
        }
    }

    // =========================================================================
    // 2. GET RETURN RECEIPTS (DANH SÁCH PHIẾU TRẢ HÀNG)
    // =========================================================================
    @Nested
    @DisplayName("Kiểm thử getReturnReceipts()")
    class GetReturnReceiptsTests {

        @Test
        @DisplayName("Ném ngoại lệ khi page <= 0")
        void testGetReturnReceipts_InvalidPage_ThrowsIllegalArgumentException() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    returnReceiptService.getReturnReceipts(0, 10, null, null, null, null)
            );
            assertEquals("Tham số page phải lớn hơn 0", ex.getMessage());
        }

        @Test
        @DisplayName("Ném ngoại lệ khi limit <= 0")
        void testGetReturnReceipts_InvalidLimit_ThrowsIllegalArgumentException() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    returnReceiptService.getReturnReceipts(1, -1, null, null, null, null)
            );
            assertEquals("Tham số limit phải lớn hơn 0", ex.getMessage());
        }

        @Test
        @DisplayName("Ném ngoại lệ khi fromDate sai định dạng YYYY-MM-DD")
        void testGetReturnReceipts_InvalidFromDate_ThrowsIllegalArgumentException() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    returnReceiptService.getReturnReceipts(1, 10, null, null, "2026/10/01", null)
            );
            assertEquals("Định dạng ngày tháng fromDate không hợp lệ (yêu cầu YYYY-MM-DD)", ex.getMessage());
        }

        @Test
        @DisplayName("Ném ngoại lệ khi toDate sai định dạng YYYY-MM-DD")
        void testGetReturnReceipts_InvalidToDate_ThrowsIllegalArgumentException() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    returnReceiptService.getReturnReceipts(1, 10, null, null, null, "31-10-2026")
            );
            assertEquals("Định dạng ngày tháng toDate không hợp lệ (yêu cầu YYYY-MM-DD)", ex.getMessage());
        }

        @Test
        @DisplayName("Ném ngoại lệ khi fromDate sau toDate")
        void testGetReturnReceipts_FromDateAfterToDate_ThrowsIllegalArgumentException() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    returnReceiptService.getReturnReceipts(1, 10, null, null, "2026-10-31", "2026-10-01")
            );
            assertEquals("fromDate không được sau toDate", ex.getMessage());
        }

        @Test
        @DisplayName("Lấy danh sách phiếu trả hàng thành công")
        void testGetReturnReceipts_Success() {
            Object[] row = new Object[]{
                    "RET001", "INV001", new BigDecimal("50000"), "Hàng lỗi",
                    LocalDateTime.now(), "EMP01", "Nguyễn Văn A"
            };

            when(returnReceiptRepository.findReturnReceipts(eq(1), eq(10), isNull(), isNull(), isNull(), isNull()))
                    .thenReturn(Collections.singletonList(row));

            List<ReturnReceiptResponse.ListItemDto> result = returnReceiptService.getReturnReceipts(
                    1, 10, null, null, null, null
            );

            assertEquals(1, result.size());
            assertEquals("RET001", result.get(0).getReturnReceiptId());
            assertEquals("INV001", result.get(0).getInvoiceId());
            assertEquals(new BigDecimal("50000"), result.get(0).getRefundAmount());
            assertEquals("Nguyễn Văn A", result.get(0).getCreatedBy().getFullName());
        }
    }

    // =========================================================================
    // 3. GET RETURN RECEIPT DETAIL (CHI TIẾT PHIẾU TRẢ HÀNG)
    // =========================================================================
    @Nested
    @DisplayName("Kiểm thử getReturnReceiptDetail()")
    class GetReturnReceiptDetailTests {

        @Test
        @DisplayName("Ném ngoại lệ khi id để trống")
        void testGetReturnReceiptDetail_BlankId_ThrowsIllegalArgumentException() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    returnReceiptService.getReturnReceiptDetail("   ")
            );
            assertEquals("Mã phiếu trả hàng không được để trống", ex.getMessage());
        }

        @Test
        @DisplayName("Ném ngoại lệ NotFoundException khi không tìm thấy phiếu trả hàng")
        void testGetReturnReceiptDetail_NotFound_ThrowsNotFoundException() {
            when(returnReceiptRepository.findReturnReceiptHeaderById("RET_UNKNOWN")).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(NotFoundException.class, () ->
                    returnReceiptService.getReturnReceiptDetail("RET_UNKNOWN")
            );
            assertTrue(ex.getMessage().contains("Không tìm thấy phiếu trả hàng với ID tương ứng: RET_UNKNOWN"));
        }

        @Test
        @DisplayName("Lấy chi tiết phiếu trả hàng thành công đầy đủ thông tin nhân viên và khách hàng")
        void testGetReturnReceiptDetail_Success() {
            Object[] row = new Object[]{
                    "RET001", "INV001", new BigDecimal("50000"), "Hàng lỗi",
                    LocalDateTime.now(), "EMP01", "Nguyễn Văn A", "nva",
                    "CUST01", "Trần Văn B", "0901234567"
            };

            when(returnReceiptRepository.findReturnReceiptHeaderById("RET001")).thenReturn(Optional.of(row));

            ReturnReceiptResponse.DetailResponse response = returnReceiptService.getReturnReceiptDetail("RET001");

            assertNotNull(response);
            assertEquals("RET001", response.getReturnReceiptId());
            assertEquals("INV001", response.getInvoiceId());
            assertEquals("Hàng lỗi", response.getReason());
            assertEquals(new BigDecimal("50000"), response.getRefundAmount());
            assertEquals("EMP01", response.getCreatedBy().getEmployeeId());
            assertEquals("Nguyễn Văn A", response.getCreatedBy().getFullName());
            assertEquals("CUST01", response.getCustomer().getCustomerId());
            assertEquals("Trần Văn B", response.getCustomer().getFullName());
        }
    }
}
