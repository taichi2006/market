package com.mycompany.quanlysieuthi.feedback;

/**
 * Request DTO for submitting customer feedback:
 * POST /api/feedback
 */
public class CreateFeedbackRequest {

    private String customerName;
    private String phoneNumber;
    private Integer rating;
    private String comment;

    public CreateFeedbackRequest() {
    }

    public CreateFeedbackRequest(String customerName, String phoneNumber, Integer rating, String comment) {
        this.customerName = customerName;
        this.phoneNumber = phoneNumber;
        this.rating = rating;
        this.comment = comment;
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
}
