package com.mycompany.quanlysieuthi.modules.employee;

import com.mycompany.quanlysieuthi.common.base.BaseController;
import com.mycompany.quanlysieuthi.common.exception.AppException.*;
import jakarta.servlet.annotation.WebServlet;
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
public class EmployeeController extends BaseController {

    private final EmployeeService employeeService = new EmployeeService();

    /**
     * 1. POST /api/employee - Tạo tài khoản nhân viên.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handle(resp, HttpServletResponse.SC_CREATED, () -> {
            EmployeeRequest requestDto = parseBody(req, EmployeeRequest.class);
            if (requestDto == null) {
                throw new IllegalArgumentException("Request body không được để trống");
            }
            return employeeService.createEmployee(requestDto);
        }, "Tạo nhân viên thành công", null);
    }

    /**
     * 2. GET /api/employee - Lấy danh sách nhân viên
     * 3. GET /api/employee/:id - Lấy chi tiết nhân viên
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo != null && (pathInfo.equals("/cashier-performance") || pathInfo.equals("/cashier-performance/"))) {
            handle(resp, () -> employeeService.getCashierPerformance(req.getParameter("date")), "Lấy báo cáo hiệu suất thu ngân thành công");
            return;
        }

        String targetId = extractPathId(req);

        if (targetId != null && !targetId.equalsIgnoreCase("employee") && !targetId.equalsIgnoreCase("employees")) {
            handle(resp, () -> employeeService.getEmployeeById(targetId), "Lấy chi tiết nhân viên thành công");
        } else {
            execute(resp, () -> {
                int page = parsePage(req);
                int limit = parseLimit(req);

                String keyword = req.getParameter("keyword");
                String position = req.getParameter("position");

                Boolean status = null;
                String statusParam = req.getParameter("status");
                if (statusParam != null && !statusParam.trim().isEmpty()) {
                    status = Boolean.parseBoolean(statusParam.trim());
                }

                EmployeeResponse.PageResult result = employeeService.getEmployees(page, limit, keyword, position, status);
                sendSuccess(resp, HttpServletResponse.SC_OK, "Lấy danh sách nhân viên thành công",
                        result.getData(), result.getMeta());
            });
        }
    }

    /**
     * 4. PUT /api/employee/:id - Cập nhật thông tin nhân viên.
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String targetId = extractPathId(req);
        if (targetId == null || targetId.equalsIgnoreCase("employee") || targetId.equalsIgnoreCase("employees")) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Thiếu ID nhân viên trên URL.");
            return;
        }

        handle(resp, () -> {
            EmployeeRequest requestDto = parseBody(req, EmployeeRequest.class);
            if (requestDto == null) {
                throw new BadRequestException("Request body không được để trống");
            }

            return employeeService.updateEmployee(targetId, requestDto);
        }, "Cập nhật nhân viên thành công");
    }

    /**
     * 5. DELETE /api/employee/:id - Xóa mềm nhân viên (status = false).
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String targetId = extractPathId(req);
        if (targetId == null || targetId.equalsIgnoreCase("employee") || targetId.equalsIgnoreCase("employees")) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Thiếu ID nhân viên trên URL.");
            return;
        }

        handle(resp, () -> {
            String currentUserId = getCurrentEmployeeId(req);
            return employeeService.deleteEmployee(targetId, currentUserId);
        }, "Xóa nhân viên thành công");
    }
}
