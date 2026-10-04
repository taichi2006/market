package com.mycompany.quanlysieuthi.auth;

public class LoginResult {

    private final LoginResponseData responseData;
    private final String refreshToken;

    public LoginResult(LoginResponseData responseData, String refreshToken) {
        this.responseData = responseData;
        this.refreshToken = refreshToken;
    }

    public LoginResponseData getResponseData() {
        return responseData;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}
