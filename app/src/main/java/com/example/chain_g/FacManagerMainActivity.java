package com.example.chaing;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chain_g.BoxScanActivity;
import com.example.chain_g.R;

public class FacManagerMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fac_manager_main);

        // 1. 버튼 연결하기
        TextView btnInput = findViewById(R.id.btn_input);   // 입고 버튼
        TextView btnOutput = findViewById(R.id.btn_output); // 출고 버튼

        // 2. [입고] 버튼 눌렀을 때
        btnInput.setOnClickListener(v -> {
            Intent intent = new Intent(FacManagerMainActivity.this, BoxScanActivity.class);
            // ⭐ "입고(IN)" 모드라는 이름표를 붙여서 던지기!
            intent.putExtra("mode", "IN");
            startActivity(intent);
        });

        // 3. [출고] 버튼 눌렀을 때
        btnOutput.setOnClickListener(v -> {
            Intent intent = new Intent(FacManagerMainActivity.this, BoxScanActivity.class);
            // ⭐ "출고(OUT)" 모드라는 이름표를 붙여서 던지기!
            intent.putExtra("mode", "OUT");
            startActivity(intent);
        });

        // 로그아웃 버튼 같은 다른 기능들도 여기에 추가하면 돼!
    }
}