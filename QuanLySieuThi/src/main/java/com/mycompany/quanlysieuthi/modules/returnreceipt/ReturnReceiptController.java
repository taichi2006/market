package com.mycompany.quanlysieuthi.modules.returnreceipt;

import com.mycompany.quanlysieuthi.common.base.BaseController;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Controller handling Return Receipt endpoints:
 * POST /api/return-receipt - Tạo phiếu trả hàng
 * GET  /api/return-receipt - Lấy danh sách phiếu trả hàng
 * GET  /api/return-receipt/:id - Lấy chi tiết phiếu trả hàng
 */
@WebServlet(name = "ReturnReceiptController", urlPatterns = {"/api/return-receipt/*", "/api/return-receipt"})
public class ReturnReceiptController extends BaseController {

    private final ReturnReceiptService returnReceiptService = new ReturnReceiptService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String id = extractPathId(req);

        if (id == null || id.isEmpty() || id.equalsIgnoreCase("return-receipt")) {
            handleGetReturnReceiptList(req, resp);
        } else {
            handleGetReturnReceiptDetail(id, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handle(resp, HttpServletResponse.SC_CREATED, () -> {
            ReturnReceiptRequest.Create requestDto = parseBody(req, ReturnReceiptRequest.Create.class);
            if (requestDto == null) {
                throw new IllegalArgumentException("Request body không được để trống");
            }

            String employeeId = getCurrentEmployeeId(req);
            String fullName = getCurrentFullName(req);

            return returnReceiptService.createReturnReceipt(
                    requestDto,
                    employeeId != null ? employeeId : "",
                    fullName != null ? fullName : ""
            );
        }, "Tạo phiếu trả hàng thành công", null);
    }

    private void handleGetReturnReceiptList(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handle(resp, () -> {
            int page = parsePage(req);
            int limit = parseLimit(req);
            String invoiceId = req.getParameter("invoiceId");
            String employeeId = req.getParameter("employeeId");
            String fromDate = req.getParameter("fromDate");
            String toDate = req.getParameter("toDate");

            return returnReceiptService.getReturnReceipts(
                    page, limit, invoiceId, employeeId, fromDate, toDate
            );
        }, "Lấy danh sách phiếu trả hàng thành công");
    }

    private void handleGetReturnReceiptDetail(String id, HttpServletResponse resp) throws IOException {
        handle(resp, () -> returnReceiptService.getReturnReceiptDetail(id), "Lấy chi tiết phiếu trả hàng thành công");
    }
}
