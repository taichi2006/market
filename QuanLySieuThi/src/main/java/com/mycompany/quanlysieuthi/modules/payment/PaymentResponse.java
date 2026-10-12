package com.mycompany.quanlysieuthi.modules.payment;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO definitions for Payment module.
 */
public class PaymentResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    public static class CashResponse implements Serializable {
        private static final long serialVersionUID = 1L;

        private String transactionId;
        private String invoiceId;
        private BigDecimal amount;
        private String paymentMethod;
        private String status;
        private String paymentTime;

        public CashResponse() {
        }

        public CashResponse(String transactionId, String invoiceId, BigDecimal amount,
                            String paymentMethod, String status, String paymentTime) {
            this.transactionId = transactionId;
            this.invoiceId = invoiceId;
            this.amount = amount;
            this.paymentMethod = paymentMethod;
            this.status = status;
            this.paymentTime = paymentTime;
        }

        public String getTransactionId() {
            return transactionId;
        }

        public void setTransactionId(String transactionId) {
            this.transactionId = transactionId;
        }

        public String getInvoiceId() {
            return invoiceId;
        }

        public void setInvoiceId(String invoiceId) {
            this.invoiceId = invoiceId;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public String getPaymentMethod() {
            return paymentMethod;
        }

        public void setPaymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getPaymentTime() {
            return paymentTime;
        }

        public void setPaymentTime(String paymentTime) {
            this.paymentTime = paymentTime;
        }
    }

    public static class VnpayResponse implements Serializable {
        private static final long serialVersionUID = 1L;

        private String transactionId;
        private String invoiceId;
        private BigDecimal amount;
        private String paymentUrl;

        public VnpayResponse() {
        }

        public VnpayResponse(String transactionId, String invoiceId, BigDecimal amount, String paymentUrl) {
            this.transactionId = transactionId;
            this.invoiceId = invoiceId;
            this.amount = amount;
            this.paymentUrl = paymentUrl;
        }

        public String getTransactionId() {
            return transactionId;
        }

        public void setTransactionId(String transactionId) {
            this.transactionId = transactionId;
        }

        public String getInvoiceId() {
            return invoiceId;
        }

        public void setInvoiceId(String invoiceId) {
            this.invoiceId = invoiceId;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public String getPaymentUrl() {
            return paymentUrl;
        }

        public void setPaymentUrl(String paymentUrl) {
            this.paymentUrl = paymentUrl;
        }
    }

    public static class TransactionDto implements Serializable {
        private static final long serialVersionUID = 1L;

        private String transactionId;
        private String invoiceId;
        private BigDecimal amount;
        private String paymentMethod;
        private String status;
        private String paymentTime;
        private String cashierName;

        public TransactionDto() {
        }

        public TransactionDto(String transactionId, BigDecimal amount, String paymentMethod, String status, String paymentTime) {
            this.transactionId = transactionId;
            this.amount = amount;
            this.paymentMethod = paymentMethod;
            this.status = status;
            this.paymentTime = paymentTime;
        }

        public TransactionDto(String transactionId, String invoiceId, String paymentMethod,
                              BigDecimal amount, String status, String paymentTime, String cashierName) {
            this.transactionId = transactionId;
            this.invoiceId = invoiceId;
            this.paymentMethod = paymentMethod;
            this.amount = amount;
            this.status = status;
            this.paymentTime = paymentTime;
            this.cashierName = cashierName;
        }

        public String getTransactionId() {
            return transactionId;
        }

        public void setTransactionId(String transactionId) {
            this.transactionId = transactionId;
        }

        public String getInvoiceId() {
            return invoiceId;
        }

        public void setInvoiceId(String invoiceId) {
            this.invoiceId = invoiceId;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public String getPaymentMethod() {
            return paymentMethod;
        }

        public void setPaymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getPaymentTime() {
            return paymentTime;
        }

        public void setPaymentTime(String paymentTime) {
            this.paymentTime = paymentTime;
        }

        public String getCashierName() {
            return cashierName;
        }

        public void setCashierName(String cashierName) {
            this.cashierName = cashierName;
        }
    }

    public static class SummaryDto implements Serializable {
        private static final long serialVersionUID = 1L;

        private BigDecimal originalTotal;
        private BigDecimal discountAmount;
        private BigDecimal finalAmount;
        private BigDecimal totalPaid;
        private BigDecimal remainingAmount;

        public SummaryDto() {
        }

        public SummaryDto(BigDecimal originalTotal, BigDecimal discountAmount, BigDecimal finalAmount, BigDecimal totalPaid, BigDecimal remainingAmount) {
            this.originalTotal = originalTotal;
            this.discountAmount = discountAmount;
            this.finalAmount = finalAmount;
            this.totalPaid = totalPaid;
            this.remainingAmount = remainingAmount;
        }

        public BigDecimal getOriginalTotal() {
            return originalTotal;
        }

        public void setOriginalTotal(BigDecimal originalTotal) {
            this.originalTotal = originalTotal;
        }

        public BigDecimal getDiscountAmount() {
            return discountAmount;
        }

        public void setDiscountAmount(BigDecimal discountAmount) {
            this.discountAmount = discountAmount;
        }

        public BigDecimal getFinalAmount() {
            return finalAmount;
        }

        public void setFinalAmount(BigDecimal finalAmount) {
            this.finalAmount = finalAmount;
        }

        public BigDecimal getTotalPaid() {
            return totalPaid;
        }

        public void setTotalPaid(BigDecimal totalPaid) {
            this.totalPaid = totalPaid;
        }

        public BigDecimal getRemainingAmount() {
            return remainingAmount;
        }

        public void setRemainingAmount(BigDecimal remainingAmount) {
            this.remainingAmount = remainingAmount;
        }
    }

    public static class HistoryResponse implements Serializable {
        private static final long serialVersionUID = 1L;

        private String invoiceId;
        private String invoiceStatus;
        private SummaryDto summary;
        private List<TransactionDto> transactions;

        public HistoryResponse() {
        }

        public HistoryResponse(String invoiceId, String invoiceStatus, SummaryDto summary, List<TransactionDto> transactions) {
            this.invoiceId = invoiceId;
            this.invoiceStatus = invoiceStatus;
            this.summary = summary;
            this.transactions = transactions;
        }

        public String getInvoiceId() {
            return invoiceId;
        }

        public void setInvoiceId(String invoiceId) {
            this.invoiceId = invoiceId;
        }

        public String getInvoiceStatus() {
            return invoiceStatus;
        }

        public void setInvoiceStatus(String invoiceStatus) {
            this.invoiceStatus = invoiceStatus;
        }

        public SummaryDto getSummary() {
            return summary;
        }

        public void setSummary(SummaryDto summary) {
            this.summary = summary;
        }

        public List<TransactionDto> getTransactions() {
            return transactions;
        }

        public void setTransactions(List<TransactionDto> transactions) {
            this.transactions = transactions;
        }
    }

    public static class IpnResponse implements Serializable {
        private static final long serialVersionUID = 1L;

        private String RspCode;
        private String Message;

        public IpnResponse() {
        }

        public IpnResponse(String rspCode, String message) {
            this.RspCode = rspCode;
            this.Message = message;
        }

        public String getRspCode() {
            return RspCode;
        }

        public void setRspCode(String rspCode) {
            this.RspCode = rspCode;
        }

        public String getMessage() {
            return Message;
        }

        public void setMessage(String message) {
            this.Message = message;
        }
    }
}
