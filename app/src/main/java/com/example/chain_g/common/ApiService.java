package com.example.chain_g.common;

import com.example.chain_g.auth.dto.request.LoginRequest;
import com.example.chain_g.auth.dto.response.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    // 로그인
    @POST("/api/v1/auth/login")
    Call<ApiResponse<LoginResponse>> login(@Body LoginRequest request);

    // 비밀번호 재설정

    // 로그아웃
}
