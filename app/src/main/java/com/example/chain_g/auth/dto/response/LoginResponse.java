package com.example.chain_g.auth.dto.response;

import com.example.chain_g.auth.enums.UserRole;

public class LoginResponse {

    private final String accessToken;

    private final String refreshToken;

    private final UserRole userRole;

    public LoginResponse(String accessToken, String refreshToken, UserRole userRole) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.userRole = userRole;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public UserRole getUserRole() {
        return userRole;
    }
}
