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

public class SaleScanActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sale_scan);

        // 시스템 바 영역 설정
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. 버튼들 가져오기
        // (XML에서 CHAIN-G 텍스트뷰에 id를 @+id/btn_home으로 꼭 추가해줘!)
        TextView btnHome = findViewById(R.id.btn_home);
        ImageButton btnBack = findViewById(R.id.btn_back);

        // 2. 홈 버튼 (CHAIN-G 로고) 누르면 가맹점 메인으로!
        if (btnHome != null) {
            btnHome.setOnClickListener(v -> {
                Intent intent = new Intent(SaleScanActivity.this, FranManagerMainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }

        // 3. 뒤로 가기 (X 버튼) 누르면 가맹점 메인으로!
        btnBack.setOnClickListener(v -> {
            // 이 화면은 보통 메인에서 바로 오니까 finish()만 해도 메인으로 돌아가!
            finish();
        });
    }
}