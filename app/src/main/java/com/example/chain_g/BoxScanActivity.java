package com.example.chain_g;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class BoxScanActivity extends AppCompatActivity {

    private String currentMode; // "IN"(입고) 또는 "OUT"(출고)
    private RecyclerView rvBoxList;
    // private BoxAdapter adapter; // 나중에 만들 어댑터!

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_box_scan);

        // 1. 이름표(Mode) 확인
        currentMode = getIntent().getStringExtra("mode");
        if (currentMode == null) currentMode = "IN"; // 기본값 방어 코드

        // 2. 뷰 연결
        TextView tvTitle = findViewById(R.id.tv_toolbar_title);
        TextView btnHome = findViewById(R.id.btn_home);
        ImageButton btnBack = findViewById(R.id.btn_back);
        rvBoxList = findViewById(R.id.rv_box_list); // XML에 있는 RecyclerView ID

        // 3. 모드에 따른 상단 제목 세팅
        if ("IN".equals(currentMode)) {
            tvTitle.setText("박스 입고 스캔");
        } else {
            tvTitle.setText("박스 출고 스캔");
        }

        // 4. 뒤로 가기 로직 (완벽 구현!)
        btnHome.setOnClickListener(v -> finish());
        btnBack.setOnClickListener(v -> finish());

        // 5. 리스트 설정 (나중에 데이터 연결할 곳)
        rvBoxList.setLayoutManager(new LinearLayoutManager(this));

        /* [여기서 잠깐!]
           지금은 데이터가 없으니까, 나중에 어댑터를 만들어서
           클릭 이벤트가 발생하면 아래 moveToDetail()을 실행하게 연결할 거야!
        */
    }

    /**
     * 리스트의 박스를 클릭했을 때 실행되는 마법의 로직!
     * @param boxCode 클릭한 박스의 번호 (예: BOX-001)
     */
    public void moveToDetail(String boxCode) {
        Intent intent;

        if ("IN".equals(currentMode)) {
            // 🚛 점주용: 입고 상세(리스트만) 화면으로!
            intent = new Intent(this, InDetailActivity.class);
        } else {
            // 📦 관리자용: 제품 스캔(카메라+리스트) 화면으로!
            intent = new Intent(this, ScanActivity.class);
        }

        // 어떤 박스인지, 무슨 모드인지 가방(Intent)에 담아서 보내기
        intent.putExtra("selected_box", boxCode);
        intent.putExtra("mode", currentMode);
        startActivity(intent);
    }
}