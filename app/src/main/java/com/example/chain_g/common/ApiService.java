package com.example.chain_g.common;

import com.example.chain_g.auth.dto.request.LoginRequest;
import com.example.chain_g.auth.dto.request.ResetPasswordRequest;
import com.example.chain_g.auth.dto.response.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiService {

    // 로그인
    @POST("/api/v1/auth/login")
    Call<ApiResponse<LoginResponse>> login(@Body LoginRequest request);

    // 비밀번호 재설정
    @POST("/api/v1/auth/reset-password")
    Call<ApiResponse<String>> resetPassword(@Body ResetPasswordRequest request);

    // 토큰 재발급
    @POST("/api/v1/auth/reissue")
    Call<ApiResponse<LoginResponse>> reissue(@Header("Authorization-Refresh") String refreshToken);

    // 로그아웃
    @POST("/api/v1/auth/logout")
    Call<ApiResponse<String>> logout(@Header("Authorization") String token);
}
