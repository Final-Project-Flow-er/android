package com.example.chain_g;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chain_g.auth.activity.LoginActivity;
import com.example.chain_g.auth.jwt.TokenManager;
import com.example.chain_g.common.ApiResponse;
import com.example.chain_g.common.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FacManagerMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fac_manager_main);

        TextView btnInput = findViewById(R.id.btn_input);
        TextView btnOutput = findViewById(R.id.btn_output);
        LinearLayout layoutLogout = findViewById(R.id.layout_logout);

        // 입고
        btnInput.setOnClickListener(v -> {
            Intent intent = new Intent(FacManagerMainActivity.this, BoxScanActivity.class);
            intent.putExtra("mode", "IN");
            startActivity(intent);
        });

        // 출고
        btnOutput.setOnClickListener(v -> {
            Intent intent = new Intent(FacManagerMainActivity.this, BoxScanActivity.class);
            intent.putExtra("mode", "OUT");
            startActivity(intent);
        });

        // 로그아웃
        layoutLogout.setOnClickListener(v -> {

            String refreshToken = TokenManager.getRefreshToken(this);
            String authHeader = "Bearer " + refreshToken;

            RetrofitClient.getApiService(this).logout(authHeader).enqueue(new Callback<ApiResponse<String>>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse<String>> call, @NonNull Response<ApiResponse<String>> response) {
                    performLocalLogout();
                }

                @Override
                public void onFailure(@NonNull Call<ApiResponse<String>> call, @NonNull Throwable t) {
                    performLocalLogout();
                }
            });
        });
    }

    private void performLocalLogout() {
        TokenManager.clearTokens(this);
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        Toast.makeText(this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
    }
}