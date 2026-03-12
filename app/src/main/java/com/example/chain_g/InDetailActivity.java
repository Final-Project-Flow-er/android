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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chain_g.common.ApiResponse;
import com.example.chain_g.common.ApiService;
import com.example.chain_g.common.RetrofitClient;
import com.example.chain_g.dto.response.InboundDetailResponse;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InDetailActivity extends AppCompatActivity {
    private List<ProductAdapter.ProductItem> productItems;
    private ProductAdapter adapter;
    private String currentBoxCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_in_detail);

        productItems = new ArrayList<>();
        adapter = new ProductAdapter(productItems);
        RecyclerView rvList = findViewById(R.id.rv_in_detail_list);
        if (rvList != null) {
            rvList.setLayoutManager(new LinearLayoutManager(this));
            rvList.setAdapter(adapter);
        }

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

        // 전달받은 박스 코드 표시 (JSON 형태 대응)
        String selectedBoxData = getIntent().getStringExtra("selected_box");
        TextView tvBoxCode = findViewById(R.id.tv_current_box_code);
        
        if (selectedBoxData != null) {
            try {
                JsonObject jsonObject = new JsonParser().parse(selectedBoxData).getAsJsonObject();
                currentBoxCode = jsonObject.get("boxCode").getAsString();
            } catch (Exception e) {
                currentBoxCode = selectedBoxData;
            }
            
            if (tvBoxCode != null) {
                tvBoxCode.setText(currentBoxCode);
            }
            
            // API 호출하여 상세 내역 로드
            if (currentBoxCode != null) {
                loadInboundDetails(currentBoxCode);
            }
        }

        ImageButton btnBack = findViewById(R.id.btn_back);
        TextView btnHome = findViewById(R.id.btn_home);

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        if (btnHome != null) btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(InDetailActivity.this, FacManagerMainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void loadInboundDetails(String boxCode) {
        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.getInboundItemDetailsByBoxCode(boxCode).enqueue(new Callback<ApiResponse<List<InboundDetailResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<InboundDetailResponse>>> call, Response<ApiResponse<List<InboundDetailResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<InboundDetailResponse> data = response.body().getData();
                    if (data != null) {
                        productItems.clear();
                        for (InboundDetailResponse item : data) {
                            productItems.add(new ProductAdapter.ProductItem(
                                    item.getSerialCode(),
                                    "PROD-" + item.getProductId(),
                                    item.getProductName(),
                                    item.getManufactureDate(),
                                    "-" // expirationDate는 백엔드 응답에 없음
                            ));

                        }
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<InboundDetailResponse>>> call, Throwable t) {
                android.util.Log.e("InDetailActivity", "API 로드 실패", t);
            }
        });
    }
}