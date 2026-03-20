package com.example.chain_g.common;

import com.example.chain_g.auth.dto.request.LoginRequest;
import com.example.chain_g.auth.dto.request.ResetPasswordRequest;
import com.example.chain_g.auth.dto.response.LoginResponse;
import com.example.chain_g.dto.response.InboundDetailResponse;
import com.example.chain_g.dto.response.OutboundItemResponse;
import com.example.chain_g.dto.request.OutboundAssignRequest;
import com.example.chain_g.dto.request.OutboundUpdateRequest;
import com.example.chain_g.dto.request.InboundScanBoxRequest;
import com.example.chain_g.dto.request.InboundScanItemRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

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

    // 공장 입고 스캔
    @POST("/api/v1/inbounds/scan/item")
    Call<ApiResponse<Void>> scanInboundItems(@Body InboundScanItemRequest request);

    // 가맹점 입고 스캔
    @POST("/api/v1/inbounds/scan/box")
    Call<ApiResponse<Void>> scanInboundBoxes(@Body InboundScanBoxRequest request);

    // 공장 입고 대기 세부 목록 조회
    @GET("/api/v1/inbounds/items")
    Call<ApiResponse<List<InboundDetailResponse>>> getInboundItemDetails();

    // 특정 박스의 입고 대기 세부 목록 조회
    @GET("/api/v1/inbounds/boxes/{boxCode}")
    Call<ApiResponse<List<InboundDetailResponse>>> getInboundItemDetailsByBoxCode(@Path("boxCode") String boxCode);

    // 박스 할당
    @PATCH("/api/v1/outbounds/assigns")
    Call<ApiResponse<Void>> assignBox(@Body OutboundAssignRequest request);

    // 출고 스캔
    @PATCH("/api/v1/outbounds/scans")
    Call<ApiResponse<Void>> scanOutbound(@Body OutboundUpdateRequest request);

    // 출고 상세 목록 조회
    @GET("/api/v1/outbounds/boxes/items")
    Call<ApiResponse<List<OutboundItemResponse>>> getItemDetail(@Query("boxCode") String boxCode);

    // 가맹점 판매 스캔 리스트 조회
    @GET("/api/v1/franchise/inventory/scanned-sales-list")
    Call<ApiResponse<com.example.chain_g.dto.response.ScannedForSaleResponse>> getScannedSalesList(@Query("serialCodes") List<String> serialCodes);

    // 가맹점 판매 생성
    @POST("/api/v1/franchise/sales")
    Call<ApiResponse<Void>> createSale(@Body com.example.chain_g.dto.request.FranchiseSellRequest request);
}
