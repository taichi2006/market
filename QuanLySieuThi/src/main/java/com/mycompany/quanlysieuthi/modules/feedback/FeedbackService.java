package com.mycompany.quanlysieuthi.modules.feedback;

import com.mycompany.quanlysieuthi.entity.Feedback;
import com.mycompany.quanlysieuthi.util.NotFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.mycompany.quanlysieuthi.util.IdGenerator;
import java.util.regex.Pattern;

/**
 * Service handling business logic for Feedback operations.
 */
public class FeedbackService {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9+]{9,15}$");

    private final FeedbackRepository feedbackRepository;

    public FeedbackService() {
        this.feedbackRepository = new FeedbackRepository();
    }

    public FeedbackService(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    public FeedbackResponse.CreateResponse createFeedback(FeedbackRequest.Create request) {
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

        String feedbackId = IdGenerator.nextFeedbackId();
        LocalDateTime now = LocalDateTime.now();

        Feedback feedback = new Feedback(
                feedbackId,
                customerName,
                phoneNumber,
                request.getRating(),
                comment,
                now
        );

        Feedback saved = feedbackRepository.save(feedback);
        String createdAtStr = saved.getCreatedAt() != null ? saved.getCreatedAt().format(ISO_FORMATTER) : now.format(ISO_FORMATTER);

        return new FeedbackResponse.CreateResponse(
                saved.getFeedbackId(),
                createdAtStr
        );
    }

    public FeedbackResponse.ListResponse getFeedbacks(Integer pageParam, Integer limitParam,
                                                      Integer ratingParam, String fromDateStr, String toDateStr) {
        int page = (pageParam != null) ? pageParam : 1;
        int limit = (limitParam != null) ? limitParam : 10;

        if (page <= 0) {
            throw new IllegalArgumentException("Tham sá»‘ page pháº£i lá»›n hÆ¡n 0");
        }
        if (limit <= 0) {
            throw new IllegalArgumentException("Tham sá»‘ limit pháº£i lá»›n hÆ¡n 0");
        }

        if (ratingParam != null && (ratingParam < 1 || ratingParam > 5)) {
            throw new IllegalArgumentException("Sá»‘ sao lá»c (rating) khÃ´ng há»£p lá»‡ (rating pháº£i tá»« 1 Ä‘áº¿n 5).");
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
                throw new IllegalArgumentException("Äá»‹nh dáº¡ng ngÃ y thÃ¡ng khÃ´ng há»£p lá»‡ (YYYY-MM-DD).");
            }
        }

        if (toDateStr != null && !toDateStr.trim().isEmpty()) {
            try {
                toLocalDate = LocalDate.parse(toDateStr.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
                toDate = toLocalDate.atTime(LocalTime.MAX);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Äá»‹nh dáº¡ng ngÃ y thÃ¡ng khÃ´ng há»£p lá»‡ (YYYY-MM-DD).");
            }
        }

        if (fromLocalDate != null && toLocalDate != null && fromLocalDate.isAfter(toLocalDate)) {
            throw new IllegalArgumentException("fromDate khÃ´ng Ä‘Æ°á»£c sau toDate.");
        }

        FeedbackResponse.Summary summary = feedbackRepository.getSummary(fromDate, toDate);
        List<Feedback> feedbacks = feedbackRepository.findFeedbacks(page, limit, ratingParam, fromDate, toDate);

        List<FeedbackResponse.Detail> items = new ArrayList<>();
        for (Feedback f : feedbacks) {
            items.add(mapToDetailResponse(f));
        }

        return new FeedbackResponse.ListResponse(summary, items);
    }

    public FeedbackResponse.Detail getFeedbackDetail(String feedbackId) {
        if (feedbackId == null || feedbackId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID khÃ´ng Ä‘Ãºng Ä‘á»‹nh dáº¡ng UUID.");
        }


        Feedback feedback = feedbackRepository.findById(feedbackId.trim())
                .orElseThrow(() -> new NotFoundException("KhÃ´ng tÃ¬m tháº¥y Ä‘Ã¡nh giÃ¡ vá»›i ID tÆ°Æ¡ng á»©ng."));

        return mapToDetailResponse(feedback);
    }

    private FeedbackResponse.Detail mapToDetailResponse(Feedback f) {
        String createdAtStr = null;
        if (f.getCreatedAt() != null) {
            createdAtStr = f.getCreatedAt().format(ISO_FORMATTER);
        }
        return new FeedbackResponse.Detail(
                f.getFeedbackId(),
                f.getCustomerName(),
                f.getPhoneNumber(),
                f.getRating(),
                f.getComment(),
                createdAtStr
        );
    }
}
