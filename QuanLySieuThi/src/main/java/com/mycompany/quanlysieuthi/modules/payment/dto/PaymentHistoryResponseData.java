package com.mycompany.quanlysieuthi.modules.payment.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO dữ liệu trả về cho API xem lịch sử thanh toán (GET /api/payment).
 * Bao gồm bảng tóm tắt doanh thu (summary) và danh sách giao dịch (transactions).
 */
public class PaymentHistoryResponseData {

    private PaymentSummaryDto summary;                  // Thống kê tổng tiền theo phương thức
    private List<PaymentTransactionDto> transactions;   // Danh sách chi tiết các giao dịch

    public PaymentHistoryResponseData() {
        this.summary = new PaymentSummaryDto();
        this.transactions = new ArrayList<>();
    }

    public PaymentHistoryResponseData(PaymentSummaryDto summary, List<PaymentTransactionDto> transactions) {
        this.summary = summary != null ? summary : new PaymentSummaryDto();
        this.transactions = transactions != null ? transactions : new ArrayList<>();
    }

    public PaymentSummaryDto getSummary() {
        return summary;
    }

    public void setSummary(PaymentSummaryDto summary) {
        this.summary = summary;
    }

    public List<PaymentTransactionDto> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<PaymentTransactionDto> transactions) {
        this.transactions = transactions;
    }
}
