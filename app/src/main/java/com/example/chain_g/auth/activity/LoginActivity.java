package com.example.chain_g.auth.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.chain_g.FacManagerMainActivity;
import com.example.chain_g.FranManagerMainActivity;
import com.example.chain_g.R;
import com.example.chain_g.auth.dto.request.LoginRequest;
import com.example.chain_g.auth.dto.response.LoginResponse;
import com.example.chain_g.auth.enums.UserRole;
import com.example.chain_g.auth.jwt.TokenManager;
import com.example.chain_g.common.ApiResponse;
import com.example.chain_g.common.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView loginButton = findViewById(R.id.login_btn);
        TextView findAccountButton = findViewById(R.id.find_account_btn);
        EditText idField = findViewById(R.id.id);
        EditText passwordField = findViewById(R.id.password);

        loginButton.setOnClickListener(v -> {
            String inputId = idField.getText().toString().trim();
            String inputPw = passwordField.getText().toString().trim();

            if (inputId.isEmpty() || inputPw.isEmpty()) {
                Toast.makeText(this, "아이디와 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            LoginRequest loginRequest = new LoginRequest(inputId, inputPw);

            RetrofitClient.getApiService().login(loginRequest).enqueue(new Callback<ApiResponse<LoginResponse>>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse<LoginResponse>> call, @NonNull Response<ApiResponse<LoginResponse>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        LoginResponse loginData = response.body().getData();

                        String accessToken = loginData.getAccessToken();
                        String refreshToken = loginData.getRefreshToken();
                        UserRole userRole = loginData.getRole();

                        TokenManager.saveTokens(LoginActivity.this, accessToken, refreshToken);

                        Log.d("LOGIN_API", "로그인 성공!");

                        Intent intent;
                        if (userRole == UserRole.FACTORY) {
                            intent = new Intent(LoginActivity.this, FacManagerMainActivity.class);
                        } else if (userRole == UserRole.FRANCHISE) {
                            intent = new Intent(LoginActivity.this, FranManagerMainActivity.class);
                        } else {
                            Toast.makeText(LoginActivity.this, "접근 권한이 없습니다.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        startActivity(intent);
                        finish();

                    } else {
                        Log.e("LOGIN_API", "실패 코드: " + response.code());
                        Toast.makeText(LoginActivity.this, "로그인 정보가 올바르지 않습니다.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ApiResponse<LoginResponse>> call, @NonNull Throwable t) {
                    Log.e("LOGIN_API", "네트워크 에러: " + t.getMessage());
                    Toast.makeText(LoginActivity.this, "서버 연결에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        });

        findAccountButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, FindAccountActivity.class);
            startActivity(intent);
        });
    }
}