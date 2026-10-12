package com.mycompany.quanlysieuthi.modules.purchasereceipt;

import com.google.gson.JsonSyntaxException;
import com.mycompany.quanlysieuthi.common.util.RequestUtil;
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
 * Controller handling Purchase Receipt endpoints:
 * POST /api/purchase-receipt - Tạo phiếu nhập hàng
 * GET  /api/purchase-receipt - Lấy danh sách phiếu nhập hàng
 * GET  /api/purchase-receipt/:id - Lấy chi tiết phiếu nhập hàng
 */
@WebServlet(name = "PurchaseReceiptController", urlPatterns = {"/api/purchase-receipt/*", "/api/purchase-receipt"})
public class PurchaseReceiptController extends HttpServlet {

    private final PurchaseReceiptService purchaseReceiptService = new PurchaseReceiptService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/") || pathInfo.isEmpty()) {
            handleGetPurchaseReceiptList(req, resp);
        } else {
            String id = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
            if (id.endsWith("/")) {
                id = id.substring(0, id.length() - 1);
            }
            if (id.isEmpty()) {
                handleGetPurchaseReceiptList(req, resp);
            } else {
                handleGetPurchaseReceiptDetail(id, resp);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            PurchaseReceiptRequest requestDto = RequestUtil.parseJson(req, PurchaseReceiptRequest.class);
            if (requestDto == null) {
                ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Request body không được để trống");
                return;
            }

            Claims claims = (Claims) req.getAttribute("currentUser");
            String employeeId = claims != null ? claims.get("employeeId", String.class) : "";
            String fullName = claims != null ? claims.get("fullName", String.class) : "";

            PurchaseReceiptResponse responseDto = purchaseReceiptService.createPurchaseReceipt(
                    requestDto,
                    employeeId,
                    fullName
            );

            ResponseUtil.sendSuccess(resp, HttpServletResponse.SC_CREATED, "Nhập hàng thành công", responseDto);

        } catch (JsonSyntaxException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Malformed JSON payload: " + e.getMessage());
        } catch (NotFoundException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi hệ thống khi tạo phiếu nhập hàng: " + e.getMessage());
        }
    }

    private void handleGetPurchaseReceiptList(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            int page = RequestUtil.parsePage(req);
            int limit = RequestUtil.parseLimit(req);
            String supplierName = req.getParameter("supplierName");
            String employeeId = req.getParameter("employeeId");
            String fromDate = req.getParameter("fromDate");
            String toDate = req.getParameter("toDate");

            List<PurchaseReceiptResponse.ListItemDto> data = purchaseReceiptService.getPurchaseReceipts(
                    page, limit, supplierName, employeeId, fromDate, toDate
            );

            ResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, "Lấy danh sách phiếu nhập hàng thành công", data);

        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi hệ thống khi lấy danh sách phiếu nhập: " + e.getMessage());
        }
    }

    private void handleGetPurchaseReceiptDetail(String id, HttpServletResponse resp) throws IOException {
        try {
            PurchaseReceiptResponse.DetailResponse data = purchaseReceiptService.getPurchaseReceiptDetail(id);
            ResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, "Lấy chi tiết phiếu nhập hàng thành công", data);

        } catch (NotFoundException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi hệ thống khi lấy chi tiết phiếu nhập: " + e.getMessage());
        }
    }
}
