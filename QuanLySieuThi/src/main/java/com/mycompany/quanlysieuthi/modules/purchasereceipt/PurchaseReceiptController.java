package com.mycompany.quanlysieuthi.modules.purchasereceipt;

import com.mycompany.quanlysieuthi.common.base.BaseController;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Controller handling Purchase Receipt endpoints:
 * POST /api/purchase-receipt - Tạo phiếu nhập hàng
 * GET  /api/purchase-receipt - Lấy danh sách phiếu nhập hàng
 * GET  /api/purchase-receipt/:id - Lấy chi tiết phiếu nhập hàng
 */
@WebServlet(name = "PurchaseReceiptController", urlPatterns = {"/api/purchase-receipt/*", "/api/purchase-receipt"})
public class PurchaseReceiptController extends BaseController {

    private final PurchaseReceiptService purchaseReceiptService = new PurchaseReceiptService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String id = extractPathId(req);

        if (id == null || id.isEmpty() || id.equalsIgnoreCase("purchase-receipt")) {
            handleGetPurchaseReceiptList(req, resp);
        } else {
            handleGetPurchaseReceiptDetail(id, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handle(resp, HttpServletResponse.SC_CREATED, () -> {
            PurchaseReceiptRequest requestDto = parseBody(req, PurchaseReceiptRequest.class);
            if (requestDto == null) {
                throw new IllegalArgumentException("Request body không được để trống");
            }

            String employeeId = getCurrentEmployeeId(req);
            String fullName = getCurrentFullName(req);

            return purchaseReceiptService.createPurchaseReceipt(
                    requestDto,
                    employeeId != null ? employeeId : "",
                    fullName != null ? fullName : ""
            );
        }, "Nhập hàng thành công", null);
    }

    private void handleGetPurchaseReceiptList(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handle(resp, () -> {
            int page = parsePage(req);
            int limit = parseLimit(req);
            String supplierName = req.getParameter("supplierName");
            String employeeId = req.getParameter("employeeId");
            String fromDate = req.getParameter("fromDate");
            String toDate = req.getParameter("toDate");

            return purchaseReceiptService.getPurchaseReceipts(
                    page, limit, supplierName, employeeId, fromDate, toDate
            );
        }, "Lấy danh sách phiếu nhập hàng thành công");
    }

    private void handleGetPurchaseReceiptDetail(String id, HttpServletResponse resp) throws IOException {
        handle(resp, () -> purchaseReceiptService.getPurchaseReceiptDetail(id), "Lấy chi tiết phiếu nhập hàng thành công");
    }
}
