package com.mycompany.quanlysieuthi.feedback;

import com.mycompany.quanlysieuthi.util.NotFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Service handling business logic for Feedback operations.
 */
public class FeedbackService {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9+]{9,15}$");

    private final FeedbackDao feedbackDao;

    public FeedbackService() {
        this.feedbackDao = new FeedbackDao();
    }

    public FeedbackService(FeedbackDao feedbackDao) {
        this.feedbackDao = feedbackDao;
    }

    public CreateFeedbackResponseData createFeedback(CreateFeedbackRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Dữ liệu đánh giá không được để trống");
        }

        if (request.getRating() == null) {
            throw new IllegalArgumentException("Thiếu số sao đánh giá (rating).");
        }

        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new IllegalArgumentException("rating không nằm trong khoảng từ 1 đến 5.");
        }

        String phoneNumber = null;
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) {
            phoneNumber = request.getPhoneNumber().trim();
            if (!PHONE_PATTERN.matcher(phoneNumber).matches()) {
                throw new IllegalArgumentException("Số điện thoại không đúng định dạng.");
            }
        }

        String customerName;
        if (request.getCustomerName() == null || request.getCustomerName().trim().isEmpty()) {
            customerName = "Khách ẩn danh";
        } else {
            customerName = request.getCustomerName().trim();
        }

        String comment = (request.getComment() != null && !request.getComment().trim().isEmpty())
                ? request.getComment().trim()
                : null;

        String feedbackId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();

        Feedback feedback = new Feedback(
                feedbackId,
                customerName,
                phoneNumber,
                request.getRating(),
                comment,
                now
        );

        Feedback saved = feedbackDao.save(feedback);
        String createdAtStr = saved.getCreatedAt() != null ? saved.getCreatedAt().format(ISO_FORMATTER) : now.format(ISO_FORMATTER);

        return new CreateFeedbackResponseData(
                saved.getFeedbackId(),
                saved.getRating(),
                createdAtStr
        );
    }

    public FeedbackListResponseData getFeedbacks(Integer pageParam, Integer limitParam,
                                                 Integer ratingParam, String fromDateStr, String toDateStr) {
        int page = (pageParam != null) ? pageParam : 1;
        int limit = (limitParam != null) ? limitParam : 10;

        if (page <= 0) {
            throw new IllegalArgumentException("Tham số page phải lớn hơn 0");
        }
        if (limit <= 0) {
            throw new IllegalArgumentException("Tham số limit phải lớn hơn 0");
        }

        if (ratingParam != null && (ratingParam < 1 || ratingParam > 5)) {
            throw new IllegalArgumentException("Số sao lọc (rating) không hợp lệ (rating phải từ 1 đến 5).");
        }

        LocalDateTime fromDate = null;
        LocalDateTime toDate = null;
        LocalDate fromLocalDate = null;
        LocalDate toLocalDate = null;

        if (fromDateStr != null && !fromDateStr.trim().isEmpty()) {
            try {
                fromLocalDate = LocalDate.parse(fromDateStr.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
                fromDate = fromLocalDate.atStartOfDay();
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Định dạng ngày tháng không hợp lệ (YYYY-MM-DD).");
            }
        }

        if (toDateStr != null && !toDateStr.trim().isEmpty()) {
            try {
                toLocalDate = LocalDate.parse(toDateStr.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
                toDate = toLocalDate.atTime(LocalTime.MAX);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Định dạng ngày tháng không hợp lệ (YYYY-MM-DD).");
            }
        }

        if (fromLocalDate != null && toLocalDate != null && fromLocalDate.isAfter(toLocalDate)) {
            throw new IllegalArgumentException("fromDate không được sau toDate.");
        }

        FeedbackSummaryDto summary = feedbackDao.getSummary(fromDate, toDate);
        List<Feedback> feedbacks = feedbackDao.findFeedbacks(page, limit, ratingParam, fromDate, toDate);

        List<FeedbackDetailResponse> items = new ArrayList<>();
        for (Feedback f : feedbacks) {
            items.add(mapToDetailResponse(f));
        }

        return new FeedbackListResponseData(summary, items);
    }

    public FeedbackDetailResponse getFeedbackDetail(String feedbackId) {
        if (feedbackId == null || feedbackId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID không đúng định dạng UUID.");
        }

        try {
            UUID.fromString(feedbackId.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("ID không đúng định dạng UUID.");
        }

        Feedback feedback = feedbackDao.findById(feedbackId.trim())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy đánh giá với ID tương ứng."));

        return mapToDetailResponse(feedback);
    }

    private FeedbackDetailResponse mapToDetailResponse(Feedback f) {
        String createdAtStr = null;
        if (f.getCreatedAt() != null) {
            createdAtStr = f.getCreatedAt().format(ISO_FORMATTER);
        }
        return new FeedbackDetailResponse(
                f.getFeedbackId(),
                f.getCustomerName(),
                f.getPhoneNumber(),
                f.getRating(),
                f.getComment(),
                createdAtStr
        );
    }
}
