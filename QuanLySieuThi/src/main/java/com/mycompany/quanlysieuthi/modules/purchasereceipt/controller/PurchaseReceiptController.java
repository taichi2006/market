package com.mycompany.quanlysieuthi.modules.purchasereceipt.controller;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.mycompany.quanlysieuthi.modules.purchasereceipt.dto.request.PurchaseReceiptRequest;
import com.mycompany.quanlysieuthi.modules.purchasereceipt.dto.response.PurchaseReceiptDetailResponse;
import com.mycompany.quanlysieuthi.modules.purchasereceipt.dto.response.PurchaseReceiptListItemDto;
import com.mycompany.quanlysieuthi.modules.purchasereceipt.dto.response.PurchaseReceiptResponse;
import com.mycompany.quanlysieuthi.modules.purchasereceipt.service.PurchaseReceiptService;
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
 * Controller handling purchase receipt endpoints:
 * POST /api/purchase-receipt (or /api/purchase-receipt/)
 * GET  /api/purchase-receipt (or /api/purchase-receipt/)
 * GET  /api/purchase-receipt/:id
 */
@WebServlet(name = "PurchaseReceiptController", urlPatterns = {"/api/purchase-receipt", "/api/purchase-receipt/*"})
public class PurchaseReceiptController extends HttpServlet {

    private final PurchaseReceiptService purchaseReceiptService = new PurchaseReceiptService();
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

    private void handleGetDetail(String voucherId, HttpServletResponse resp) throws IOException {
        try {
            PurchaseReceiptDetailResponse data = purchaseReceiptService.getPurchaseReceiptDetail(voucherId);
            ResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, "Lấy chi tiết phiếu nhập hàng thành công", data);
        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (NotFoundException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi hệ thống khi lấy chi tiết phiếu nhập hàng: " + e.getMessage());
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

            String supplierName = req.getParameter("supplierName");
            String employeeId = req.getParameter("employeeId");
            String fromDate = req.getParameter("fromDate");
            String toDate = req.getParameter("toDate");

            List<PurchaseReceiptListItemDto> data = purchaseReceiptService.getPurchaseReceipts(
                    page,
                    limit,
                    supplierName,
                    employeeId,
                    fromDate,
                    toDate
            );

            ResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, "Lấy danh sách phiếu nhập hàng thành công", data);
        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi hệ thống khi lấy danh sách phiếu nhập hàng: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            PurchaseReceiptRequest requestDto = gson.fromJson(req.getReader(), PurchaseReceiptRequest.class);
            if (requestDto == null) {
                ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Request body không được để trống");
                return;
            }

            // Extract logged-in employee info from token claims (populated by AuthFilter)
            Claims claims = (Claims) req.getAttribute("currentUser");
            String employeeId = claims != null ? claims.get("employeeId", String.class) : "";
            String fullName = claims != null ? claims.get("fullName", String.class) : "";

            PurchaseReceiptResponse responseDto = purchaseReceiptService.createPurchaseReceipt(
                    requestDto,
                    employeeId,
                    fullName
            );

            // Return 201 Created with standard format
            ResponseUtil.sendSuccess(resp, HttpServletResponse.SC_CREATED,
                    "Tạo phiếu nhập hàng và nhập lô sản phẩm thành công", responseDto);

        } catch (JsonSyntaxException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "Malformed JSON payload: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            // 400 Bad Request: Lỗi validation dữ liệu đầu vào
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (NotFoundException e) {
            // 404 Not Found: categoryId không tồn tại
            ResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi hệ thống khi tạo phiếu nhập hàng: " + e.getMessage());
        }
    }
}
