package com.mycompany.quanlysieuthi.modules.product;

import com.google.gson.JsonSyntaxException;
import com.mycompany.quanlysieuthi.common.util.RequestUtil;
import com.mycompany.quanlysieuthi.util.NotFoundException;
import com.mycompany.quanlysieuthi.util.ResponseUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Controller handling Product operations:
 * GET  /api/product
 * GET  /api/product/:id
 * PUT  /api/product/:id
 * GET  /api/product/low-stock/alerts
 */
@WebServlet(name = "ProductController", urlPatterns = {"/api/product", "/api/product/*"})
public class ProductController extends HttpServlet {

    private final ProductService productService = new ProductService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/") || pathInfo.isEmpty()) {
            handleGetProducts(req, resp);
        } else if (pathInfo.equals("/low-stock/alerts") || pathInfo.equals("/low-stock/alerts/")) {
            handleGetLowStockAlerts(req, resp);
        } else {
            String id = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
            if (id.endsWith("/")) {
                id = id.substring(0, id.length() - 1);
            }
            if (id.isEmpty()) {
                handleGetProducts(req, resp);
            } else {
                handleGetProductById(id, resp);
            }
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/") || pathInfo.isEmpty()) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Thiếu ID sản phẩm trên URL.");
            return;
        }

        String id = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
        if (id.endsWith("/")) {
            id = id.substring(0, id.length() - 1);
        }
        if (id.isEmpty()) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Thiếu ID sản phẩm trên URL.");
            return;
        }

        handleUpdateProduct(id, req, resp);
    }

    private void handleGetProducts(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            int page = RequestUtil.parsePage(req);
            int limit = RequestUtil.parseLimit(req);
            String keyword = req.getParameter("keyword");
            String categoryId = req.getParameter("categoryId");
            String productStatus = req.getParameter("productStatus");

            List<ProductResponse> products = productService.getProducts(page, limit, keyword, categoryId, productStatus);
            ResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, "Lấy danh sách sản phẩm thành công.", products);

        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi hệ thống khi lấy danh sách sản phẩm: " + e.getMessage());
        }
    }

    private void handleGetProductById(String id, HttpServletResponse resp) throws IOException {
        try {
            ProductResponse product = productService.getProductById(id);
            ResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, "Lấy thông tin sản phẩm thành công.", product);

        } catch (NotFoundException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi hệ thống khi lấy chi tiết sản phẩm: " + e.getMessage());
        }
    }

    private void handleUpdateProduct(String id, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            ProductRequest.Update requestDto = RequestUtil.parseJson(req, ProductRequest.Update.class);
            if (requestDto == null) {
                ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Request body không được để trống.");
                return;
            }

            ProductResponse.UpdateResponseDto updatedProduct = productService.updateProduct(id, requestDto);
            ResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, "Cập nhật sản phẩm thành công.", updatedProduct);

        } catch (JsonSyntaxException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Định dạng JSON không hợp lệ: " + e.getMessage());
        } catch (NotFoundException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi hệ thống khi cập nhật sản phẩm: " + e.getMessage());
        }
    }

    private void handleGetLowStockAlerts(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Integer threshold = null;
            String thresholdStr = req.getParameter("threshold");
            if (thresholdStr != null && !thresholdStr.trim().isEmpty()) {
                threshold = Integer.parseInt(thresholdStr.trim());
            }

            int page = RequestUtil.parsePage(req);
            int limit = RequestUtil.parseLimit(req);
            String categoryId = req.getParameter("categoryId");

            List<ProductResponse.LowStockDto> alerts = productService.getLowStockAlerts(threshold, categoryId, page, limit);
            ResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, "Lấy danh sách cảnh báo tồn kho thành công.", alerts);

        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi hệ thống khi lấy danh sách cảnh báo tồn kho: " + e.getMessage());
        }
    }
}
