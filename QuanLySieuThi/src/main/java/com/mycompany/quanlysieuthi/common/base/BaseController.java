package com.mycompany.quanlysieuthi.common.base;

import com.google.gson.JsonSyntaxException;
import com.mycompany.quanlysieuthi.common.exception.AppException;
import com.mycompany.quanlysieuthi.util.RequestUtil;
import com.mycompany.quanlysieuthi.util.ResponseUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceException;

import java.io.IOException;
import java.time.format.DateTimeParseException;
import java.util.NoSuchElementException;

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
     * - IllegalArgumentException / JsonSyntaxException / IllegalStateException / DateTimeParseException -> 400 BAD REQUEST
     * - EntityNotFoundException / NoResultException / NoSuchElementException -> 404 NOT FOUND
     * - PersistenceException (Foreign Key, Unique constraint...) -> 409 CONFLICT / 422 UNPROCESSABLE ENTITY
     * - SecurityException -> 403 FORBIDDEN
     * - Tuyệt đối không để văng lỗi 500 cho các trường hợp vi phạm ràng buộc dữ liệu.
     */
    protected void execute(HttpServletResponse resp, ControllerAction action) throws IOException {
        try {
            action.execute();
        } catch (AppException e) {
            ResponseUtil.sendError(resp, e.getStatusCode(), e.getMessage());
        } catch (IllegalArgumentException | JsonSyntaxException | IllegalStateException | DateTimeParseException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (SecurityException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        } catch (EntityNotFoundException | NoResultException | NoSuchElementException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND,
                    e.getMessage() != null ? e.getMessage() : "Không tìm thấy dữ liệu yêu cầu.");
        } catch (PersistenceException e) {
            handlePersistenceException(resp, e);
        } catch (NullPointerException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "Dữ liệu gửi lên không hợp lệ hoặc thiếu trường bắt buộc.");
        } catch (Exception e) {
            Throwable root = getRootCause(e);
            if (root instanceof PersistenceException) {
                handlePersistenceException(resp, (PersistenceException) root);
            } else {
                String errorMsg = root.getMessage() != null ? root.getMessage() : e.getMessage();
                ResponseUtil.sendError(resp, 422,
                        "Không thể xử lý yêu cầu: " + (errorMsg != null ? errorMsg : e.getClass().getSimpleName()));
            }
        }
    }

    private void handlePersistenceException(HttpServletResponse resp, PersistenceException e) throws IOException {
        Throwable root = getRootCause(e);
        String msg = root != null && root.getMessage() != null ? root.getMessage() : e.getMessage();
        if (msg == null) {
            msg = "";
        }
        String lower = msg.toLowerCase();

        if (lower.contains("violates foreign key constraint") || lower.contains("foreign key") || lower.contains("khóa ngoại")) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_CONFLICT,
                    "Không thể xóa hoặc thay đổi dữ liệu do có các bản ghi khác trong hệ thống đang liên kết.");
        } else if (lower.contains("violates unique constraint") || lower.contains("duplicate key") || lower.contains("trùng")) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_CONFLICT,
                    "Dữ liệu bị trùng lặp với bản ghi đã tồn tại trong hệ thống.");
        } else if (lower.contains("violates not-null constraint") || lower.contains("not-null")) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "Dữ liệu bắt buộc không được để trống trong cơ sở dữ liệu.");
        } else {
            ResponseUtil.sendError(resp, 422,
                    "Lỗi ràng buộc cơ sở dữ liệu: " + msg);
        }
    }

    private Throwable getRootCause(Throwable t) {
        Throwable curr = t;
        while (curr.getCause() != null && curr.getCause() != curr) {
            curr = curr.getCause();
        }
        return curr;
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
