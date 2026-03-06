package com.example.chain_g;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.chain_g.auth.activity.LoginActivity;
import com.example.chain_g.auth.jwt.TokenManager;

public class FranManagerMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_fran_manager_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView btnInput = findViewById(R.id.btn_input);
        TextView btnSell = findViewById(R.id.btn_sell);
        LinearLayout layoutLogout = findViewById(R.id.layout_logout);

        // 2입고
        btnInput.setOnClickListener(v -> {
            Intent intent = new Intent(FranManagerMainActivity.this, BoxScanActivity.class);
            intent.putExtra("type", "FRAN_IN");
            startActivity(intent);
        });

        // 판매
        btnSell.setOnClickListener(v -> {
            Intent intent = new Intent(FranManagerMainActivity.this, SaleScanActivity.class);
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