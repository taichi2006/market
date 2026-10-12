package com.mycompany.quanlysieuthi.modules.payment.dto;

import java.math.BigDecimal;

/**
 * DTO tóm tắt doanh thu theo phương thức thanh toán trong lịch sử (summary).
 */
public class PaymentSummaryDto {

    private BigDecimal totalCash;       // Tổng tiền mặt thu được (CASH)
    private BigDecimal totalOnline;     // Tổng tiền thanh toán điện tử (QR / BANK_TRANSFER)
    private BigDecimal totalRevenue;    // Tổng toàn bộ doanh thu

    public PaymentSummaryDto() {
        this.totalCash = BigDecimal.ZERO;
        this.totalOnline = BigDecimal.ZERO;
        this.totalRevenue = BigDecimal.ZERO;
    }

    public PaymentSummaryDto(BigDecimal totalCash, BigDecimal totalOnline, BigDecimal totalRevenue) {
        this.totalCash = totalCash != null ? totalCash : BigDecimal.ZERO;
        this.totalOnline = totalOnline != null ? totalOnline : BigDecimal.ZERO;
        this.totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
    }

    public BigDecimal getTotalCash() {
        return totalCash;
    }

    public void setTotalCash(BigDecimal totalCash) {
        this.totalCash = totalCash;
    }

    public BigDecimal getTotalOnline() {
        return totalOnline;
    }

    public void setTotalOnline(BigDecimal totalOnline) {
        this.totalOnline = totalOnline;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
}
