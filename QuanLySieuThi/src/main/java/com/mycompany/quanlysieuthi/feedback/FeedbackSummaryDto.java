package com.mycompany.quanlysieuthi.feedback;

/**
 * Summary DTO of feedbacks:
 * totalFeedbacks, averageRating, breakdown
 */
public class FeedbackSummaryDto {

    private long totalFeedbacks;
    private double averageRating;
    private FeedbackRatingBreakdownDto breakdown;

    public FeedbackSummaryDto() {
    }

    public FeedbackSummaryDto(long totalFeedbacks, double averageRating, FeedbackRatingBreakdownDto breakdown) {
        this.totalFeedbacks = totalFeedbacks;
        this.averageRating = averageRating;
        this.breakdown = breakdown;
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

    public FeedbackRatingBreakdownDto getBreakdown() {
        return breakdown;
    }

    public void setBreakdown(FeedbackRatingBreakdownDto breakdown) {
        this.breakdown = breakdown;
    }
}
