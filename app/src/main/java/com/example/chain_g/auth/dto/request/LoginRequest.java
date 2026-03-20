package com.example.chain_g.auth.dto.request;

public class LoginRequest {

    private final String loginId;
    private final String password;

    public LoginRequest(String loginId, String password) {
        this.loginId = loginId;
        this.password = password;
    }

    public String getLoginId() {
        return loginId;
    }

    public String getPassword() {
        return password;
    }
}
