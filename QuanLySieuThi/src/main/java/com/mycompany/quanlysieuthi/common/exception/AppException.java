package com.mycompany.quanlysieuthi.common.exception;

import jakarta.servlet.http.HttpServletResponse;

/**
 * File định nghĩa Exception tập trung cho toàn bộ hệ thống.
 * Chứa lớp cha AppException và toàn bộ các Exception nghiệp vụ con dưới dạng static nested classes.
 */
public class AppException extends RuntimeException {

    private final int statusCode;

    public AppException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public AppException(int statusCode, String message, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    // ==================== CÁC NGOẠI LỆ NGHIỆP VỤ CON ====================

    /** HTTP 400 - Dữ liệu client gửi lên sai cú pháp / format */
    public static class BadRequestException extends AppException {
        public BadRequestException(String message) {
            super(HttpServletResponse.SC_BAD_REQUEST, message);
        }
        public BadRequestException(String message, Throwable cause) {
            super(HttpServletResponse.SC_BAD_REQUEST, message, cause);
        }
    }

    /** HTTP 401 - Chưa đăng nhập hoặc token không hợp lệ / hết hạn */
    public static class UnauthorizedException extends AppException {
        public UnauthorizedException(String message) {
            super(HttpServletResponse.SC_UNAUTHORIZED, message);
        }
        public UnauthorizedException(String message, Throwable cause) {
            super(HttpServletResponse.SC_UNAUTHORIZED, message, cause);
        }
    }

    /** HTTP 403 - Đã đăng nhập nhưng không đủ quyền truy cập */
    public static class ForbiddenException extends AppException {
        public ForbiddenException(String message) {
            super(HttpServletResponse.SC_FORBIDDEN, message);
        }
        public ForbiddenException(String message, Throwable cause) {
            super(HttpServletResponse.SC_FORBIDDEN, message, cause);
        }
    }

    /** HTTP 404 - Không tìm thấy tài nguyên / Entity ID tương ứng */
    public static class NotFoundException extends AppException {
        public NotFoundException(String message) {
            super(HttpServletResponse.SC_NOT_FOUND, message);
        }
        public NotFoundException(String message, Throwable cause) {
            super(HttpServletResponse.SC_NOT_FOUND, message, cause);
        }
    }

    /** HTTP 409 - Trùng lặp dữ liệu (số ĐT, username, tên danh mục...) */
    public static class ConflictException extends AppException {
        public ConflictException(String message) {
            super(HttpServletResponse.SC_CONFLICT, message);
        }
        public ConflictException(String message, Throwable cause) {
            super(HttpServletResponse.SC_CONFLICT, message, cause);
        }
    }

    /** HTTP 422 - Vi phạm quy tắc nghiệp vụ (không đủ tồn kho, hóa đơn đã hủy...) */
    public static class BusinessException extends AppException {
        public static final int HTTP_UNPROCESSABLE_ENTITY = 422;

        public BusinessException(String message) {
            super(HTTP_UNPROCESSABLE_ENTITY, message);
        }
        public BusinessException(int statusCode, String message) {
            super(statusCode, message);
        }
        public BusinessException(String message, Throwable cause) {
            super(HTTP_UNPROCESSABLE_ENTITY, message, cause);
        }
    }

    // ==================== FACTORY HELPER METHODS ====================

    public static NotFoundException notFound(String message) {
        return new NotFoundException(message);
    }

    public static ConflictException conflict(String message) {
        return new ConflictException(message);
    }

    public static BadRequestException badRequest(String message) {
        return new BadRequestException(message);
    }

    public static UnauthorizedException unauthorized(String message) {
        return new UnauthorizedException(message);
    }

    public static ForbiddenException forbidden(String message) {
        return new ForbiddenException(message);
    }

    public static BusinessException business(String message) {
        return new BusinessException(message);
    }
}
