package com.mycompany.quanlysieuthi.modules.payment.dto;

/**
 * DTO dữ liệu trả về khi khởi tạo link thanh toán VNPay Sandbox thành công (QR / BANK_TRANSFER).
 */
public class PaymentVnpayResponseData {

    private String paymentMethod;   // Phương thức thanh toán ("QR" hoặc "BANK_TRANSFER")
    private String paymentUrl;      // Đường dẫn thanh toán VNPay gửi cho FE hiển thị QR hoặc redirect

    public PaymentVnpayResponseData() {
    }

    public PaymentVnpayResponseData(String paymentMethod, String paymentUrl) {
        this.paymentMethod = paymentMethod;
        this.paymentUrl = paymentUrl;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentUrl() {
        return paymentUrl;
    }

    public void setPaymentUrl(String paymentUrl) {
        this.paymentUrl = paymentUrl;
    }
}
