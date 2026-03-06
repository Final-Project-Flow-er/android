package com.example.chain_g.auth.dto.request;

public class ResetPasswordRequest {

    private final String loginId;
    public final String email;

    public ResetPasswordRequest(String loginId, String email) {
        this.loginId = loginId;
        this.email = email;
    }

    public String getLoginId() {
        return loginId;
    }

    public String getEmail() {
        return email;
    }
}
