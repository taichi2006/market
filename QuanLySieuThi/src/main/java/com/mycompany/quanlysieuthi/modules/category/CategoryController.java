package com.mycompany.quanlysieuthi.modules.category;

import com.mycompany.quanlysieuthi.common.base.BaseController;
import jakarta.servlet.annotation.WebServlet;
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
public class CategoryController extends BaseController {

    private final CategoryService categoryService = new CategoryService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handle(resp, HttpServletResponse.SC_CREATED, () -> {
            CategoryRequest requestDto = parseBody(req, CategoryRequest.class);
            if (requestDto == null) {
                throw new IllegalArgumentException("Request body không được để trống");
            }
            return categoryService.createCategory(requestDto);
        }, "Tạo loại sản phẩm thành công", null);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String targetId = extractPathId(req);

        if (targetId != null && !targetId.equalsIgnoreCase("category") && !targetId.equalsIgnoreCase("categories")) {
            handle(resp, () -> categoryService.getCategoryById(targetId), "Lấy thông tin loại sản phẩm thành công");
        } else {
            String allParam = req.getParameter("all");
            if ("true".equalsIgnoreCase(allParam)) {
                handle(resp, () -> categoryService.getAllCategories(), "Lấy danh sách tất cả loại sản phẩm thành công");
                return;
            }

            execute(resp, () -> {
                int page = parsePage(req);
                int limit = parseLimit(req);
                String keyword = req.getParameter("keyword");

                Map<String, Object> result = categoryService.getCategories(page, limit, keyword);
                sendSuccess(resp, HttpServletResponse.SC_OK, "Lấy danh sách loại sản phẩm thành công",
                        result.get("data"), result.get("meta"));
            });
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String targetId = extractPathId(req);
        if (targetId == null || targetId.equalsIgnoreCase("category") || targetId.equalsIgnoreCase("categories")) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Thiếu ID loại sản phẩm trên URL.");
            return;
        }

        handle(resp, () -> {
            CategoryRequest requestDto = parseBody(req, CategoryRequest.class);
            if (requestDto == null) {
                throw new IllegalArgumentException("Request body không được để trống");
            }
            return categoryService.updateCategory(targetId, requestDto);
        }, "Cập nhật loại sản phẩm thành công");
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String targetId = extractPathId(req);
        if (targetId == null || targetId.equalsIgnoreCase("category") || targetId.equalsIgnoreCase("categories")) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Thiếu ID loại sản phẩm trên URL.");
            return;
        }

        handleVoid(resp, () -> categoryService.deleteCategory(targetId), "Xóa loại sản phẩm thành công");
    }
}
