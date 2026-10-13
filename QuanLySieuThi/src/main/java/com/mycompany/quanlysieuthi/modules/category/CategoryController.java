package com.mycompany.quanlysieuthi.modules.category;

import com.google.gson.JsonSyntaxException;
import com.mycompany.quanlysieuthi.common.util.RequestUtil;
import com.mycompany.quanlysieuthi.util.ConflictException;
import com.mycompany.quanlysieuthi.util.NotFoundException;
import com.mycompany.quanlysieuthi.util.ResponseUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Controller handling Category management endpoints:
 * 1. POST   /api/category      - Tạo loại sản phẩm mới (chỉ admin)
 * 2. GET    /api/category      - Lấy danh sách loại sản phẩm (hỗ trợ phân trang, tìm kiếm hoặc query ?all=true)
 * 3. GET    /api/category/:id  - Lấy chi tiết loại sản phẩm
 * 4. PUT    /api/category/:id  - Cập nhật thông tin loại sản phẩm (chỉ admin)
 * 5. DELETE /api/category/:id  - Xóa loại sản phẩm (chỉ admin, nếu chưa có SP liên kết)
 */
@WebServlet(name = "CategoryController", urlPatterns = {"/api/category", "/api/category/*", "/api/categories", "/api/categories/*"})
public class CategoryController extends HttpServlet {

    private final CategoryService categoryService = new CategoryService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            CategoryRequest requestDto = RequestUtil.parseJson(req, CategoryRequest.class);
            if (requestDto == null) {
                ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Request body không được để trống");
                return;
            }

            CategoryResponse responseDto = categoryService.createCategory(requestDto);
            ResponseUtil.sendSuccess(resp, HttpServletResponse.SC_CREATED, "Tạo loại sản phẩm thành công", responseDto);

        } catch (JsonSyntaxException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Malformed JSON payload: " + e.getMessage());
        } catch (ConflictException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_CONFLICT, e.getMessage());
        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi hệ thống khi tạo loại sản phẩm: " + e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String targetId = extractCategoryId(req);

        if (targetId != null) {
            try {
                CategoryResponse category = categoryService.getCategoryById(targetId);
                ResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, "Lấy thông tin loại sản phẩm thành công", category);
            } catch (NotFoundException e) {
                ResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
            } catch (IllegalArgumentException e) {
                ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            } catch (Exception e) {
                ResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Lỗi hệ thống khi lấy thông tin loại sản phẩm: " + e.getMessage());
            }
        } else {
            try {
                String allParam = req.getParameter("all");
                if ("true".equalsIgnoreCase(allParam)) {
                    List<CategoryResponse> list = categoryService.getAllCategories();
                    ResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, "Lấy danh sách tất cả loại sản phẩm thành công", list);
                    return;
                }

                int page = RequestUtil.parsePage(req);
                int limit = RequestUtil.parseLimit(req);
                String keyword = req.getParameter("keyword");

                Map<String, Object> result = categoryService.getCategories(page, limit, keyword);
                ResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, "Lấy danh sách loại sản phẩm thành công",
                        result.get("data"), result.get("meta"));

            } catch (IllegalArgumentException e) {
                ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            } catch (Exception e) {
                ResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Lỗi hệ thống khi lấy danh sách loại sản phẩm: " + e.getMessage());
            }
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String targetId = extractCategoryId(req);
        if (targetId == null) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Thiếu ID loại sản phẩm trên URL.");
            return;
        }

        try {
            CategoryRequest requestDto = RequestUtil.parseJson(req, CategoryRequest.class);
            if (requestDto == null) {
                ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Request body không được để trống");
                return;
            }

            CategoryResponse updated = categoryService.updateCategory(targetId, requestDto);
            ResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, "Cập nhật loại sản phẩm thành công", updated);

        } catch (JsonSyntaxException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Malformed JSON payload: " + e.getMessage());
        } catch (NotFoundException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (ConflictException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_CONFLICT, e.getMessage());
        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi hệ thống khi cập nhật loại sản phẩm: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String targetId = extractCategoryId(req);
        if (targetId == null) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Thiếu ID loại sản phẩm trên URL.");
            return;
        }

        try {
            categoryService.deleteCategory(targetId);
            ResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, "Xóa loại sản phẩm thành công", null);

        } catch (NotFoundException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (ConflictException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_CONFLICT, e.getMessage());
        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi hệ thống khi xóa loại sản phẩm: " + e.getMessage());
        }
    }

    private String extractCategoryId(HttpServletRequest request) {
        String pathInfo = request.getPathInfo();
        if (pathInfo != null && !pathInfo.trim().isEmpty() && !pathInfo.trim().equals("/")) {
            String clean = pathInfo.trim();
            if (clean.startsWith("/")) {
                clean = clean.substring(1);
            }
            if (clean.endsWith("/")) {
                clean = clean.substring(0, clean.length() - 1);
            }
            if (!clean.isEmpty()) {
                return clean;
            }
        }
        String uri = request.getRequestURI();
        if (uri != null) {
            String[] parts = uri.split("/");
            if (parts.length > 0) {
                String last = parts[parts.length - 1];
                if (!last.isEmpty() && !last.equalsIgnoreCase("category") && !last.equalsIgnoreCase("categories")) {
                    return last;
                }
            }
        }
        return null;
    }
}
