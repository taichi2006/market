package com.mycompany.quanlysieuthi.modules.feedback;

import com.mycompany.quanlysieuthi.common.base.BaseController;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Controller handling Feedback endpoints:
 * POST /api/feedback (or /api/feedback/) - Public endpoint (with RateLimitFilter)
 * GET  /api/feedback (or /api/feedback/) - STORE_OWNER only
 * GET  /api/feedback/:id                - STORE_OWNER only
 */
@WebServlet(name = "FeedbackController", urlPatterns = {"/api/feedback", "/api/feedback/*"})
public class FeedbackController extends BaseController {

    private final FeedbackService feedbackService = new FeedbackService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo != null && !pathInfo.isEmpty() && !pathInfo.equals("/")) {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found: " + pathInfo);
            return;
        }

        handle(resp, HttpServletResponse.SC_CREATED, () -> {
            FeedbackRequest.Create request = parseBody(req, FeedbackRequest.Create.class);
            if (request == null) {
                throw new IllegalArgumentException("Request body không được để trống");
            }
            return feedbackService.createFeedback(request);
        }, "Cảm ơn bạn đã gửi đánh giá cho cửa hàng!", null);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.isEmpty() || pathInfo.equals("/")) {
            handleGetList(req, resp);
            return;
        }

        String targetId = extractPathId(req);
        if (targetId != null && !targetId.equalsIgnoreCase("feedback")) {
            handleGetDetail(targetId, resp);
        } else {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found: " + pathInfo);
        }
    }

    private void handleGetList(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handle(resp, () -> {
            int page = parsePage(req);
            int limit = parseLimit(req);

            Integer rating = null;
            String ratingStr = req.getParameter("rating");
            if (ratingStr != null && !ratingStr.trim().isEmpty()) {
                try {
                    rating = Integer.parseInt(ratingStr.trim());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Số sao lọc (rating) không hợp lệ (rating phải từ 1 đến 5).");
                }
            }

            String fromDate = req.getParameter("fromDate");
            String toDate = req.getParameter("toDate");

            return feedbackService.getFeedbacks(page, limit, rating, fromDate, toDate);
        }, "Lấy danh sách đánh giá thành công");
    }

    private void handleGetDetail(String feedbackId, HttpServletResponse resp) throws IOException {
        handle(resp, () -> feedbackService.getFeedbackDetail(feedbackId), "Lấy chi tiết đánh giá thành công");
    }
}
