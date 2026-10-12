package com.mycompany.quanlysieuthi.modules.feedback;

import java.io.Serializable;
import java.util.List;

/**
 * Response payload definitions for Feedback module.
 */
public class FeedbackResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    public static class Detail implements Serializable {
        private static final long serialVersionUID = 1L;

        private String feedbackId;
        private String customerName;
        private String phoneNumber;
        private Integer rating;
        private String comment;
        private String createdAt;

        public Detail() {
        }

        public Detail(String feedbackId, String customerName, String phoneNumber,
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

    public static class RatingBreakdown implements Serializable {
        private static final long serialVersionUID = 1L;

        private long fiveStar;
        private long fourStar;
        private long threeStar;
        private long twoStar;
        private long oneStar;

        public RatingBreakdown() {
        }

        public RatingBreakdown(long fiveStar, long fourStar, long threeStar, long twoStar, long oneStar) {
            this.fiveStar = fiveStar;
            this.fourStar = fourStar;
            this.threeStar = threeStar;
            this.twoStar = twoStar;
            this.oneStar = oneStar;
        }

        public long getFiveStar() {
            return fiveStar;
        }

        public void setFiveStar(long fiveStar) {
            this.fiveStar = fiveStar;
        }

        public long getFourStar() {
            return fourStar;
        }

        public void setFourStar(long fourStar) {
            this.fourStar = fourStar;
        }

        public long getThreeStar() {
            return threeStar;
        }

        public void setThreeStar(long threeStar) {
            this.threeStar = threeStar;
        }

        public long getTwoStar() {
            return twoStar;
        }

        public void setTwoStar(long twoStar) {
            this.twoStar = twoStar;
        }

        public long getOneStar() {
            return oneStar;
        }

        public void setOneStar(long oneStar) {
            this.oneStar = oneStar;
        }
    }

    public static class Summary implements Serializable {
        private static final long serialVersionUID = 1L;

        private long totalFeedbacks;
        private double averageRating;
        private RatingBreakdown ratingBreakdown;

        public Summary() {
        }

        public Summary(long totalFeedbacks, double averageRating, RatingBreakdown ratingBreakdown) {
            this.totalFeedbacks = totalFeedbacks;
            this.averageRating = averageRating;
            this.ratingBreakdown = ratingBreakdown;
        }

        public long getTotalFeedbacks() {
            return totalFeedbacks;
        }

        public void setTotalFeedbacks(long totalFeedbacks) {
            this.totalFeedbacks = totalFeedbacks;
        }

        public double getAverageRating() {
            return averageRating;
        }

        public void setAverageRating(double averageRating) {
            this.averageRating = averageRating;
        }

        public RatingBreakdown getRatingBreakdown() {
            return ratingBreakdown;
        }

        public void setRatingBreakdown(RatingBreakdown ratingBreakdown) {
            this.ratingBreakdown = ratingBreakdown;
        }
    }

    public static class CreateResponse implements Serializable {
        private static final long serialVersionUID = 1L;

        private String feedbackId;
        private String createdAt;

        public CreateResponse() {
        }

        public CreateResponse(String feedbackId, String createdAt) {
            this.feedbackId = feedbackId;
            this.createdAt = createdAt;
        }

        public String getFeedbackId() {
            return feedbackId;
        }

        public void setFeedbackId(String feedbackId) {
            this.feedbackId = feedbackId;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }
    }

    public static class ListResponse implements Serializable {
        private static final long serialVersionUID = 1L;

        private Summary summary;
        private List<Detail> feedbacks;

        public ListResponse() {
        }

        public ListResponse(Summary summary, List<Detail> feedbacks) {
            this.summary = summary;
            this.feedbacks = feedbacks;
        }

        public Summary getSummary() {
            return summary;
        }

        public void setSummary(Summary summary) {
            this.summary = summary;
        }

        public List<Detail> getFeedbacks() {
            return feedbacks;
        }

        public void setFeedbacks(List<Detail> feedbacks) {
            this.feedbacks = feedbacks;
        }
    }
}
