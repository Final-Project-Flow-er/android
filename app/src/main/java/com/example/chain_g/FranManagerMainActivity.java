package com.example.chain_g;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class FranManagerMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_fran_manager_main);

        // 상단 바 겹침 방지 (기본 세팅!)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. 버튼들 연결하기 (XML ID 확인!)
        TextView btnInput = findViewById(R.id.btn_input);   // 입고
        TextView btnSell = findViewById(R.id.btn_sell);     // 판매
        LinearLayout layoutLogout = findViewById(R.id.layout_logout); // 로그아웃 영역

        // 2. [입고] 버튼 클릭 -> BoxScanActivity로 이동
        btnInput.setOnClickListener(v -> {
            Intent intent = new Intent(FranManagerMainActivity.this, BoxScanActivity.class);
            // 가맹점주 모드라는 걸 알려주기 위해 데이터를 보낼 수도 있어!
            intent.putExtra("type", "FRAN_IN");
            startActivity(intent);
        });

        // 3. [판매] 버튼 클릭 -> SaleScanActivity로 이동
        btnSell.setOnClickListener(v -> {
            Intent intent = new Intent(FranManagerMainActivity.this, SaleScanActivity.class);
            startActivity(intent);
        });

        // 4. [로그아웃] 버튼 클릭 -> 로그인 화면으로 돌아가거나 앱 종료
        layoutLogout.setOnClickListener(v -> {
            // 현재 화면을 닫아서 이전(로그인) 화면으로 보내기!
            finish();
        });
    }
}