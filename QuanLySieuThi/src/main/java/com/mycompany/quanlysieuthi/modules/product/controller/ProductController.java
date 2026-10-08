package com.mycompany.quanlysieuthi.modules.product.controller;

import com.mycompany.quanlysieuthi.modules.product.dto.response.ProductResponseDto;
import com.mycompany.quanlysieuthi.modules.product.service.ProductService;
import com.mycompany.quanlysieuthi.util.ResponseUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Controller handling Product endpoints:
 * GET /api/product (or /api/product/)
 */
@WebServlet(name = "ProductController", urlPatterns = {"/api/product", "/api/product/*"})
public class ProductController extends HttpServlet {

    private final ProductService productService = new ProductService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.isEmpty() || pathInfo.equals("/")) {
            handleGetProducts(req, resp);
            return;
        }

        ResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found: " + pathInfo);
    }

    private void handleGetProducts(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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

            String keyword = req.getParameter("keyword");
            String categoryId = req.getParameter("categoryId");
            String productStatus = req.getParameter("productStatus");

            List<ProductResponseDto> data = productService.getProducts(page, limit, keyword, categoryId, productStatus);
            ResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, "Lấy danh sách sản phẩm thành công", data);

        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi hệ thống khi lấy danh sách sản phẩm: " + e.getMessage());
        }
    }
}
