package com.mycompany.quanlysieuthi.modules.payment;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO definitions for Payment module.
 */
public class PaymentResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    public record CashResponse(String transactionId, String invoiceId, BigDecimal amount,
                               String paymentMethod, String status, String paymentTime) implements Serializable {}

    public record VnpayResponse(String transactionId, String invoiceId, BigDecimal amount,
                                String paymentUrl) implements Serializable {}

    public record TransactionDto(String transactionId, String invoiceId, String paymentMethod,
                                 BigDecimal amount, String status, String paymentTime,
                                 String cashierName) implements Serializable {
        public TransactionDto(String transactionId, BigDecimal amount, String paymentMethod, String status, String paymentTime) {
            this(transactionId, null, paymentMethod, amount, status, paymentTime, null);
        }

        public String getInvoiceId() { return invoiceId; }
        public String getStatus() { return status; }
        public BigDecimal getAmount() { return amount; }
    }

    public record SummaryDto(BigDecimal originalTotal, BigDecimal discountAmount, BigDecimal finalAmount,
                             BigDecimal totalPaid, BigDecimal remainingAmount) implements Serializable {
        public SummaryDto() {
            this(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }
    }

    public record HistoryResponse(String invoiceId, String invoiceStatus, SummaryDto summary,
                                  List<TransactionDto> transactions) implements Serializable {}

    public record IpnResponse(String RspCode, String Message) implements Serializable {}
}
