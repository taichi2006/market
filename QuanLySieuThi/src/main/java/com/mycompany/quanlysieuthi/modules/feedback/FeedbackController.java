package com.mycompany.quanlysieuthi.modules.feedback;

import com.mycompany.quanlysieuthi.common.util.RequestUtil;
import com.mycompany.quanlysieuthi.util.NotFoundException;
import com.mycompany.quanlysieuthi.util.ResponseUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
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
public class FeedbackController extends HttpServlet {

    private final FeedbackService feedbackService = new FeedbackService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo != null && !pathInfo.isEmpty() && !pathInfo.equals("/")) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found: " + pathInfo);
            return;
        }

        try {
            FeedbackRequest.Create request = RequestUtil.parseJsonBody(req, FeedbackRequest.Create.class);
            if (request == null) {
                ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Request body không được để trống");
                return;
            }

            FeedbackResponse.CreateResponse result = feedbackService.createFeedback(request);
            ResponseUtil.sendSuccess(resp, HttpServletResponse.SC_CREATED,
                    "Cảm ơn bạn đã gửi đánh giá cho cửa hàng!", result);

        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi hệ thống khi gửi đánh giá: " + e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.isEmpty() || pathInfo.equals("/")) {
            handleGetList(req, resp);
            return;
        }

        String targetId = RequestUtil.extractPathId(req);
        if (targetId != null) {
            handleGetDetail(targetId, resp);
        } else {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found: " + pathInfo);
        }
    }

    private void handleGetList(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            int page = RequestUtil.parsePage(req);
            int limit = RequestUtil.parseLimit(req);

            Integer rating = null;
            String ratingStr = req.getParameter("rating");
            if (ratingStr != null && !ratingStr.trim().isEmpty()) {
                try {
                    rating = Integer.parseInt(ratingStr.trim());
                } catch (NumberFormatException e) {
                    ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Số sao lọc (rating) không hợp lệ (rating phải từ 1 đến 5).");
                    return;
                }
            }

            String fromDate = req.getParameter("fromDate");
            String toDate = req.getParameter("toDate");

            FeedbackResponse.ListResponse data = feedbackService.getFeedbacks(page, limit, rating, fromDate, toDate);
            ResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, "Lấy danh sách đánh giá thành công", data);

        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi hệ thống khi lấy danh sách đánh giá: " + e.getMessage());
        }
    }

    private void handleGetDetail(String feedbackId, HttpServletResponse resp) throws IOException {
        try {
            FeedbackResponse.Detail data = feedbackService.getFeedbackDetail(feedbackId);
            ResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, "Lấy chi tiết đánh giá thành công", data);
        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (NotFoundException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi hệ thống khi lấy chi tiết đánh giá: " + e.getMessage());
        }
    }
}
