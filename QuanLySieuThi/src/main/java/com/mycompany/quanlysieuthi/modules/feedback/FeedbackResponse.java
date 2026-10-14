package com.mycompany.quanlysieuthi.modules.feedback;

import java.io.Serializable;
import java.util.List;

/**
 * Response payload definitions for Feedback module.
 */
public class FeedbackResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    public record Detail(String feedbackId, String customerName, String phoneNumber,
                         Integer rating, String comment, String createdAt) implements Serializable {}

    public record RatingBreakdown(long fiveStar, long fourStar, long threeStar,
                                  long twoStar, long oneStar) implements Serializable {}

    public record Summary(long totalFeedbacks, double averageRating,
                          RatingBreakdown ratingBreakdown) implements Serializable {}

    public record CreateResponse(String feedbackId, String createdAt) implements Serializable {}

    public record ListResponse(Summary summary, List<Detail> feedbacks) implements Serializable {}
}
