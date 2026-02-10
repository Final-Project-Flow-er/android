package com.example.chain_g;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class InDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_in_detail);

        // 상단 바(시스템 바) 영역 겹치지 않게 조절하는 코드!
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. 버튼들 연결하기
        ImageButton btnBack = findViewById(R.id.btn_back); // 뒤로 가기 화살표
        TextView btnHome = findViewById(R.id.btn_home);   // 홈 로고 또는 글자

        // 2. 뒤로 가기 버튼 눌렀을 때 (이전 스캔 화면으로!)
        btnBack.setOnClickListener(v -> {
            finish(); // 현재 상세 화면만 닫고 이전 화면으로 돌아가기
        });

        // 3. 홈 버튼 눌렀을 때 (관리자 메인 화면으로 한방에!)
        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(InDetailActivity.this, FacManagerMainActivity.class);
            // 메인으로 갈 때는 중간에 쌓인 화면들을 싹 정리해주는 게 좋아!
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }
}