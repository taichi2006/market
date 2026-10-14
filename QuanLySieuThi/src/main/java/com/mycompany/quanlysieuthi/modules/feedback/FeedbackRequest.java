package com.mycompany.quanlysieuthi.modules.feedback;

import java.io.Serializable;

/**
 * Request payload definitions for Feedback module.
 */
public class FeedbackRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    public record Create(String customerName, String phoneNumber,
                         Integer rating, String comment) implements Serializable {
        public String getCustomerName() { return customerName; }
        public String getPhoneNumber() { return phoneNumber; }
        public Integer getRating() { return rating; }
        public String getComment() { return comment; }
    }
}
