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

public class BoxScanActivity extends AppCompatActivity {

    private static final String TAG = "BoxScanActivity";
    private static final int PERMISSION_REQUEST_CODE = 1002;
    private String currentMode; 
    private RecyclerView rvBoxList;
    private PreviewView previewView;
    private ExecutorService cameraExecutor;
    private List<BoxAdapter.BoxItem> boxItems;
    private BoxAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_box_scan);

        // 1. 모드 확인 (FacManagerMainActivity에서 보낸 데이터)
        currentMode = getIntent().getStringExtra("mode");
        if (currentMode == null) currentMode = "IN"; 
        Log.d(TAG, "현재 실행 모드: " + currentMode);

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

        TextView tvTitle = findViewById(R.id.tv_toolbar_title);
        TextView btnHome = findViewById(R.id.btn_home);
        ImageButton btnBack = findViewById(R.id.btn_back);
        rvBoxList = findViewById(R.id.rv_box_list);

        if ("IN".equals(currentMode)) {
            tvTitle.setText("박스 입고 스캔");
        } else {
            tvTitle.setText("박스 출고 스캔");
        }

        btnHome.setOnClickListener(v -> finish());
        btnBack.setOnClickListener(v -> finish());

        // 2. 리스트 설정 (항목 클릭 시 moveToDetail 호출)
        boxItems = new ArrayList<>();
        boxItems.add(new BoxAdapter.BoxItem("BOX-TEST-001", "PROD-001", "테스트 박스 상품"));
        
        adapter = new BoxAdapter(boxItems, this::moveToDetail); // 👈 여기서 클릭 시 moveToDetail 실행!
        rvBoxList.setLayoutManager(new LinearLayoutManager(this));
        rvBoxList.setAdapter(adapter);

        cameraExecutor = Executors.newSingleThreadExecutor();

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
        }
    }

    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE && allPermissionsGranted()) {
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
        for(BoxAdapter.BoxItem item : boxItems) {
            if(item.boxCode.equals(qrData)) return; 
        }
        boxItems.add(0, new BoxAdapter.BoxItem(qrData, "SCAN-PROD", "스캔된 박스"));
        adapter.notifyItemInserted(0);
        rvBoxList.scrollToPosition(0);
    }

    // 🚀 모드별 화면 이동 로직 (완전 보강)
    public void moveToDetail(String boxCode) {
        Intent intent;
        if ("OUT".equals(currentMode)) {
            // ⭐ 출고 모드 -> ScanActivity (제품 스캔 화면)로 이동!
            Log.d(TAG, "출고 모드 감지: ScanActivity로 이동합니다.");
            intent = new Intent(BoxScanActivity.this, ScanActivity.class);
        } else {
            // ⭐ 입고 모드 -> InDetailActivity (입고 내역 화면)로 이동!
            Log.d(TAG, "입고 모드 감지: InDetailActivity로 이동합니다.");
            intent = new Intent(BoxScanActivity.this, InDetailActivity.class);
        }

        intent.putExtra("selected_box", boxCode);
        intent.putExtra("mode", currentMode);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}