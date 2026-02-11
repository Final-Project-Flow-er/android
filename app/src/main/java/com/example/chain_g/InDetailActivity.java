package com.example.chain_g;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class InDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_in_detail);

        View mainLayout = findViewById(R.id.main);
        View toolbar = findViewById(R.id.toolbar);

        // 시스템 바 영역 설정 (상태바 겹침 방지)
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            if (toolbar != null) {
                toolbar.setPadding(toolbar.getPaddingLeft(), systemBars.top, toolbar.getPaddingRight(), toolbar.getPaddingBottom());
            }
            WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(getWindow(), v);
            controller.setAppearanceLightStatusBars(false);
            return insets;
        });

        // 전달받은 박스 코드 표시
        String boxCode = getIntent().getStringExtra("selected_box");
        TextView tvBoxCode = findViewById(R.id.tv_current_box_code);
        if (boxCode != null && tvBoxCode != null) {
            tvBoxCode.setText(boxCode);
        }

        ImageButton btnBack = findViewById(R.id.btn_back);
        TextView btnHome = findViewById(R.id.btn_home);

        btnBack.setOnClickListener(v -> finish());
        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(InDetailActivity.this, FacManagerMainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }
}