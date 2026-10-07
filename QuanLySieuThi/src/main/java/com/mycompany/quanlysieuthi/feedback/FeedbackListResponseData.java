package com.mycompany.quanlysieuthi.feedback;

import java.util.List;

/**
 * Response Data DTO for listing feedbacks:
 * GET /api/feedback
 */
public class FeedbackListResponseData {

    private FeedbackSummaryDto summary;
    private List<FeedbackDetailResponse> items;

    public FeedbackListResponseData() {
    }

    public FeedbackListResponseData(FeedbackSummaryDto summary, List<FeedbackDetailResponse> items) {
        this.summary = summary;
        this.items = items;
    }

    public FeedbackSummaryDto getSummary() {
        return summary;
    }

    public void setSummary(FeedbackSummaryDto summary) {
        this.summary = summary;
    }

    public List<FeedbackDetailResponse> getItems() {
        return items;
    }

    public void setItems(List<FeedbackDetailResponse> items) {
        this.items = items;
    }
}
