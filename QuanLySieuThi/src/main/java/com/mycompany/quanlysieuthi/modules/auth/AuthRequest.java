package com.mycompany.quanlysieuthi.modules.auth;

import java.io.Serializable;

/**
 * Request payload definitions for Auth module.
 */
public class AuthRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    public record Login(String username, String password) implements Serializable {
        public String getUsername() { return username; }
        public String getPassword() { return password; }
    }
}
