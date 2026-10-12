package com.mycompany.quanlysieuthi.modules.returnreceipt;

import com.google.gson.JsonSyntaxException;
import com.mycompany.quanlysieuthi.common.util.RequestUtil;
import com.mycompany.quanlysieuthi.util.ConflictException;
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
 * POST /api/return-receipt - Tạo phiếu trả hàng
 * GET  /api/return-receipt - Lấy danh sách phiếu trả hàng
 * GET  /api/return-receipt/:id - Lấy chi tiết phiếu trả hàng
 */
@WebServlet(name = "ReturnReceiptController", urlPatterns = {"/api/return-receipt/*", "/api/return-receipt"})
public class ReturnReceiptController extends HttpServlet {

    private final ReturnReceiptService returnReceiptService = new ReturnReceiptService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/") || pathInfo.isEmpty()) {
            handleGetReturnReceiptList(req, resp);
        } else {
            String id = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
            if (id.endsWith("/")) {
                id = id.substring(0, id.length() - 1);
            }
            if (id.isEmpty()) {
                handleGetReturnReceiptList(req, resp);
            } else {
                handleGetReturnReceiptDetail(id, resp);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            ReturnReceiptRequest.Create requestDto = RequestUtil.parseJson(req, ReturnReceiptRequest.Create.class);
            if (requestDto == null) {
                ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Request body không được để trống");
                return;
            }

            Claims claims = (Claims) req.getAttribute("currentUser");
            String employeeId = claims != null ? claims.get("employeeId", String.class) : "";
            String fullName = claims != null ? claims.get("fullName", String.class) : "";

            ReturnReceiptResponse responseDto = returnReceiptService.createReturnReceipt(
                    requestDto,
                    employeeId,
                    fullName
            );

            ResponseUtil.sendSuccess(resp, HttpServletResponse.SC_CREATED, "Tạo phiếu trả hàng thành công", responseDto);

        } catch (JsonSyntaxException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Malformed JSON payload: " + e.getMessage());
        } catch (ConflictException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_CONFLICT, e.getMessage());
        } catch (NotFoundException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi hệ thống khi tạo phiếu trả hàng: " + e.getMessage());
        }
    }

    private void handleGetReturnReceiptList(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            int page = RequestUtil.parsePage(req);
            int limit = RequestUtil.parseLimit(req);
            String invoiceId = req.getParameter("invoiceId");
            String employeeId = req.getParameter("employeeId");
            String fromDate = req.getParameter("fromDate");
            String toDate = req.getParameter("toDate");

            List<ReturnReceiptResponse.ListItemDto> data = returnReceiptService.getReturnReceipts(
                    page, limit, invoiceId, employeeId, fromDate, toDate
            );

            ResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, "Lấy danh sách phiếu trả hàng thành công", data);

        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi hệ thống khi lấy danh sách phiếu trả hàng: " + e.getMessage());
        }
    }

    private void handleGetReturnReceiptDetail(String id, HttpServletResponse resp) throws IOException {
        try {
            ReturnReceiptResponse.DetailResponse data = returnReceiptService.getReturnReceiptDetail(id);
            ResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, "Lấy chi tiết phiếu trả hàng thành công", data);

        } catch (NotFoundException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi hệ thống khi lấy chi tiết phiếu trả hàng: " + e.getMessage());
        }
    }
}
