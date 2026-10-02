package com.mycompany.quanlysieuthi.employee;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.mycompany.quanlysieuthi.util.ResponseUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * RESTful API Controller for Employee resource:
 * - GET    /employees       : Danh sách nhân viên
 * - POST   /employees       : Thêm nhân viên mới
 * - GET    /employees/{id}  : Xem chi tiết nhân viên
 * - PATCH  /employees/{id}  : Cập nhật nhân viên
 * - DELETE /employees/{id}  : Vô hiệu hóa nhân viên
 */
@WebServlet(name = "EmployeeController", urlPatterns = {"/api/employees", "/api/employees/*"})
public class EmployeeController extends HttpServlet {

    private final EmployeeService employeeService = new EmployeeService();
    private final Gson gson = new Gson();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("PATCH".equalsIgnoreCase(req.getMethod())) {
            doPatch(req, resp);
        } else {
            super.service(req, resp);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String employeeId = extractIdFromPath(req);

            if (employeeId == null) {
                // GET /employees : Danh sách nhân viên
                List<EmployeeResponse> employees = employeeService.getAllEmployees();
                ResponseUtil.sendJson(resp, HttpServletResponse.SC_OK, employees);
            } else {
                // GET /employees/{id} : Xem chi tiết nhân viên
                EmployeeResponse employee = employeeService.getEmployeeById(employeeId);
                if (employee == null) {
                    ResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND,
                            "Employee not found with ID: " + employeeId);
                } else {
                    ResponseUtil.sendJson(resp, HttpServletResponse.SC_OK, employee);
                }
            }
        } catch (Exception e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Failed to retrieve employee data: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // POST /employees : Thêm nhân viên mới
        try {
            EmployeeRequest requestDto = gson.fromJson(req.getReader(), EmployeeRequest.class);
            if (requestDto == null) {
                ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Request body is empty");
                return;
            }

            EmployeeResponse responseDto = employeeService.createEmployee(requestDto);
            ResponseUtil.sendJson(resp, HttpServletResponse.SC_CREATED, responseDto);
        } catch (JsonSyntaxException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "Malformed JSON payload: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Failed to create employee: " + e.getMessage());
        }
    }

    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // PATCH /employees/{id} : Cập nhật nhân viên
        String employeeId = extractIdFromPath(req);
        if (employeeId == null) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "Employee ID is required in URL path (e.g. /employees/{id})");
            return;
        }

        try {
            EmployeeRequest requestDto = gson.fromJson(req.getReader(), EmployeeRequest.class);
            if (requestDto == null) {
                ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Request body is empty");
                return;
            }

            EmployeeResponse updatedDto = employeeService.updateEmployee(employeeId, requestDto);
            ResponseUtil.sendJson(resp, HttpServletResponse.SC_OK, updatedDto);
        } catch (JsonSyntaxException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "Malformed JSON payload: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            int status = e.getMessage().toLowerCase().contains("not found")
                    ? HttpServletResponse.SC_NOT_FOUND
                    : HttpServletResponse.SC_BAD_REQUEST;
            ResponseUtil.sendError(resp, status, e.getMessage());
        } catch (Exception e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Failed to update employee: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // DELETE /employees/{id} : Vô hiệu hóa nhân viên
        String employeeId = extractIdFromPath(req);
        if (employeeId == null) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "Employee ID is required in URL path (e.g. /employees/{id})");
            return;
        }

        try {
            EmployeeResponse deactivatedDto = employeeService.deactivateEmployee(employeeId);
            ResponseUtil.sendJson(resp, HttpServletResponse.SC_OK, deactivatedDto);
        } catch (IllegalArgumentException e) {
            int status = e.getMessage().toLowerCase().contains("not found")
                    ? HttpServletResponse.SC_NOT_FOUND
                    : HttpServletResponse.SC_BAD_REQUEST;
            ResponseUtil.sendError(resp, status, e.getMessage());
        } catch (Exception e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Failed to deactivate employee: " + e.getMessage());
        }
    }

    private String extractIdFromPath(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.trim().isEmpty() || "/".equals(pathInfo.trim())) {
            return null;
        }
        String cleanPath = pathInfo.trim();
        if (cleanPath.startsWith("/")) {
            cleanPath = cleanPath.substring(1);
        }
        if (cleanPath.endsWith("/")) {
            cleanPath = cleanPath.substring(0, cleanPath.length() - 1);
        }
        return cleanPath.isEmpty() ? null : cleanPath;
    }
}
