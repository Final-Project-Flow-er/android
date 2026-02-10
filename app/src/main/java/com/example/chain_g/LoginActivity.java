package com.example.chain_g;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView loginButton = findViewById(R.id.login_btn);
        TextView findAccountButton = findViewById(R.id.find_account_btn);

        EditText id = findViewById(R.id.id);
        EditText password = findViewById(R.id.password);

        loginButton.setOnClickListener(v -> {

            if(id.equals("fac")) {
                Intent intent = new Intent(LoginActivity.this, FacManagerMainActivity.class);
                startActivity(intent);
            } else if (id.equals("fran")) {
                Intent intent = new Intent(LoginActivity.this, FranManagerMainActivity.class);
                startActivity(intent);
            }
        });

        findAccountButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, FindAccountActivity.class);
            startActivity(intent);
        });
    }
}