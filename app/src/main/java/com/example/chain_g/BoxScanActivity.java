package com.example.chain_g;

import android.content.Intent; // ⭐ 화면 이동을 위해 추가!
import android.os.Bundle;
import android.view.View; // ⭐ 뷰 클릭을 위해 추가!
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class BoxScanActivity extends AppCompatActivity {

    private String currentMode; // 현재 무슨 모드인지 저장할 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_box_scan);

        // 1. 메인에서 보낸 "mode" 이름표 꺼내기
        currentMode = getIntent().getStringExtra("mode");

        // 2. 상단 바 제목 연결 및 설정
        TextView tvTitle = findViewById(R.id.tv_toolbar_title);

        if ("IN".equals(currentMode)) {
            tvTitle.setText("박스 입고 스캔");
        } else if ("OUT".equals(currentMode)) {
            tvTitle.setText("박스 출고 스캔");
        }

        // 3. 닫기 버튼(홈 버튼) 설정 (선택 사항)
        TextView btnHome = findViewById(R.id.btn_home);
        btnHome.setOnClickListener(v -> finish()); // 누르면 메인으로 돌아감!

        /* ⭐ 여기에 리스트 클릭 로직이 들어갈 거야!
           나중에 RecyclerView 어댑터에서 이 메서드를 호출하게 만들면 돼.
        */
    }

    // ⭐ 박스 아이템을 클릭했을 때 호출될 메서드 (미리 만들어두기!)
    public void moveToDetail(String boxCode) {
        if ("IN".equals(currentMode)) {
            // 🚛 입고 모드: 카메라는 없고 리스트만 있는 '입고 상세' 화면으로!
            Intent intent = new Intent(this, InDetailActivity.class);
            intent.putExtra("selected_box", boxCode);
            startActivity(intent);
        }
        else if ("OUT".equals(currentMode)) {
            // 📦 출고 모드: 카메라로 제품을 찍어야 하는 '제품 스캔' 화면으로!
            Intent intent = new Intent(this, ScanActivity.class);
            intent.putExtra("selected_box", boxCode);
            startActivity(intent);
        }
    }
}