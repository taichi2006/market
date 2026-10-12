package com.mycompany.quanlysieuthi.modules.feedback;

import java.io.Serializable;

/**
 * Request payload definitions for Feedback module.
 */
public class FeedbackRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    public static class Create implements Serializable {
        private static final long serialVersionUID = 1L;

        private String customerName;
        private String phoneNumber;
        private Integer rating;
        private String comment;

        public Create() {
        }

        public Create(String customerName, String phoneNumber, Integer rating, String comment) {
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
}
