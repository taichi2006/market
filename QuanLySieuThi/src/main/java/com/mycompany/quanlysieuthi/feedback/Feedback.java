package com.mycompany.quanlysieuthi.feedback;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * JPA Entity mapping store_feedback table in PostgreSQL.
 */
@Entity
@Table(name = "store_feedback")
public class Feedback implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "feedback_id", length = 36, nullable = false)
    private String feedbackId;

    @Column(name = "customer_name", length = 100)
    private String customerName;

    @Column(name = "phone_number", length = 15)
    private String phoneNumber;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public Feedback() {
    }

    public Feedback(String feedbackId, String customerName, String phoneNumber,
                    Integer rating, String comment, LocalDateTime createdAt) {
        this.feedbackId = feedbackId;
        this.customerName = customerName;
        this.phoneNumber = phoneNumber;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Feedback feedback = (Feedback) o;
        return Objects.equals(feedbackId, feedback.feedbackId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(feedbackId);
    }
}
