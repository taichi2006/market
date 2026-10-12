package com.mycompany.quanlysieuthi.modules.auth;

public class RefreshResponseData {

    private String accessToken;

    public RefreshResponseData() {
    }

    public RefreshResponseData(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
