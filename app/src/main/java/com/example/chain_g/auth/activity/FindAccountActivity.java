package com.example.chain_g.auth.activity;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.chain_g.R;
import com.example.chain_g.auth.dto.request.ResetPasswordRequest;
import com.example.chain_g.common.ApiResponse;
import com.example.chain_g.common.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FindAccountActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_find_account);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. 뷰 연결
        EditText idEditText = findViewById(R.id.editTextText2); // 아이디 입력창
        EditText emailEditText = findViewById(R.id.editTextText); // 이메일 입력창
        TextView sendButton = findViewById(R.id.button6); // 발송 버튼
        TextView backButton = findViewById(R.id.back_btn); // 뒤로가기 버튼

        // 2. 발송 버튼 클릭 시
        sendButton.setOnClickListener(v -> {
            String loginId = idEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();

            // 유효성 검사
            if (loginId.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "아이디와 이메일을 모두 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            // [마초의 해결책] HQ(Headquarter) 본사 관리자 필터링
            // 소문자로 변환해서 체크하면 HQ, hq 둘 다 막을 수 있습니다.
            if (loginId.toLowerCase().startsWith("hq")) {
                Toast.makeText(this, "접근 권한이 없습니다.", Toast.LENGTH_LONG).show();
                return; // 서버에 요청도 안 보내고 여기서 끝!
            }

            ResetPasswordRequest request = new ResetPasswordRequest(loginId, email);

            // API 호출
            RetrofitClient.getApiService(this).resetPassword(request).enqueue(new Callback<ApiResponse<String>>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse<String>> call, @NonNull Response<ApiResponse<String>> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(FindAccountActivity.this, "재설정 메일이 발송되었습니다.", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        // 서버에서 만약 다른 이유로 403을 줘도 대응 가능하도록 유지
                        if (response.code() == 403) {
                            Toast.makeText(FindAccountActivity.this, "접근 권한이 없습니다. 시스템 관리자에게 문의하세요.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(FindAccountActivity.this, "입력하신 정보가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ApiResponse<String>> call, @NonNull Throwable t) {
                    Toast.makeText(FindAccountActivity.this, "서버 연결 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        // 뒤로가기 버튼
        backButton.setOnClickListener(v -> finish());
    }
}