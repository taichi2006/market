package com.mycompany.quanlysieuthi.feedback;

/**
 * Response Data DTO returned after creating a feedback:
 * POST /api/feedback
 */
public class CreateFeedbackResponseData {

    private String feedbackId;
    private Integer rating;
    private String createdAt;

    public CreateFeedbackResponseData() {
    }

    public CreateFeedbackResponseData(String feedbackId, Integer rating, String createdAt) {
        this.feedbackId = feedbackId;
        this.rating = rating;
        this.createdAt = createdAt;
    }

    public String getFeedbackId() {
        return feedbackId;
    }

    public void setFeedbackId(String feedbackId) {
        this.feedbackId = feedbackId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
