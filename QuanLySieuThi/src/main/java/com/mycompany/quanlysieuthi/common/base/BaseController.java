package com.mycompany.quanlysieuthi.common.base;

import com.google.gson.JsonSyntaxException;
import com.mycompany.quanlysieuthi.common.exception.AppException;
import com.mycompany.quanlysieuthi.util.RequestUtil;
import com.mycompany.quanlysieuthi.util.ResponseUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Base Controller cung cấp các tiện ích xử lý request, response, trích xuất dữ liệu,
 * xác thực người dùng và cơ chế bắt ngoại lệ tập trung (Centralized Exception Handling).
 */
public abstract class BaseController extends HttpServlet {

    @FunctionalInterface
    public interface ControllerAction {
        void execute() throws Exception;
    }

    @FunctionalInterface
    public interface ControllerSupplier<R> {
        R get() throws Exception;
    }

    // ==================== CƠ CHẾ BẮT LỖI TẬP TRUNG (EXCEPTION HANDLING) ====================

    /**
     * Bọc khối thực thi và tự động phân loại xử lý exception chuẩn hóa theo mã HTTP:
     * - AppException (NotFound, Conflict, BadRequest, Unauthorized, Forbidden, Business...) -> mã HTTP tương ứng
     * - IllegalArgumentException / JsonSyntaxException / IllegalStateException -> 400 BAD REQUEST
     * - SecurityException -> 403 FORBIDDEN
     * - Exception khác -> 500 INTERNAL SERVER ERROR
     */
    protected void execute(HttpServletResponse resp, ControllerAction action) throws IOException {
        try {
            action.execute();
        } catch (AppException e) {
            ResponseUtil.sendError(resp, e.getStatusCode(), e.getMessage());
        } catch (IllegalArgumentException | JsonSyntaxException | IllegalStateException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (SecurityException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi hệ thống: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
        }
    }

    /**
     * Tự động bắt lỗi, nếu thành công sẽ gửi Response 200 OK kèm thông điệp và dữ liệu trả về.
     */
    protected <R> void handle(HttpServletResponse resp, ControllerSupplier<R> supplier, String successMessage) throws IOException {
        handle(resp, HttpServletResponse.SC_OK, supplier, successMessage, null);
    }

    /**
     * Tự động bắt lỗi và gửi Response với mã HTTP tùy chọn (ví dụ 201 CREATED) kèm metadata.
     */
    protected <R> void handle(HttpServletResponse resp, int statusCode, ControllerSupplier<R> supplier,
                              String successMessage, Object meta) throws IOException {
        execute(resp, () -> {
            R result = supplier.get();
            ResponseUtil.sendSuccess(resp, statusCode, successMessage, result, meta);
        });
    }

    /**
     * Tự động bắt lỗi cho thao tác void (như Delete, Update không trả về nội dung lớn).
     */
    protected void handleVoid(HttpServletResponse resp, ControllerAction action, String successMessage) throws IOException {
        execute(resp, () -> {
            action.execute();
            ResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, successMessage, null);
        });
    }

    // ==================== TIỆN ÍCH TRÍCH XUẤT REQUEST ====================

    /**
     * Lấy ID từ Path URL (ví dụ: /api/product/SP001 -> SP001).
     */
    protected String extractPathId(HttpServletRequest req) {
        return RequestUtil.extractPathId(req);
    }

    /**
     * Đọc tham số page từ query string (mặc định 1 nếu không truyền).
     */
    protected int parsePage(HttpServletRequest req) {
        return RequestUtil.parsePage(req);
    }

    /**
     * Đọc tham số limit từ query string (mặc định 10 nếu không truyền).
     */
    protected int parseLimit(HttpServletRequest req) {
        return RequestUtil.parseLimit(req);
    }

    /**
     * Đọc và parse request body JSON sang DTO class.
     */
    protected <T> T parseBody(HttpServletRequest req, Class<T> clazz) throws IOException {
        return RequestUtil.parseJson(req, clazz);
    }

    /**
     * Lấy địa chỉ IP thực của client.
     */
    protected String getClientIp(HttpServletRequest req) {
        return RequestUtil.extractClientIp(req);
    }

    // ==================== TIỆN ÍCH THÔNG TIN NGƯỜI DÙNG HIỆN TẠI (JWT) ====================

    /**
     * Lấy đối tượng Claims đã được SecurityMiddleware xác thực và gán vào request attribute.
     */
    protected Claims getCurrentUser(HttpServletRequest req) {
        Object userAttr = req.getAttribute("currentUser");
        if (userAttr instanceof Claims) {
            return (Claims) userAttr;
        }
        return null;
    }

    /**
     * Lấy Employee ID của nhân viên đang đăng nhập.
     */
    protected String getCurrentEmployeeId(HttpServletRequest req) {
        Claims claims = getCurrentUser(req);
        return claims != null ? claims.get("employeeId", String.class) : null;
    }

    /**
     * Lấy vị trí / chức vụ của người dùng đang đăng nhập (ví dụ STORE_OWNER, CASHIER, INVENTORY_MANAGER).
     */
    protected String getCurrentPosition(HttpServletRequest req) {
        Claims claims = getCurrentUser(req);
        return claims != null ? claims.get("position", String.class) : null;
    }

    /**
     * Lấy họ tên đầy đủ của nhân viên đang đăng nhập.
     */
    protected String getCurrentFullName(HttpServletRequest req) {
        Claims claims = getCurrentUser(req);
        return claims != null ? claims.get("fullName", String.class) : null;
    }

    // ==================== TIỆN ÍCH GỬI RESPONSE ====================

    protected void sendSuccess(HttpServletResponse resp, String message, Object data) throws IOException {
        ResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, message, data);
    }

    protected void sendSuccess(HttpServletResponse resp, int statusCode, String message, Object data, Object meta) throws IOException {
        ResponseUtil.sendSuccess(resp, statusCode, message, data, meta);
    }

    protected void sendError(HttpServletResponse resp, int statusCode, String message) throws IOException {
        ResponseUtil.sendError(resp, statusCode, message);
    }
}
