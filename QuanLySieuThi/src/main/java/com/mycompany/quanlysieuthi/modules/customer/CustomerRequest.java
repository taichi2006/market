package com.mycompany.quanlysieuthi.modules.customer;

import java.io.Serializable;

/**
 * Request payload definitions for Customer module.
 */
public class CustomerRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    public static class Create implements Serializable {
        private static final long serialVersionUID = 1L;

        private String fullName;
        private String phoneNumber;
        private String email;

        public Create() {
        }

        public Create(String fullName, String phoneNumber, String email) {
            this.fullName = fullName;
            this.phoneNumber = phoneNumber;
            this.email = email;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}
