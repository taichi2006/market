package com.mycompany.quanlysieuthi.modules.product;

import com.mycompany.quanlysieuthi.common.base.BaseController;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Controller handling Product operations:
 * GET  /api/product
 * GET  /api/product/:id
 * PUT  /api/product/:id
 * GET  /api/product/low-stock/alerts
 */
@WebServlet(name = "ProductController", urlPatterns = {"/api/product", "/api/product/*"})
public class ProductController extends BaseController {

    private final ProductService productService = new ProductService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/") || pathInfo.isEmpty()) {
            handleGetProducts(req, resp);
        } else if (pathInfo.equals("/low-stock/alerts") || pathInfo.equals("/low-stock/alerts/")) {
            handleGetLowStockAlerts(req, resp);
        } else {
            String id = extractPathId(req);
            if (id == null || id.isEmpty()) {
                handleGetProducts(req, resp);
            } else {
                handleGetProductById(id, resp);
            }
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String id = extractPathId(req);
        if (id == null || id.isEmpty()) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Thiếu ID sản phẩm trên URL.");
            return;
        }

        handleUpdateProduct(id, req, resp);
    }

    private void handleGetProducts(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handle(resp, () -> {
            int page = parsePage(req);
            int limit = parseLimit(req);
            String keyword = req.getParameter("keyword");
            String categoryId = req.getParameter("categoryId");
            String productStatus = req.getParameter("productStatus");

            return productService.getProducts(page, limit, keyword, categoryId, productStatus);
        }, "Lấy danh sách sản phẩm thành công.");
    }

    private void handleGetProductById(String id, HttpServletResponse resp) throws IOException {
        handle(resp, () -> productService.getProductById(id), "Lấy thông tin sản phẩm thành công.");
    }

    private void handleUpdateProduct(String id, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handle(resp, () -> {
            ProductRequest.Update requestDto = parseBody(req, ProductRequest.Update.class);
            if (requestDto == null) {
                throw new IllegalArgumentException("Request body không được để trống.");
            }
            return productService.updateProduct(id, requestDto);
        }, "Cập nhật sản phẩm thành công.");
    }

    private void handleGetLowStockAlerts(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handle(resp, () -> {
            Integer threshold = null;
            String thresholdStr = req.getParameter("threshold");
            if (thresholdStr != null && !thresholdStr.trim().isEmpty()) {
                threshold = Integer.parseInt(thresholdStr.trim());
            }

            int page = parsePage(req);
            int limit = parseLimit(req);
            String categoryId = req.getParameter("categoryId");

            return productService.getLowStockAlerts(threshold, categoryId, page, limit);
        }, "Lấy danh sách cảnh báo tồn kho thành công.");
    }
}
