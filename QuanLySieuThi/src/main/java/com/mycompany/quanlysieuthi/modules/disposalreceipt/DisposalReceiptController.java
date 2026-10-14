package com.mycompany.quanlysieuthi.modules.disposalreceipt;

import com.mycompany.quanlysieuthi.common.base.BaseController;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Controller handling Disposal Receipt endpoints:
 * POST /api/disposal-receipt - Tạo phiếu xuất hủy hàng
 * GET  /api/disposal-receipt - Lấy danh sách phiếu xuất hủy
 * GET  /api/disposal-receipt/:id - Lấy chi tiết phiếu xuất hủy
 */
@WebServlet(name = "DisposalReceiptController", urlPatterns = {"/api/disposal-receipt/*", "/api/disposal-receipt"})
public class DisposalReceiptController extends BaseController {

    private final DisposalReceiptService disposalReceiptService = new DisposalReceiptService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String id = extractPathId(req);

        if (id == null || id.isEmpty() || id.equalsIgnoreCase("disposal-receipt")) {
            handleGetDisposalReceiptList(req, resp);
        } else {
            handleGetDisposalReceiptDetail(id, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handle(resp, HttpServletResponse.SC_CREATED, () -> {
            DisposalReceiptRequest requestDto = parseBody(req, DisposalReceiptRequest.class);
            if (requestDto == null) {
                throw new IllegalArgumentException("Request body không được để trống");
            }

            String employeeId = getCurrentEmployeeId(req);
            String fullName = getCurrentFullName(req);

            return disposalReceiptService.createDisposalReceipt(
                    requestDto,
                    employeeId != null ? employeeId : "",
                    fullName != null ? fullName : ""
            );
        }, "Tạo phiếu hủy kho thành công", null);
    }

    private void handleGetDisposalReceiptList(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handle(resp, () -> {
            int page = parsePage(req);
            int limit = parseLimit(req);
            String employeeId = req.getParameter("employeeId");
            String fromDate = req.getParameter("fromDate");
            String toDate = req.getParameter("toDate");

            return disposalReceiptService.getDisposalReceipts(page, limit, employeeId, fromDate, toDate);
        }, "Lấy danh sách phiếu hủy kho thành công");
    }

    private void handleGetDisposalReceiptDetail(String id, HttpServletResponse resp) throws IOException {
        handle(resp, () -> disposalReceiptService.getDisposalReceiptDetail(id), "Lấy chi tiết phiếu hủy kho thành công");
    }
}
