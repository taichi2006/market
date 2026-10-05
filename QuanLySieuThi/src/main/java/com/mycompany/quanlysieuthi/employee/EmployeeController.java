package com.mycompany.quanlysieuthi.employee;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.mycompany.quanlysieuthi.security.OwnerRoleFilter;
import com.mycompany.quanlysieuthi.util.ConflictException;
import com.mycompany.quanlysieuthi.util.ResponseUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Controller handling employee management endpoints:
 * 1. POST   /api/employee      - Tạo tài khoản nhân viên
 * 2. GET    /api/employee      - Lấy danh sách nhân viên (phân trang + lọc)
 * 3. GET    /api/employee/:id  - Lấy chi tiết nhân viên
 * 4. PUT    /api/employee/:id  - Cập nhật thông tin nhân viên
 * 5. DELETE /api/employee/:id  - Xóa mềm nhân viên (status = false)
 */
@WebServlet(name = "EmployeeController", urlPatterns = {"/api/employee", "/api/employee/*", "/api/employees", "/api/employees/*"})
public class EmployeeController extends HttpServlet {

    private final EmployeeService employeeService = new EmployeeService();
    private final Gson gson = new Gson();

    /**
     * 1. POST /api/employee
     * Tạo tài khoản nhân viên.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            EmployeeRequest requestDto = gson.fromJson(req.getReader(), EmployeeRequest.class);
            if (requestDto == null) {
                ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Request body không được để trống");
                return;
            }

            EmployeeResponse responseDto = employeeService.createEmployee(requestDto);
            ResponseUtil.sendSuccess(resp, HttpServletResponse.SC_CREATED, "Tạo nhân viên thành công", responseDto);

        } catch (JsonSyntaxException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Malformed JSON payload: " + e.getMessage());
        } catch (ConflictException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_CONFLICT, e.getMessage());
        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi hệ thống khi tạo nhân viên: " + e.getMessage());
        }
    }

    /**
     * 2. GET /api/employee - Lấy danh sách nhân viên
     * 3. GET /api/employee/:id - Lấy chi tiết nhân viên
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String targetId = OwnerRoleFilter.extractTargetId(req);

        if (targetId != null) {
            // Endpoint 3: GET /api/employee/:id
            try {
                EmployeeResponse employee = employeeService.getEmployeeById(targetId);
                if (employee == null) {
                    ResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy nhân viên với ID tương ứng.");
                    return;
                }
                ResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, "Lấy chi tiết nhân viên thành công", employee);
            } catch (IllegalArgumentException e) {
                ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            } catch (Exception e) {
                ResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Lỗi hệ thống khi lấy chi tiết nhân viên: " + e.getMessage());
            }
        } else {
            // Endpoint 2: GET /api/employee
            try {
                Integer page = null;
                Integer limit = null;

                String pageParam = req.getParameter("page");
                if (pageParam != null && !pageParam.trim().isEmpty()) {
                    try {
                        page = Integer.parseInt(pageParam.trim());
                    } catch (NumberFormatException e) {
                        ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Tham số page không hợp lệ.");
                        return;
                    }
                }

                String limitParam = req.getParameter("limit");
                if (limitParam != null && !limitParam.trim().isEmpty()) {
                    try {
                        limit = Integer.parseInt(limitParam.trim());
                    } catch (NumberFormatException e) {
                        ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Tham số limit không hợp lệ.");
                        return;
                    }
                }

                String keyword = req.getParameter("keyword");
                String position = req.getParameter("position");

                Boolean status = null;
                String statusParam = req.getParameter("status");
                if (statusParam != null && !statusParam.trim().isEmpty()) {
                    status = Boolean.parseBoolean(statusParam.trim());
                }

                EmployeePageResult result = employeeService.getEmployees(page, limit, keyword, position, status);
                ResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, "Lấy danh sách nhân viên thành công",
                        result.getData(), result.getMeta());

            } catch (IllegalArgumentException e) {
                ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            } catch (Exception e) {
                ResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Lỗi hệ thống khi lấy danh sách nhân viên: " + e.getMessage());
            }
        }
    }

    /**
     * 4. PUT /api/employee/:id
     * Cập nhật thông tin nhân viên.
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String targetId = OwnerRoleFilter.extractTargetId(req);
        if (targetId == null) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Thiếu ID nhân viên trên URL.");
            return;
        }

        try {
            EmployeeRequest requestDto = gson.fromJson(req.getReader(), EmployeeRequest.class);
            if (requestDto == null) {
                ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Request body không được để trống");
                return;
            }

            EmployeeResponse updated = employeeService.updateEmployee(targetId, requestDto);
            if (updated == null) {
                ResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy nhân viên với ID tương ứng.");
                return;
            }

            ResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, "Cập nhật nhân viên thành công", updated);

        } catch (JsonSyntaxException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Malformed JSON payload: " + e.getMessage());
        } catch (ConflictException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_CONFLICT, e.getMessage());
        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi hệ thống khi cập nhật nhân viên: " + e.getMessage());
        }
    }

    /**
     * 5. DELETE /api/employee/:id
     * Xóa mềm nhân viên (status = false).
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String targetId = OwnerRoleFilter.extractTargetId(req);
        if (targetId == null) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Thiếu ID nhân viên trên URL.");
            return;
        }

        try {
            Claims claims = (Claims) req.getAttribute("currentUser");
            String currentUserId = (claims != null) ? claims.get("employeeId", String.class) : null;

            EmployeeDeleteResponse result = employeeService.deleteEmployee(targetId, currentUserId);
            if (result == null) {
                ResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy nhân viên với ID tương ứng.");
                return;
            }

            ResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, "Xóa nhân viên thành công", result);

        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi hệ thống khi xóa nhân viên: " + e.getMessage());
        }
    }
}
