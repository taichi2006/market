package com.mycompany.quanlysieuthi.modules.invoice;

import com.mycompany.quanlysieuthi.entity.Customer;
import com.mycompany.quanlysieuthi.entity.Invoice;
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
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test cho InvoiceService")
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    private InvoiceService invoiceService;

    @BeforeEach
    void setUp() {
        invoiceService = new InvoiceService(invoiceRepository);
    }

    // =========================================================================
    // 1. CREATE INVOICE (TẠO HÓA ĐƠN BÁN HÀNG)
    // =========================================================================
    @Nested
    @DisplayName("Kiểm thử createInvoice()")
    class CreateInvoiceTests {

        @Test
        @DisplayName("Ném ngoại lệ khi request là null")
        void testCreateInvoice_NullRequest_ThrowsIllegalArgumentException() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    invoiceService.createInvoice(null, "EMP001", "Nguyễn Văn A")
            );
            assertEquals("Dữ liệu hóa đơn không được để trống", ex.getMessage());
        }

        @Test
        @DisplayName("Ném ngoại lệ khi danh sách items là null hoặc rỗng")
        void testCreateInvoice_EmptyItems_ThrowsIllegalArgumentException() {
            InvoiceRequest.Create request = new InvoiceRequest.Create("CUST001", false, Collections.emptyList());

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    invoiceService.createInvoice(request, "EMP001", "Nguyễn Văn A")
            );
            assertEquals("Danh sách sản phẩm không được rỗng", ex.getMessage());
        }

        @Test
        @DisplayName("Ném ngoại lệ NotFoundException khi customerId không tồn tại")
        void testCreateInvoice_CustomerNotFound_ThrowsNotFoundException() {
            InvoiceRequest.Create request = new InvoiceRequest.Create("CUST_NON_EXIST", false,
                    List.of(new InvoiceRequest.Item("PROD001", 1)));

            when(invoiceRepository.findCustomerById("CUST_NON_EXIST")).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(NotFoundException.class, () ->
                    invoiceService.createInvoice(request, "EMP001", "Nguyễn Văn A")
            );
            assertTrue(ex.getMessage().contains("Khách hàng không tồn tại: CUST_NON_EXIST"));
        }

        @Test
        @DisplayName("Ném ngoại lệ khi productId trong item bị để trống")
        void testCreateInvoice_BlankProductId_ThrowsIllegalArgumentException() {
            InvoiceRequest.Create request = new InvoiceRequest.Create(null, false,
                    List.of(new InvoiceRequest.Item("   ", 2)));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    invoiceService.createInvoice(request, "EMP001", "Nguyễn Văn A")
            );
            assertEquals("Mã sản phẩm không được để trống", ex.getMessage());
        }

        @Test
        @DisplayName("Ném ngoại lệ khi số lượng mua <= 0 hoặc null")
        void testCreateInvoice_InvalidQuantity_ThrowsIllegalArgumentException() {
            InvoiceRequest.Create request = new InvoiceRequest.Create(null, false,
                    List.of(new InvoiceRequest.Item("PROD001", 0)));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    invoiceService.createInvoice(request, "EMP001", "Nguyễn Văn A")
            );
            assertEquals("Số lượng sản phẩm phải > 0", ex.getMessage());
        }

        @Test
        @DisplayName("Ném ngoại lệ NotFoundException khi productId không tồn tại trong hệ thống")
        void testCreateInvoice_ProductNotFound_ThrowsNotFoundException() {
            InvoiceRequest.Create request = new InvoiceRequest.Create(null, false,
                    List.of(new InvoiceRequest.Item("PROD_UNKNOWN", 2)));

            when(invoiceRepository.findProductById("PROD_UNKNOWN")).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(NotFoundException.class, () ->
                    invoiceService.createInvoice(request, "EMP001", "Nguyễn Văn A")
            );
            assertTrue(ex.getMessage().contains("Sản phẩm không tồn tại: PROD_UNKNOWN"));
        }

        @Test
        @DisplayName("Ném ngoại lệ khi sản phẩm không đủ tồn kho")
        void testCreateInvoice_InsufficientStock_ThrowsIllegalArgumentException() {
            Product product = new Product("PROD001", "CAT01", "Mì Hảo Hảo", 5,
                    LocalDateTime.now().plusMonths(6), "ON_SALE", new BigDecimal("4500"));

            InvoiceRequest.Create request = new InvoiceRequest.Create(null, false,
                    List.of(new InvoiceRequest.Item("PROD001", 10)));

            when(invoiceRepository.findProductById("PROD001")).thenReturn(Optional.of(product));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    invoiceService.createInvoice(request, "EMP001", "Nguyễn Văn A")
            );
            assertTrue(ex.getMessage().contains("không đủ tồn kho (hiện có: 5)"));
        }

        @Test
        @DisplayName("Ném ngoại lệ khi tổng số lượng của nhiều item trùng productId vượt quá tồn kho")
        void testCreateInvoice_DuplicateItemExceedsStock_ThrowsIllegalArgumentException() {
            Product product = new Product("PROD001", "CAT01", "Mì Hảo Hảo", 5,
                    LocalDateTime.now().plusMonths(6), "ON_SALE", new BigDecimal("4500"));

            InvoiceRequest.Create request = new InvoiceRequest.Create(null, false, List.of(
                    new InvoiceRequest.Item("PROD001", 3),
                    new InvoiceRequest.Item("PROD001", 3)
            ));

            when(invoiceRepository.findProductById("PROD001")).thenReturn(Optional.of(product));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    invoiceService.createInvoice(request, "EMP001", "Nguyễn Văn A")
            );
            assertTrue(ex.getMessage().contains("không đủ tồn kho (hiện có: 5)"));
        }

        @Test
        @DisplayName("Tạo hóa đơn thành công cho khách vãng lai (không có customerId)")
        void testCreateInvoice_GuestCustomer_Success() {
            Product p1 = new Product("PROD001", "CAT01", "Mì Hảo Hảo", 50,
                    LocalDateTime.now().plusMonths(6), "ON_SALE", new BigDecimal("4500"));
            Product p2 = new Product("PROD002", "CAT01", "Sữa tươi TH True Milk", 20,
                    LocalDateTime.now().plusMonths(3), "ON_SALE", new BigDecimal("35000"));

            when(invoiceRepository.findProductById("PROD001")).thenReturn(Optional.of(p1));
            when(invoiceRepository.findProductById("PROD002")).thenReturn(Optional.of(p2));

            InvoiceRequest.Create request = new InvoiceRequest.Create(null, false, List.of(
                    new InvoiceRequest.Item("PROD001", 2), // 9,000
                    new InvoiceRequest.Item("PROD002", 1)  // 35,000 -> Total = 44,000
            ));

            InvoiceResponse.CreateResponse response = invoiceService.createInvoice(request, "EMP001", "Trần Văn B");

            assertNotNull(response);
            assertNotNull(response.getInvoiceId());
            assertEquals("PENDING", response.getStatus());
            assertEquals(new BigDecimal("44000"), response.getOriginalTotal());
            assertEquals(BigDecimal.ZERO, response.getDiscountAmount());
            assertEquals(new BigDecimal("44000"), response.getFinalTotal());
            assertNull(response.getCustomer());
            assertEquals("EMP001", response.getEmployee().getEmployeeId());
            assertEquals("Trần Văn B", response.getEmployee().getFullName());
            assertEquals(2, response.getItems().size());

            verify(invoiceRepository, times(1)).saveInvoice(any(Invoice.class), anyList(), isNull(), anyMap());
        }

        @Test
        @DisplayName("Tạo hóa đơn thành công cho hội viên: tích điểm thưởng khi không dùng điểm giảm giá")
        void testCreateInvoice_MemberCustomer_EarnsLoyaltyPoints_Success() {
            Customer customer = new Customer("CUST001", "Lê Văn C", "0901234567", "c@gmail.com", 500, LocalDateTime.now());
            Product product = new Product("PROD001", "CAT01", "Thùng bia Tiger", 100,
                    LocalDateTime.now().plusMonths(6), "ON_SALE", new BigDecimal("390000"));

            when(invoiceRepository.findCustomerById("CUST001")).thenReturn(Optional.of(customer));
            when(invoiceRepository.findProductById("PROD001")).thenReturn(Optional.of(product));

            InvoiceRequest.Create request = new InvoiceRequest.Create("CUST001", false, List.of(
                    new InvoiceRequest.Item("PROD001", 1) // 390,000 -> Earns floor(390000 / 10000) * 100 = 3900 points
            ));

            InvoiceResponse.CreateResponse response = invoiceService.createInvoice(request, "EMP001", "Trần Văn B");

            assertNotNull(response);
            assertEquals(new BigDecimal("390000"), response.getOriginalTotal());
            assertEquals(BigDecimal.ZERO, response.getDiscountAmount());
            assertEquals(new BigDecimal("390000"), response.getFinalTotal());
            // 500 + 3900 = 4400
            assertEquals(4400, customer.getLoyaltyPoints());

            verify(invoiceRepository, times(1)).saveInvoice(any(Invoice.class), anyList(), eq(customer), anyMap());
        }

        @Test
        @DisplayName("Tạo hóa đơn thành công có áp dụng điểm thưởng giảm giá (>= 100,000 điểm)")
        void testCreateInvoice_MemberCustomer_UseLoyaltyPoints_Success() {
            // Khách có 250,000 điểm -> Dùng 200,000 điểm đổi thành 2,000 VND giảm giá. Còn dư 50,000 điểm
            Customer customer = new Customer("CUST001", "Lê Văn C", "0901234567", "c@gmail.com", 250000, LocalDateTime.now());
            Product product = new Product("PROD001", "CAT01", "Dầu ăn Simply", 50,
                    LocalDateTime.now().plusMonths(6), "ON_SALE", new BigDecimal("65000"));

            when(invoiceRepository.findCustomerById("CUST001")).thenReturn(Optional.of(customer));
            when(invoiceRepository.findProductById("PROD001")).thenReturn(Optional.of(product));

            InvoiceRequest.Create request = new InvoiceRequest.Create("CUST001", true, List.of(
                    new InvoiceRequest.Item("PROD001", 1) // 65,000 VND
            ));

            InvoiceResponse.CreateResponse response = invoiceService.createInvoice(request, "EMP001", "Trần Văn B");

            assertNotNull(response);
            assertEquals(new BigDecimal("65000"), response.getOriginalTotal());
            assertEquals(new BigDecimal("2000"), response.getDiscountAmount());
            assertEquals(new BigDecimal("63000"), response.getFinalTotal());
            // Điểm còn lại: 250,000 - 200,000 = 50,000 (không được cộng điểm mới vì đã giảm giá)
            assertEquals(50000, customer.getLoyaltyPoints());
        }

        @Test
        @DisplayName("Điểm thưởng không đủ 100,000 điểm: không giảm giá, được tích điểm bình thường")
        void testCreateInvoice_MemberCustomer_NotEnoughPoints_NoDiscount() {
            Customer customer = new Customer("CUST001", "Lê Văn C", "0901234567", "c@gmail.com", 80000, LocalDateTime.now());
            Product product = new Product("PROD001", "CAT01", "Bánh Cosy", 50,
                    LocalDateTime.now().plusMonths(6), "ON_SALE", new BigDecimal("50000"));

            when(invoiceRepository.findCustomerById("CUST001")).thenReturn(Optional.of(customer));
            when(invoiceRepository.findProductById("PROD001")).thenReturn(Optional.of(product));

            InvoiceRequest.Create request = new InvoiceRequest.Create("CUST001", true, List.of(
                    new InvoiceRequest.Item("PROD001", 1) // 50,000 -> Tích 500 điểm
            ));

            InvoiceResponse.CreateResponse response = invoiceService.createInvoice(request, "EMP001", "Trần Văn B");

            assertNotNull(response);
            assertEquals(BigDecimal.ZERO, response.getDiscountAmount());
            assertEquals(new BigDecimal("50000"), response.getFinalTotal());
            // 80000 + 500 = 80500
            assertEquals(80500, customer.getLoyaltyPoints());
        }

        @Test
        @DisplayName("Số tiền giảm giá vượt quá tổng hóa đơn: giảm tối đa bằng tổng hóa đơn")
        void testCreateInvoice_DiscountExceedsTotal_CappedAtOriginalTotal() {
            // Khách có 600,000 điểm -> Đổi được 6,000 VND giảm giá, nhưng mua kẹo chỉ 4,000 VND
            Customer customer = new Customer("CUST001", "Lê Văn C", "0901234567", "c@gmail.com", 600000, LocalDateTime.now());
            Product product = new Product("PROD001", "CAT01", "Kẹo mút Chupa Chups", 100,
                    LocalDateTime.now().plusMonths(6), "ON_SALE", new BigDecimal("4000"));

            when(invoiceRepository.findCustomerById("CUST001")).thenReturn(Optional.of(customer));
            when(invoiceRepository.findProductById("PROD001")).thenReturn(Optional.of(product));

            InvoiceRequest.Create request = new InvoiceRequest.Create("CUST001", true, List.of(
                    new InvoiceRequest.Item("PROD001", 1) // 4,000 VND
            ));

            InvoiceResponse.CreateResponse response = invoiceService.createInvoice(request, "EMP001", "Trần Văn B");

            assertNotNull(response);
            assertEquals(new BigDecimal("4000"), response.getOriginalTotal());
            assertEquals(new BigDecimal("4000"), response.getDiscountAmount());
            assertEquals(BigDecimal.ZERO, response.getFinalTotal());
        }
    }

    // =========================================================================
    // 2. GET INVOICES (LẤY DANH SÁCH HÓA ĐƠN)
    // =========================================================================
    @Nested
    @DisplayName("Kiểm thử getInvoices()")
    class GetInvoicesTests {

        @Test
        @DisplayName("Gọi repository và trả về danh sách hóa đơn theo bộ lọc")
        void testGetInvoices_Success() {
            List<InvoiceResponse.ListItemDto> mockList = List.of(
                    new InvoiceResponse.ListItemDto("INV001", "2026-10-14T10:00:00.000Z", "PAID",
                            new BigDecimal("100000"), BigDecimal.ZERO, new BigDecimal("100000"), null, null)
            );

            when(invoiceRepository.findInvoices(1, 10, "PAID", null, null, null, null))
                    .thenReturn(mockList);

            List<InvoiceResponse.ListItemDto> result = invoiceService.getInvoices(1, 10, "PAID", null, null, null, null);

            assertEquals(1, result.size());
            assertEquals("INV001", result.get(0).getInvoiceId());
            verify(invoiceRepository, times(1)).findInvoices(1, 10, "PAID", null, null, null, null);
        }
    }

    // =========================================================================
    // 3. GET INVOICE DETAIL (CHI TIẾT HÓA ĐƠN)
    // =========================================================================
    @Nested
    @DisplayName("Kiểm thử getInvoiceDetail()")
    class GetInvoiceDetailTests {

        @Test
        @DisplayName("Ném ngoại lệ khi invoiceId để trống")
        void testGetInvoiceDetail_BlankId_ThrowsIllegalArgumentException() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    invoiceService.getInvoiceDetail("   ")
            );
            assertEquals("Mã hóa đơn không được để trống", ex.getMessage());
        }

        @Test
        @DisplayName("Ném ngoại lệ NotFoundException khi hóa đơn không tồn tại")
        void testGetInvoiceDetail_NotFound_ThrowsNotFoundException() {
            when(invoiceRepository.findInvoiceDetailById("INV_NON_EXIST")).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(NotFoundException.class, () ->
                    invoiceService.getInvoiceDetail("INV_NON_EXIST")
            );
            assertEquals("Không tìm thấy hóa đơn với ID tương ứng", ex.getMessage());
        }

        @Test
        @DisplayName("Lấy chi tiết hóa đơn thành công")
        void testGetInvoiceDetail_Success() {
            InvoiceResponse.DetailResponse mockDetail = new InvoiceResponse.DetailResponse(
                    "INV001", "2026-10-14T10:00:00.000Z", "PAID",
                    new BigDecimal("100000"), BigDecimal.ZERO, new BigDecimal("100000"),
                    null, null, Collections.emptyList()
            );

            when(invoiceRepository.findInvoiceDetailById("INV001")).thenReturn(Optional.of(mockDetail));

            InvoiceResponse.DetailResponse response = invoiceService.getInvoiceDetail("INV001");

            assertNotNull(response);
            assertEquals("INV001", response.getInvoiceId());
            assertEquals("PAID", response.getStatus());
        }
    }

    // =========================================================================
    // 4. CANCEL INVOICE (HỦY HÓA ĐƠN)
    // =========================================================================
    @Nested
    @DisplayName("Kiểm thử cancelInvoice()")
    class CancelInvoiceTests {

        @Test
        @DisplayName("Ném ngoại lệ khi invoiceId để trống")
        void testCancelInvoice_BlankId_ThrowsIllegalArgumentException() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    invoiceService.cancelInvoice("")
            );
            assertEquals("Mã hóa đơn không được để trống", ex.getMessage());
        }

        @Test
        @DisplayName("Ném ngoại lệ NotFoundException khi không tìm thấy hóa đơn để hủy")
        void testCancelInvoice_NotFound_ThrowsNotFoundException() {
            when(invoiceRepository.cancelInvoice("INV_NON_EXIST")).thenReturn(false);

            NotFoundException ex = assertThrows(NotFoundException.class, () ->
                    invoiceService.cancelInvoice("INV_NON_EXIST")
            );
            assertTrue(ex.getMessage().contains("Không tìm thấy hóa đơn với ID: INV_NON_EXIST"));
        }

        @Test
        @DisplayName("Hủy hóa đơn thành công")
        void testCancelInvoice_Success() {
            when(invoiceRepository.cancelInvoice("INV001")).thenReturn(true);

            InvoiceResponse.CancelResponse response = invoiceService.cancelInvoice("INV001");

            assertNotNull(response);
            assertEquals("INV001", response.getInvoiceId());
            assertEquals("CANCELLED", response.getStatus());
        }
    }
}
