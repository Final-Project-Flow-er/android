package com.example.chain_g;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import okio.Buffer;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chain_g.dto.request.InboundScanItemRequest;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.example.chain_g.common.ApiResponse;
import com.example.chain_g.common.ApiService;
import com.example.chain_g.common.RetrofitClient;
import com.example.chain_g.dto.request.OutboundAssignRequest;
import com.example.chain_g.dto.request.OutboundUpdateRequest;
import com.example.chain_g.dto.response.OutboundItemResponse;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScanActivity extends AppCompatActivity {

    private static final String TAG = "ScanActivity";
    private static final int PERMISSION_REQUEST_CAMERA = 1001;
    private static final int MAX_SCAN_LIMIT = 20;
    private PreviewView previewView;
    private volatile boolean isScanning = true;
    private android.app.ProgressDialog progressDialog;



    private ExecutorService cameraExecutor;
    private List<ProductAdapter.ProductItem> productItems;
    private ProductAdapter adapter;
    private String currentBoxCode;
    private String currentOrderCode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_scan);

        previewView = findViewById(R.id.previewView);
        View mainLayout = findViewById(R.id.main);
        View toolbar = findViewById(R.id.toolbar);

        // 시스템 바 영역 조절
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

        // 박스 코드 설정
        String selectedBoxJson = getIntent().getStringExtra("selected_box");
        TextView tvBoxCode = findViewById(R.id.tv_current_box_code);
        if (selectedBoxJson != null) {
            try {
                JsonObject jsonObject = new JsonParser().parse(selectedBoxJson).getAsJsonObject();
                currentBoxCode = jsonObject.get("boxCode").getAsString();
                currentOrderCode = jsonObject.get("orderCode").getAsString();
                if (tvBoxCode != null) {
                    tvBoxCode.setText(currentBoxCode);
                }
            } catch (Exception e) {
                currentBoxCode = selectedBoxJson;
                if (tvBoxCode != null) {
                    tvBoxCode.setText(currentBoxCode);
                }
            }
        }

        // 리스트 설정
        RecyclerView rvProductList = findViewById(R.id.rv_product_list);
        productItems = new ArrayList<>();
        adapter = new ProductAdapter(productItems);
        rvProductList.setLayoutManager(new LinearLayoutManager(this));
        rvProductList.setAdapter(adapter);

        // 상세 목록 조회
        if (currentBoxCode != null) {
            loadDetailData(currentBoxCode);
        }


        cameraExecutor = Executors.newSingleThreadExecutor();

        // 권한 확인 및 카메라 시작
        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
        }

        // UI 요소 연결
        TextView btnHome = findViewById(R.id.btn_home);
        ImageButton btnBack = findViewById(R.id.btn_back);
        TextView btnAssign = findViewById(R.id.btn_assign);

        // 초기 할당 버튼 상태 설정
        updateAssignButtonState();


        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(ScanActivity.this, FacManagerMainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        btnBack.setOnClickListener(v -> finish());
        btnAssign.setOnClickListener(v -> {
            if (currentBoxCode == null || productItems.isEmpty()) {
                Toast.makeText(this, "할당할 제품이 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            List<String> serialCodes = new ArrayList<>();
            for (ProductAdapter.ProductItem item : productItems) {
                if (item.isScanned) {
                    serialCodes.add(item.identifier);
                }
            }


            OutboundAssignRequest request = new OutboundAssignRequest(currentBoxCode, serialCodes);
            ApiService apiService = RetrofitClient.getApiService(this);
            apiService.assignBox(request).enqueue(new Callback<ApiResponse<Void>>() {
                @Override
                public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(ScanActivity.this, "할당 완료", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(ScanActivity.this, "할당 실패", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                    Log.e(TAG, "할당 요청 오류", t);
                    Toast.makeText(ScanActivity.this, "서버 통신 오류", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void loadDetailData(String boxCode) {
        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.getItemDetail(boxCode).enqueue(new Callback<ApiResponse<List<OutboundItemResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<OutboundItemResponse>>> call, Response<ApiResponse<List<OutboundItemResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<OutboundItemResponse> remoteItems = response.body().getData();
                    if (remoteItems != null) {
                        for (OutboundItemResponse remoteItem : remoteItems) {
                            productItems.add(new ProductAdapter.ProductItem(
                                    remoteItem.getSerialCode(),
                                    "PROD-X", // 백엔드 응답에 제품코드 없음
                                    remoteItem.getProductName(),
                                    remoteItem.getManufactureDate(),
                                    "-"
                            ));
                        }
                        adapter.notifyDataSetChanged();
                        updateAssignButtonState();
                    }
                }

            }

            @Override
            public void onFailure(Call<ApiResponse<List<OutboundItemResponse>>> call, Throwable t) {
                Log.e(TAG, "상세 데이터 로드 오류", t);
            }
        });
    }

    private void updateAssignButtonState() {
        TextView btnAssign = findViewById(R.id.btn_assign);
        if (btnAssign == null) return;

        int scannedCount = 0;
        for (ProductAdapter.ProductItem item : productItems) {
            if (item.isScanned) scannedCount++;
        }

        if (scannedCount == MAX_SCAN_LIMIT) {
            btnAssign.setEnabled(true);
            btnAssign.setAlpha(1.0f);
        } else {
            btnAssign.setEnabled(false);
            btnAssign.setAlpha(0.5f);
        }
    }




    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CAMERA && allPermissionsGranted()) {
            startCamera();
        } else {
            finish();
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, this::scanBarcodes);

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis);

            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "카메라 시작 실패", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @SuppressLint("UnsafeOptInUsageError")
    private void scanBarcodes(ImageProxy imageProxy) {
        if (imageProxy.getImage() == null || !isScanning) {
            imageProxy.close();
            return;
        }

        InputImage image = InputImage.fromMediaImage(imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees());
        BarcodeScanner scanner = BarcodeScanning.getClient();

        scanner.process(image)
                .addOnSuccessListener(barcodes -> {
                    for (Barcode barcode : barcodes) {
                        String rawValue = barcode.getRawValue();
                        if (rawValue != null) {
                            // 중복 스캔 방지: 백그라운드 스레드에서 플래그 확인 후 즉시 변경
                            synchronized (this) {
                                if (isScanning) {
                                    isScanning = false;
                                    Log.d(TAG, "QR 스캔 감지 (중복 방지용 일시정지)");
                                    runOnUiThread(() -> handleScannedQr(rawValue));
                                    break; 
                                }
                            }
                        }
                    }
                })


                .addOnCompleteListener(task -> imageProxy.close());
    }

    private void handleScannedQr(String qrData) {
        Log.d(TAG, "QR 처리 시작: " + qrData);
        
        List<String> extractedCodes = new ArrayList<>();
        try {
            com.google.gson.JsonElement element = new com.google.gson.JsonParser().parse(qrData);
            if (element.isJsonObject()) {
                com.google.gson.JsonObject jsonObject = element.getAsJsonObject();
                
                // serialCodes 키에 대응 (대소문자 구분 없이)
                com.google.gson.JsonArray array = null;
                for (String key : jsonObject.keySet()) {
                    if (key.equalsIgnoreCase("serialCodes")) {
                        array = jsonObject.getAsJsonArray(key);
                        break;
                    }
                }

                if (array != null) {
                    for (int i = 0; i < array.size(); i++) {
                        extractedCodes.add(array.get(i).getAsString().trim().toUpperCase());
                    }
                } else if (jsonObject.has("serialCode")) {
                    extractedCodes.add(jsonObject.get("serialCode").getAsString().trim().toUpperCase());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "QR 파싱 에러: " + e.getMessage());
        }

        if (extractedCodes.isEmpty()) {
            showErrorAndResume("QR 코드에서 유효한 시리얼 코드를 찾을 수 없습니다.");
            return;
        }

        // 여러 개의 코드가 올 수 있지만, 현재 로직상 리스트의 첫 번째 항목을 주 타겟으로 하거나 
        // 리스트 전체를 API로 보냅니다. (사용자 요청에 따라 리스트 형태로 전송)
        final String mainSerialCode = extractedCodes.get(0);
        Log.d(TAG, "최종 추출 시리얼 코드 리스트: " + extractedCodes);


        ProductAdapter.ProductItem existingItem = null;
        for(ProductAdapter.ProductItem item : productItems) {
            if(item.identifier.equalsIgnoreCase(mainSerialCode)) {
                existingItem = item;
                break;
            }
        }

        if (existingItem != null && existingItem.isScanned) {
            Log.d(TAG, "이미 스캔 완료된 항목임: " + mainSerialCode);
            isScanning = true; // 다시 스캔 재개
            return;
        }

        if (existingItem == null && productItems.size() >= MAX_SCAN_LIMIT) {
            showErrorAndResume("최대 " + MAX_SCAN_LIMIT + "개까지만 스캔 가능합니다. (미등록 제품: " + mainSerialCode + ")");
            return;
        }

        showLoading(true);

        OutboundUpdateRequest requestBody = new OutboundUpdateRequest(extractedCodes);

        Log.d(TAG, "API 호출: /api/v1/outbounds/scans | Body: " + new com.google.gson.Gson().toJson(requestBody));


        final ProductAdapter.ProductItem finalItem = existingItem;
        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.scanOutbound(requestBody).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                showLoading(false);
                if (response.isSuccessful()) {
                    Log.d(TAG, "scanOutbound 성공");
                    if (finalItem != null) {
                        finalItem.isScanned = true;
                        adapter.notifyDataSetChanged();
                    } else {
                        // 목록에 없던 제품이면 새로 추가
                        ProductAdapter.ProductItem newItem = new ProductAdapter.ProductItem(mainSerialCode, "PROD-X", "스캔 제품", "2024-05-20", "2025-05-20");
                        newItem.isScanned = true;
                        productItems.add(0, newItem);
                        adapter.notifyItemInserted(0);
                    }
                    
                    updateAssignButtonState();
                    Toast.makeText(ScanActivity.this, "제품 출고 스캔 완료: " + mainSerialCode, Toast.LENGTH_SHORT).show();
                    isScanning = true;
                } else {

                    String errorMsg = "(Error Code: " + response.code() + ")";
                    try {
                        if(response.errorBody() != null) {
                            errorMsg += "\n" + response.errorBody().string();
                        }
                    } catch (Exception e) {}
                    Log.e(TAG, "scanOutbound 에러 응답: " + errorMsg);
                    showErrorAndResume("출고 스캔 실패: " + errorMsg);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "scanOutbound 네트워크 오류", t);
                showErrorAndResume("서버 통신 오류: " + t.getMessage());
            }
        });
    }




    private void showLoading(boolean show) {
        if (show) {
            if (progressDialog == null) {
                progressDialog = new android.app.ProgressDialog(this);
                progressDialog.setMessage("확인 중...");
                progressDialog.setCancelable(false);
            }
            progressDialog.show();
        } else if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void showErrorAndResume(String message) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("알림")
                .setMessage(message)
                .setPositiveButton("확인", (dialog, which) -> {
                    isScanning = true; // 확인 버튼 누르면 스캔 재개
                })
                .setCancelable(false)
                .show();
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}