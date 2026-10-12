package com.mycompany.quanlysieuthi.modules.returnreceipt.controller;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.mycompany.quanlysieuthi.modules.returnreceipt.dto.request.ReturnReceiptRequest;
import com.mycompany.quanlysieuthi.modules.returnreceipt.dto.response.ReturnReceiptDetailResponse;
import com.mycompany.quanlysieuthi.modules.returnreceipt.dto.response.ReturnReceiptListItemDto;
import com.mycompany.quanlysieuthi.modules.returnreceipt.dto.response.ReturnReceiptResponse;
import com.mycompany.quanlysieuthi.modules.returnreceipt.service.ReturnReceiptService;
import com.mycompany.quanlysieuthi.util.NotFoundException;
import com.mycompany.quanlysieuthi.util.ResponseUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Controller handling Return Receipt endpoints:
 * POST /api/return-receipt (or /api/return-receipt/)
 * GET /api/return-receipt (or /api/return-receipt/)
 * GET /api/return-receipt/:id
 */
@WebServlet(name = "ReturnReceiptController", urlPatterns = { "/api/return-receipt", "/api/return-receipt/*" })
public class ReturnReceiptController extends HttpServlet {

    private final ReturnReceiptService returnReceiptService = new ReturnReceiptService();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo != null && pathInfo.length() > 1 && !pathInfo.equals("/")) {
            handleGetDetail(pathInfo.substring(1).trim(), resp);
        } else {
            handleGetList(req, resp);
        }
    }

    private void handleGetDetail(String returnReceiptId, HttpServletResponse resp) throws IOException {
        try {
            ReturnReceiptDetailResponse data = returnReceiptService.getReturnReceiptDetail(returnReceiptId);
            ResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, "Lấy chi tiết phiếu trả hàng thành công", data);
        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (NotFoundException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi hệ thống khi lấy chi tiết phiếu trả hàng: " + e.getMessage());
        }
    }

    private void handleGetList(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Integer page = null;
            Integer limit = null;

            String pageStr = req.getParameter("page");
            if (pageStr != null && !pageStr.trim().isEmpty()) {
                try {
                    page = Integer.parseInt(pageStr.trim());
                } catch (NumberFormatException e) {
                    ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Tham số page phải là số nguyên");
                    return;
                }
            }

            String limitStr = req.getParameter("limit");
            if (limitStr != null && !limitStr.trim().isEmpty()) {
                try {
                    limit = Integer.parseInt(limitStr.trim());
                } catch (NumberFormatException e) {
                    ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Tham số limit phải là số nguyên");
                    return;
                }
            }

            String invoiceId = req.getParameter("invoiceId");
            String employeeId = req.getParameter("employeeId");
            String fromDate = req.getParameter("fromDate");
            String toDate = req.getParameter("toDate");

            List<ReturnReceiptListItemDto> data = returnReceiptService.getReturnReceipts(
                    page,
                    limit,
                    invoiceId,
                    employeeId,
                    fromDate,
                    toDate);

            ResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, "Lấy danh sách phiếu trả hàng thành công", data);
        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi hệ thống khi lấy danh sách phiếu trả hàng: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo != null && !pathInfo.isEmpty() && !pathInfo.equals("/")) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found: " + pathInfo);
            return;
        }

        try {
            ReturnReceiptRequest requestDto = gson.fromJson(req.getReader(), ReturnReceiptRequest.class);
            if (requestDto == null) {
                ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Request body không được để trống");
                return;
            }

            // Extract logged-in employee info from token claims (populated by
            // SecurityMiddleware)
            Claims claims = (Claims) req.getAttribute("currentUser");
            String employeeId = claims != null ? claims.get("employeeId", String.class) : "";
            String fullName = claims != null ? claims.get("fullName", String.class) : "";

            ReturnReceiptResponse responseDto = returnReceiptService.createReturnReceipt(
                    requestDto,
                    employeeId,
                    fullName);

            // Return 201 Created with standard format
            ResponseUtil.sendSuccess(resp, HttpServletResponse.SC_CREATED, "Tạo phiếu trả hàng thành công",
                    responseDto);

        } catch (JsonSyntaxException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "Malformed JSON payload: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (NotFoundException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi hệ thống khi tạo phiếu trả hàng: " + e.getMessage());
        }
    }
}
