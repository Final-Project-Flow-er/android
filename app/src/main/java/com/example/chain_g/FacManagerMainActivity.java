package com.example.chain_g;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chain_g.auth.activity.LoginActivity;
import com.example.chain_g.auth.jwt.TokenManager;

public class FacManagerMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fac_manager_main);

        TextView btnInput = findViewById(R.id.btn_input);
        TextView btnOutput = findViewById(R.id.btn_output);
        LinearLayout layoutLogout = findViewById(R.id.layout_logout);

        // 입고
        btnInput.setOnClickListener(v -> {
            Intent intent = new Intent(FacManagerMainActivity.this, BoxScanActivity.class);
            intent.putExtra("mode", "IN");
            startActivity(intent);
        });

        // 출고
        btnOutput.setOnClickListener(v -> {
            Intent intent = new Intent(FacManagerMainActivity.this, BoxScanActivity.class);
            intent.putExtra("mode", "OUT");
            startActivity(intent);
        });

        // 로그아웃
        layoutLogout.setOnClickListener(v -> {

            TokenManager.clearTokens(this);

            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            Toast.makeText(this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
        });
    }
}