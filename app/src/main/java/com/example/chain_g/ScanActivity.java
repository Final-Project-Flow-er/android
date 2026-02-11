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

public class ScanActivity extends AppCompatActivity {

    private static final String TAG = "ScanActivity";
    private static final int PERMISSION_REQUEST_CAMERA = 1001;
    private PreviewView previewView;
    private ExecutorService cameraExecutor;
    private List<ProductAdapter.ProductItem> productItems;
    private ProductAdapter adapter;

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
        String selectedBox = getIntent().getStringExtra("selected_box");
        TextView tvBoxCode = findViewById(R.id.tv_current_box_code);
        if (selectedBox != null && tvBoxCode != null) {
            tvBoxCode.setText(selectedBox);
        }

        // 리스트 설정
        RecyclerView rvProductList = findViewById(R.id.rv_product_list);
        productItems = new ArrayList<>();
        adapter = new ProductAdapter(productItems);
        rvProductList.setLayoutManager(new LinearLayoutManager(this));
        rvProductList.setAdapter(adapter);

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

        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(ScanActivity.this, FacManagerMainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        btnBack.setOnClickListener(v -> finish());
        btnAssign.setOnClickListener(v -> Toast.makeText(this, "할당 완료", Toast.LENGTH_SHORT).show());
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
        if (imageProxy.getImage() == null) return;

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
        for(ProductAdapter.ProductItem item : productItems) {
            if(item.identifier.equals(qrData)) return; // 중복 방지
        }
        // 스캔된 데이터를 리스트에 추가 (더미 데이터와 함께)
        productItems.add(0, new ProductAdapter.ProductItem(qrData, "PROD-X", "스캔 제품", "2024-05-20", "2025-05-20"));
        adapter.notifyItemInserted(0);
        Toast.makeText(this, "제품 스캔 완료: " + qrData, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}