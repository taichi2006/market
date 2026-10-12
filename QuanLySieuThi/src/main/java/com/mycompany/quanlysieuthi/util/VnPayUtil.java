package com.mycompany.quanlysieuthi.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Lớp tiện ích hỗ trợ tích hợp cổng thanh toán VNPay (Sandbox và Production).
 * Chịu trách nhiệm tạo URL thanh toán, băm mật mã HMAC-SHA512 và đối soát chữ ký số.
 */
public final class VnPayUtil {

    private static final String DEFAULT_TMN_CODE = "8SZPM6OH";
    private static final String DEFAULT_HASH_SECRET = "DZCCAOLKTPSPVREZXKTJYBXMRVPBVHLY";
    private static final String DEFAULT_URL = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    private static final String DEFAULT_RETURN_URL = "http://localhost:8080/QuanLySieuThi/api/payment/vnpay-return";

    private static final String TMN_CODE;
    private static final String HASH_SECRET;
    private static final String PAYMENT_URL;
    private static final String RETURN_URL;

    static {
        Properties env = new Properties();
        try (InputStream input = VnPayUtil.class.getClassLoader().getResourceAsStream(".env")) {
            if (input != null) {
                env.load(new InputStreamReader(input, StandardCharsets.UTF_8));
            }
        } catch (Exception ignored) {
        }

        TMN_CODE = layGiaTriCauHinh(env, "VNP_TMN_CODE", DEFAULT_TMN_CODE);
        HASH_SECRET = layGiaTriCauHinh(env, "VNP_HASH_SECRET", DEFAULT_HASH_SECRET);
        PAYMENT_URL = layGiaTriCauHinh(env, "VNP_URL", DEFAULT_URL);
        RETURN_URL = layGiaTriCauHinh(env, "VNP_RETURN_URL", DEFAULT_RETURN_URL);
    }

    private static String layGiaTriCauHinh(Properties env, String key, String defaultValue) {
        String val = env.getProperty(key);
        if (val == null || val.trim().isEmpty()) {
            val = System.getenv(key);
        }
        return (val != null && !val.trim().isEmpty()) ? val.trim() : defaultValue;
    }

    private VnPayUtil() {
    }

    public static String getTmnCode() {
        return TMN_CODE;
    }

    public static String getHashSecret() {
        return HASH_SECRET;
    }

    public static String getPaymentUrl() {
        return PAYMENT_URL;
    }

    public static String getReturnUrl() {
        return RETURN_URL;
    }

    public static String createPaymentUrl(String transactionId, String invoiceId, BigDecimal amount, String clientIp) {
        ZoneId zoneId = ZoneId.of("Asia/Ho_Chi_Minh");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        LocalDateTime now = LocalDateTime.now(zoneId);
        LocalDateTime expire = now.plusMinutes(15);

        long vnpAmount = amount.multiply(BigDecimal.valueOf(100)).longValue();

        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", TMN_CODE);
        vnpParams.put("vnp_Amount", String.valueOf(vnpAmount));
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", transactionId);
        vnpParams.put("vnp_OrderInfo", "Thanh toan hoa don " + invoiceId);
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", RETURN_URL);
        vnpParams.put("vnp_IpAddr", (clientIp != null && !clientIp.isEmpty()) ? clientIp : "127.0.0.1");
        vnpParams.put("vnp_CreateDate", now.format(formatter));
        vnpParams.put("vnp_ExpireDate", expire.format(formatter));

        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        try {
            Iterator<String> itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = vnpParams.get(fieldName);
                if (fieldValue != null && !fieldValue.isEmpty()) {
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));

                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));

                    if (itr.hasNext()) {
                        query.append('&');
                        hashData.append('&');
                    }
                }
            }

            String vnpSecureHash = hmacSHA512(HASH_SECRET, hashData.toString());
            query.append("&vnp_SecureHash=").append(vnpSecureHash);

            return PAYMENT_URL + "?" + query.toString();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi khởi tạo URL thanh toán VNPay: " + e.getMessage(), e);
        }
    }

    public static boolean validateSignature(Map<String, String[]> parameterMap) {
        if (parameterMap == null || parameterMap.isEmpty()) {
            return false;
        }

        String receivedSecureHash = null;
        Map<String, String> fields = new HashMap<>();

        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            String key = entry.getKey();
            String[] values = entry.getValue();
            if (values != null && values.length > 0) {
                String value = values[0];
                if ("vnp_SecureHash".equalsIgnoreCase(key)) {
                    receivedSecureHash = value;
                } else if (!"vnp_SecureHashType".equalsIgnoreCase(key) && key.startsWith("vnp_")) {
                    fields.put(key, value);
                }
            }
        }

        if (receivedSecureHash == null || receivedSecureHash.trim().isEmpty()) {
            return false;
        }

        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        try {
            Iterator<String> itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = fields.get(fieldName);
                if (fieldValue != null && !fieldValue.isEmpty()) {
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    if (itr.hasNext()) {
                        hashData.append('&');
                    }
                }
            }

            String calculatedHash = hmacSHA512(HASH_SECRET, hashData.toString());
            return calculatedHash.equalsIgnoreCase(receivedSecureHash.trim());
        } catch (Exception e) {
            return false;
        }
    }

    public static String hmacSHA512(String key, String data) {
        try {
            if (key == null || data == null) {
                throw new NullPointerException("Khóa và dữ liệu không được phép là null");
            }
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] bytes = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hash = new StringBuilder();
            for (byte b : bytes) {
                hash.append(String.format("%02x", b));
            }
            return hash.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Lỗi khi tính toán băm HMAC-SHA512: " + ex.getMessage(), ex);
        }
    }
}
