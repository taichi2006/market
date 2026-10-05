package com.mycompany.quanlysieuthi.employee;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.mycompany.quanlysieuthi.util.ConflictException;
import com.mycompany.quanlysieuthi.util.ResponseUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Controller handling employee registration endpoint:
 * POST /api/employee (or /api/employee/)
 */
@WebServlet(name = "EmployeeController", urlPatterns = {"/api/employee", "/api/employee/*"})
public class EmployeeController extends HttpServlet {

    private final EmployeeService employeeService = new EmployeeService();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            EmployeeRequest requestDto = gson.fromJson(req.getReader(), EmployeeRequest.class);
            if (requestDto == null) {
                ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Request body không được để trống");
                return;
            }

            EmployeeResponse responseDto = employeeService.createEmployee(requestDto);

            // Return 201 Created with standard format
            ResponseUtil.sendSuccess(resp, HttpServletResponse.SC_CREATED, "Tạo nhân viên thành công", responseDto);

        } catch (JsonSyntaxException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Malformed JSON payload: " + e.getMessage());
        } catch (ConflictException e) {
            // 409 Conflict: username hoặc phone_number đã tồn tại.
            ResponseUtil.sendError(resp, HttpServletResponse.SC_CONFLICT, e.getMessage());
        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi hệ thống khi tạo nhân viên: " + e.getMessage());
        }
    }
}
