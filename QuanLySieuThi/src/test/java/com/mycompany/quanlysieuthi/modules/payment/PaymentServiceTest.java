package com.mycompany.quanlysieuthi.modules.payment;

import com.mycompany.quanlysieuthi.common.exception.AppException.*;
import com.mycompany.quanlysieuthi.entity.Invoice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test cho PaymentService")
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(paymentRepository);
    }

    @Nested
    @DisplayName("Kiểm thử getInvoicePaymentHistory() với fn_TongTienDaThanhToanTheoHoaDon")
    class GetInvoicePaymentHistoryTests {

        @Test
        @DisplayName("Lấy lịch sử thanh toán hóa đơn và tổng tiền từ hàm Scalar SQL")
        void testGetInvoicePaymentHistory_Success() {
            Invoice invoice = new Invoice();
            invoice.setInvoiceId("HD000001");
            invoice.setStatus("PENDING");
            invoice.setOriginalTotal(new BigDecimal("1000000.00"));
            invoice.setDiscountAmount(new BigDecimal("100000.00"));

            when(paymentRepository.findInvoiceById("HD000001")).thenReturn(Optional.of(invoice));

            PaymentResponse.TransactionDto tx = new PaymentResponse.TransactionDto(
                    "TX000001", "HD000001", "CASH", new BigDecimal("500000.00"), "PAID", "2026-10-15T10:00:00.000Z", "Thu ngân 1"
            );
            when(paymentRepository.findTransactionsByInvoiceId("HD000001")).thenReturn(List.of(tx));
            when(paymentRepository.getTotalPaidAmountByInvoice("HD000001")).thenReturn(new BigDecimal("500000.00"));

            PaymentResponse.HistoryResponse history = paymentService.getInvoicePaymentHistory("HD000001");

            assertNotNull(history);
            assertEquals("HD000001", history.invoiceId());
            assertEquals("PENDING", history.invoiceStatus());
            assertEquals(new BigDecimal("900000.00"), history.summary().finalAmount());
            assertEquals(new BigDecimal("500000.00"), history.summary().totalPaid());
            assertEquals(new BigDecimal("400000.00"), history.summary().remainingAmount());
            assertEquals(1, history.transactions().size());

            verify(paymentRepository).findTransactionsByInvoiceId("HD000001");
            verify(paymentRepository).getTotalPaidAmountByInvoice("HD000001");
        }

        @Test
        @DisplayName("Ném NotFoundException khi hóa đơn không tồn tại")
        void testGetInvoicePaymentHistory_NotFound() {
            when(paymentRepository.findInvoiceById("HD999999")).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> paymentService.getInvoicePaymentHistory("HD999999"));
        }
    }

    @Nested
    @DisplayName("Kiểm thử processCashPaymentViaProcedure() với SP sp_XuLyThanhToanTienMat")
    class ProcessCashPaymentViaProcedureTests {

        @Test
        @DisplayName("Thu tiền mặt trực tiếp qua Stored Procedure thành công")
        void testProcessCashPaymentViaProcedure_Success() {
            doNothing().when(paymentRepository).executeCashPaymentProcedure(anyString(), eq("HD000001"), eq(new BigDecimal("300000.00")));

            PaymentResponse.CashResponse resp = paymentService.processCashPaymentViaProcedure("HD000001", new BigDecimal("300000.00"));

            assertNotNull(resp);
            assertEquals("HD000001", resp.invoiceId());
            assertEquals(new BigDecimal("300000.00"), resp.amount());
            assertEquals("CASH", resp.paymentMethod());
            assertEquals("PAID", resp.status());
            verify(paymentRepository).executeCashPaymentProcedure(anyString(), eq("HD000001"), eq(new BigDecimal("300000.00")));
        }

        @Test
        @DisplayName("Ném IllegalArgumentException khi thiếu invoiceId hoặc amount <= 0")
        void testProcessCashPaymentViaProcedure_ValidationFailures() {
            assertThrows(IllegalArgumentException.class, () ->
                    paymentService.processCashPaymentViaProcedure(null, new BigDecimal("100000.00")));
            assertThrows(IllegalArgumentException.class, () ->
                    paymentService.processCashPaymentViaProcedure("HD000001", BigDecimal.ZERO));
            assertThrows(IllegalArgumentException.class, () ->
                    paymentService.processCashPaymentViaProcedure("HD000001", new BigDecimal("-10000.00")));
        }
    }

    @Nested
    @DisplayName("Kiểm thử processPayment() với CASH và sp_Tx_ThanhToanVaTichDiem")
    class ProcessPaymentTests {

        @Test
        @DisplayName("Thanh toán tiền mặt kích hoạt giao dịch SQL sp_Tx_ThanhToanVaTichDiem")
        void testProcessPayment_CashSuccess() {
            Invoice invoice = new Invoice();
            invoice.setInvoiceId("HD000001");
            invoice.setStatus("PENDING");
            invoice.setOriginalTotal(new BigDecimal("500000.00"));
            invoice.setDiscountAmount(BigDecimal.ZERO);

            when(paymentRepository.findInvoiceById("HD000001")).thenReturn(Optional.of(invoice));
            doNothing().when(paymentRepository).executePaymentAndLoyaltyTransaction(anyString(), eq("HD000001"), eq("CASH"), eq(new BigDecimal("500000.00")));

            PaymentRequest.Process request = new PaymentRequest.Process("HD000001", "CASH");
            Object resp = paymentService.processPayment(request, "127.0.0.1");

            assertNotNull(resp);
            assertTrue(resp instanceof PaymentResponse.CashResponse);
            PaymentResponse.CashResponse cashResp = (PaymentResponse.CashResponse) resp;
            assertEquals("HD000001", cashResp.invoiceId());
            assertEquals("PAID", cashResp.status());
            verify(paymentRepository).executePaymentAndLoyaltyTransaction(anyString(), eq("HD000001"), eq("CASH"), eq(new BigDecimal("500000.00")));
        }
    }
}
