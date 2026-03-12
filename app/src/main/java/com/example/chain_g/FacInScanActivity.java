package com.example.chain_g;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
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

import com.example.chain_g.common.ApiResponse;
import com.example.chain_g.common.ApiService;
import com.example.chain_g.common.RetrofitClient;
import com.example.chain_g.dto.request.InboundScanItemRequest;
import com.example.chain_g.dto.response.InboundDetailResponse;
import com.google.gson.Gson;

import com.google.gson.JsonSyntaxException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FacInScanActivity extends AppCompatActivity {

    private static final String TAG = "FacInScanActivity";
    private static final int PERMISSION_REQUEST_CAMERA = 1001;
    private PreviewView previewView;
    private ExecutorService cameraExecutor;
    private List<ProductAdapter.ProductItem> productItems;
    private ProductAdapter adapter;
    private boolean isScanning = true;
    private android.app.ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_fac_in_scan);

        previewView = findViewById(R.id.previewView);
        View mainLayout = findViewById(R.id.main);
        View toolbar = findViewById(R.id.toolbar);

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

        // 리스트 설정
        RecyclerView rvProductList = findViewById(R.id.rv_product_list);
        productItems = new ArrayList<>();
        adapter = new ProductAdapter(productItems);
        rvProductList.setLayoutManager(new LinearLayoutManager(this));
        rvProductList.setAdapter(adapter);

        loadInitialData();

        cameraExecutor = Executors.newSingleThreadExecutor();


        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
        }

        findViewById(R.id.btn_home).setOnClickListener(v -> finish());
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_input_complete).setOnClickListener(v -> {
            Toast.makeText(this, "입고 처리가 완료되었습니다.", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void loadInitialData() {
        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.getInboundItemDetails().enqueue(new Callback<ApiResponse<List<InboundDetailResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<InboundDetailResponse>>> call, Response<ApiResponse<List<InboundDetailResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<InboundDetailResponse> remoteItems = response.body().getData();
                    if (remoteItems != null) {
                        for (InboundDetailResponse remoteItem : remoteItems) {
                            productItems.add(new ProductAdapter.ProductItem(
                                    remoteItem.getSerialCode(),
                                    remoteItem.getProductCode(),
                                    remoteItem.getProductName(),
                                    remoteItem.getManufactureDate(),
                                    "-" // 유통기한은 백엔드 응답에 없음
                            ));
                        }
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    Toast.makeText(FacInScanActivity.this, "데이터 로드 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<InboundDetailResponse>>> call, Throwable t) {
                Log.e(TAG, "초기 데이터 로드 오류", t);
            }
        });
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
                            runOnUiThread(() -> handleScannedQr(rawValue));
                        }
                    }
                })
                .addOnCompleteListener(task -> imageProxy.close());
    }

    private void handleScannedQr(String qrData) {
        if (!isScanning) return;

        InboundScanItemRequest request;
        try {
            request = new Gson().fromJson(qrData, InboundScanItemRequest.class);
            if(request == null || request.getSerialCode() == null) {
                showErrorAndResume("잘못된 QR 데이터 형식입니다. (시리얼 코드 누락)");
                return;
            }
        } catch (JsonSyntaxException e) {
            Log.e(TAG, "QR JSON 파싱 에러: " + qrData, e);
            showErrorAndResume("QR 코드가 유효한 JSON 형식이 아닙니다.");
            return;
        }

        final String serialCode = request.getSerialCode();

        for(ProductAdapter.ProductItem item : productItems) {
            if(item.identifier.equals(serialCode)) return; 
        }

        // 스캔 시 일시 정지 및 로딩 표시
        isScanning = false;
        showLoading(true);

        ApiService apiService = RetrofitClient.getApiService(this);
        Call<ApiResponse<Void>> call = apiService.scanInboundItems(request);
        call.enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    productItems.add(0, new ProductAdapter.ProductItem(serialCode, "FAC-IN", "입고 제품", "2024-05-20", "2025-05-20"));
                    adapter.notifyItemInserted(0);
                    Toast.makeText(FacInScanActivity.this, "제품 스캔 완료: " + serialCode, Toast.LENGTH_SHORT).show();
                    
                    // 성공 시 바로 재개
                    isScanning = true;
                } else {
                    showErrorAndResume("스캔 처리 실패 (가용 상태 확인 필요)");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "API 통신 오류", t);
                showErrorAndResume("서버 통신 오류가 발생했습니다.");
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