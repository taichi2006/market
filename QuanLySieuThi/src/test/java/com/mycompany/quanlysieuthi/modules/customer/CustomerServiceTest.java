package com.mycompany.quanlysieuthi.modules.customer;

import com.mycompany.quanlysieuthi.common.exception.AppException.*;
import com.mycompany.quanlysieuthi.entity.Customer;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test cho CustomerService")
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        customerService = new CustomerService(customerRepository);
    }

    @Nested
    @DisplayName("Kiểm thử getCustomerSummaries() với View v_tong_hop_chi_tieu_khach_hang")
    class GetCustomerSummariesTests {

        @Test
        @DisplayName("Lấy danh sách tổng hợp chi tiêu và phân hạng thành viên thành công")
        void testGetCustomerSummaries_Success() {
            CustomerResponse.CustomerSummaryDto summary = new CustomerResponse.CustomerSummaryDto(
                    "KH000001", "Nguyễn Văn A", "0901234567", "a@gmail.com",
                    1500, 5L, new BigDecimal("6500000.00"), "2026-10-15T10:00:00.000Z", "VÀNG"
            );

            when(customerRepository.findCustomerSummaries(1, 10, "Nguyễn"))
                    .thenReturn(List.of(summary));

            List<CustomerResponse.CustomerSummaryDto> result = customerService.getCustomerSummaries(1, 10, "Nguyễn");

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("KH000001", result.get(0).customerId());
            assertEquals("VÀNG", result.get(0).memberTier());
            assertEquals(new BigDecimal("6500000.00"), result.get(0).totalSpent());
            verify(customerRepository).findCustomerSummaries(1, 10, "Nguyễn");
        }

        @Test
        @DisplayName("Ném ngoại lệ khi page hoặc limit không hợp lệ")
        void testGetCustomerSummaries_InvalidPaging() {
            assertThrows(IllegalArgumentException.class, () -> customerService.getCustomerSummaries(0, 10, null));
            assertThrows(IllegalArgumentException.class, () -> customerService.getCustomerSummaries(1, 0, null));
        }
    }

    @Nested
    @DisplayName("Kiểm thử getCustomerTransactions() với TVF fn_LichSuGiaoDichKhachHang")
    class GetCustomerTransactionsTests {

        @Test
        @DisplayName("Lấy lịch sử giao dịch thanh toán thành công theo khoảng ngày")
        void testGetCustomerTransactions_Success() {
            Customer customer = new Customer();
            customer.setCustomerId("KH000001");
            when(customerRepository.findById("KH000001")).thenReturn(Optional.of(customer));

            CustomerResponse.CustomerTransactionDto tx = new CustomerResponse.CustomerTransactionDto(
                    "TX000001", "HD000001", new BigDecimal("500000.00"), "CASH", "2026-10-15T10:00:00.000Z", "PAID"
            );

            when(customerRepository.findCustomerTransactions(eq("KH000001"), any(LocalDate.class), any(LocalDate.class)))
                    .thenReturn(List.of(tx));

            List<CustomerResponse.CustomerTransactionDto> result = customerService.getCustomerTransactions(
                    "KH000001", "2026-10-01", "2026-10-31"
            );

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("TX000001", result.get(0).transactionId());
            assertEquals("PAID", result.get(0).status());
            verify(customerRepository).findCustomerTransactions(eq("KH000001"), any(LocalDate.class), any(LocalDate.class));
        }

        @Test
        @DisplayName("Ném NotFoundException khi khách hàng không tồn tại")
        void testGetCustomerTransactions_NotFound() {
            when(customerRepository.findById("KH999999")).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () ->
                    customerService.getCustomerTransactions("KH999999", null, null));
        }

        @Test
        @DisplayName("Ném IllegalArgumentException khi định dạng ngày không đúng hoặc fromDate > toDate")
        void testGetCustomerTransactions_InvalidDates() {
            Customer customer = new Customer();
            customer.setCustomerId("KH000001");
            when(customerRepository.findById("KH000001")).thenReturn(Optional.of(customer));

            assertThrows(IllegalArgumentException.class, () ->
                    customerService.getCustomerTransactions("KH000001", "invalid-date", null));

            assertThrows(IllegalArgumentException.class, () ->
                    customerService.getCustomerTransactions("KH000001", "2026-10-30", "2026-10-01"));
        }
    }

    @Nested
    @DisplayName("Kiểm thử redeemLoyaltyPoints() với SP sp_DoiDiemThuongKhachHang")
    class RedeemLoyaltyPointsTests {

        @Test
        @DisplayName("Đổi điểm tích lũy thành công trừ vào hóa đơn")
        void testRedeemLoyaltyPoints_Success() {
            CustomerRequest.RedeemPoints request = new CustomerRequest.RedeemPoints("KH000001", "HD000001", 5000);

            doNothing().when(customerRepository).redeemLoyaltyPoints("KH000001", "HD000001", 5000);

            CustomerResponse.RedeemPointsResponse resp = customerService.redeemLoyaltyPoints(request);

            assertNotNull(resp);
            assertEquals("KH000001", resp.customerId());
            assertEquals("HD000001", resp.invoiceId());
            assertEquals(5000, resp.pointsRedeemed());
            assertEquals(new BigDecimal("5000"), resp.discountApplied());
            verify(customerRepository).redeemLoyaltyPoints("KH000001", "HD000001", 5000);
        }

        @Test
        @DisplayName("Ném IllegalArgumentException khi thiếu tham số hoặc points <= 0")
        void testRedeemLoyaltyPoints_ValidationFailures() {
            assertThrows(IllegalArgumentException.class, () -> customerService.redeemLoyaltyPoints(null));
            assertThrows(IllegalArgumentException.class, () ->
                    customerService.redeemLoyaltyPoints(new CustomerRequest.RedeemPoints(null, "HD000001", 100)));
            assertThrows(IllegalArgumentException.class, () ->
                    customerService.redeemLoyaltyPoints(new CustomerRequest.RedeemPoints("KH000001", null, 100)));
            assertThrows(IllegalArgumentException.class, () ->
                    customerService.redeemLoyaltyPoints(new CustomerRequest.RedeemPoints("KH000001", "HD000001", 0)));
        }

        @Test
        @DisplayName("Ném BusinessException khi Stored Procedure báo lỗi nghiệp vụ")
        void testRedeemLoyaltyPoints_BusinessException() {
            CustomerRequest.RedeemPoints request = new CustomerRequest.RedeemPoints("KH000001", "HD000001", 50000);

            doThrow(new BusinessException("Khách hàng không đủ điểm tích lũy để quy đổi!"))
                    .when(customerRepository).redeemLoyaltyPoints("KH000001", "HD000001", 50000);

            BusinessException ex = assertThrows(BusinessException.class, () -> customerService.redeemLoyaltyPoints(request));
            assertTrue(ex.getMessage().contains("không đủ điểm tích lũy"));
        }
    }
}
