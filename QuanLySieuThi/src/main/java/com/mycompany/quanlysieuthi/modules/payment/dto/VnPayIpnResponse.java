package com.mycompany.quanlysieuthi.modules.payment.dto;

import com.google.gson.annotations.SerializedName;

/**
 * DTO phản hồi tiêu chuẩn cho cổng thanh toán VNPay khi nhận IPN (Server-to-Server).
 */
public class VnPayIpnResponse {

    @SerializedName("RspCode")
    private String rspCode; // Mã phản hồi: "00" (Thành công), "97" (Sai chữ ký), "01" (Không tìm thấy đơn), "02" (Đã xác nhận), "04" (Sai số tiền)

    @SerializedName("Message")
    private String message; // Thông điệp phản hồi mô tả

    public VnPayIpnResponse() {
    }

    public VnPayIpnResponse(String rspCode, String message) {
        this.rspCode = rspCode;
        this.message = message;
    }

    public String getRspCode() {
        return rspCode;
    }

    public void setRspCode(String rspCode) {
        this.rspCode = rspCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
