package com.mycompany.quanlysieuthi.feedback;

/**
 * Detailed Response DTO for a feedback item:
 * GET /api/feedback/:id
 * and items in GET /api/feedback
 */
public class FeedbackDetailResponse {

    private String feedbackId;
    private String customerName;
    private String phoneNumber;
    private Integer rating;
    private String comment;
    private String createdAt;

    public FeedbackDetailResponse() {
    }

    public FeedbackDetailResponse(String feedbackId, String customerName, String phoneNumber,
                                  Integer rating, String comment, String createdAt) {
        this.feedbackId = feedbackId;
        this.customerName = customerName;
        this.phoneNumber = phoneNumber;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public String getFeedbackId() {
        return feedbackId;
    }

    public void setFeedbackId(String feedbackId) {
        this.feedbackId = feedbackId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
