package com.mycompany.quanlysieuthi.feedback;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.mycompany.quanlysieuthi.util.NotFoundException;
import com.mycompany.quanlysieuthi.util.ResponseUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
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
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo != null && !pathInfo.isEmpty() && !pathInfo.equals("/")) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found: " + pathInfo);
            return;
        }

        try {
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = req.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            CreateFeedbackRequest request = gson.fromJson(sb.toString(), CreateFeedbackRequest.class);
            CreateFeedbackResponseData result = feedbackService.createFeedback(request);

            ResponseUtil.sendSuccess(resp, HttpServletResponse.SC_CREATED,
                    "Cảm ơn bạn đã gửi đánh giá cho cửa hàng!", result);

        } catch (JsonSyntaxException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "JSON không đúng định dạng: " + e.getMessage());
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

        // Remove leading slash
        String path = pathInfo.startsWith("/") ? pathInfo.substring(1).trim() : pathInfo.trim();
        String[] segments = path.split("/");

        if (segments.length == 1) {
            // GET /api/feedback/:id
            handleGetDetail(segments[0], resp);
        } else {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found: " + pathInfo);
        }
    }

    private void handleGetList(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Integer page = null;
            Integer limit = null;
            Integer rating = null;

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

            FeedbackListResponseData data = feedbackService.getFeedbacks(page, limit, rating, fromDate, toDate);
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
            FeedbackDetailResponse data = feedbackService.getFeedbackDetail(feedbackId);
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
