package com.example.chain_g;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout; // ⭐ 로그아웃 레이아웃을 위해 추가!
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class FacManagerMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fac_manager_main);

        // 1. 버튼 및 레이아웃 연결하기
        TextView btnInput = findViewById(R.id.btn_input);   // 입고 버튼
        TextView btnOutput = findViewById(R.id.btn_output); // 출고 버튼
        LinearLayout layoutLogout = findViewById(R.id.layout_logout); // ⭐ 로그아웃 레이아웃

        // 2. [입고] 버튼 눌렀을 때
        btnInput.setOnClickListener(v -> {
            Intent intent = new Intent(FacManagerMainActivity.this, BoxScanActivity.class);
            intent.putExtra("mode", "IN");
            startActivity(intent);
        });

        // 3. [출고] 버튼 눌렀을 때
        btnOutput.setOnClickListener(v -> {
            Intent intent = new Intent(FacManagerMainActivity.this, BoxScanActivity.class);
            intent.putExtra("mode", "OUT");
            startActivity(intent);
        });

        // 4. [로그아웃] 영역 눌렀을 때 (아이콘+글자 포함)
        layoutLogout.setOnClickListener(v -> {
            // ⭐ 로그인 화면(LoginActivity)으로 이동! (파일 이름이 다르면 수정해줘!)
            Intent intent = new Intent(FacManagerMainActivity.this, LoginActivity.class);

            // ⭐ 중요! 로그인 화면으로 갈 때 이전 화면 기록(스택)을 다 지워야 해!
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
            finish(); // 현재 메인 화면 닫기!
        });
    }
}