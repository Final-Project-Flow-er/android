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

public class ScanActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_scan);

        // 시스템 바 영역 조절 (이건 기본!)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. 뷰 연결하기 (XML에 있는 ID랑 똑같이!)
        TextView btnHome = findViewById(R.id.btn_home);       // CHAIN-G 로고
        ImageButton btnBack = findViewById(R.id.btn_back);    // 오른쪽 위 X 버튼
        TextView btnAssign = findViewById(R.id.btn_assign);   // 아래쪽 '할당' 버튼

        // 2. 홈 버튼(로고) 클릭 -> 메인 화면으로 이동
        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(ScanActivity.this, FacManagerMainActivity.class);
            // 메인으로 갈 때는 위에 쌓인 화면들 다 정리하기!
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        // 3. 닫기 버튼(X) 클릭 -> 이전 화면으로 (BoxScanActivity로 돌아가겠지?)
        btnBack.setOnClickListener(v -> {
            finish();
        });

        // 4. 할당 버튼 클릭 (나중에 기능을 넣겠지만 일단 클릭은 되게!)
        btnAssign.setOnClickListener(v -> {
            // 여기에 할당 완료 로직이 들어갈 거야!
        });
    }
}