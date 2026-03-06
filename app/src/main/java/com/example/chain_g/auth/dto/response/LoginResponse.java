package com.example.chain_g.auth.dto.response;

import com.example.chain_g.auth.enums.UserRole;

public class LoginResponse {

    private final String accessToken;

    private final String refreshToken;

    private final UserRole role;

    public LoginResponse(String accessToken, String refreshToken, UserRole role) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.role = role;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public UserRole getRole() {
        return role;
    }
}
