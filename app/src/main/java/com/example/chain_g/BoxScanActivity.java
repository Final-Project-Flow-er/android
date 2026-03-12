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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chain_g.common.ApiResponse;
import com.example.chain_g.common.ApiService;
import com.example.chain_g.common.RetrofitClient;
import com.example.chain_g.dto.request.InboundScanBoxRequest;
import com.example.chain_g.viewmodel.ScanViewModel;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.google.common.util.concurrent.ListenableFuture;

import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.ArrayList;
import java.util.Arrays;
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
    private ScanViewModel viewModel;
    private volatile boolean isScanning = true;
    private android.app.ProgressDialog progressDialog;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_box_scan);

        viewModel = new ViewModelProvider(this).get(ScanViewModel.class);


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
                            synchronized (this) {
                                if (isScanning) {
                                    isScanning = false;
                                    Log.d(TAG, "QR 스캔 감지: " + rawValue);
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
        Log.d(TAG, "QR 처리 시작 (" + currentMode + "): " + qrData);

        if ("IN".equals(currentMode)) {
            // 가맹점 입고(IN) 모드: 이제 서버는 boxCode 하나만 받음 (나머지는 서버에서 자동 처리)
            String boxCode = null;
            try {
                // 1. JSON 형식 시도
                JsonObject jsonObject = new JsonParser().parse(qrData).getAsJsonObject();
                if (jsonObject.has("boxCode")) {
                    boxCode = jsonObject.get("boxCode").getAsString();
                } else if (jsonObject.has("code")) {
                    boxCode = jsonObject.get("code").getAsString();
                }
            } catch (Exception e) {
                // 2. JSON이 아니면 그냥 raw string을 코드로 간주 (유연성)
                boxCode = qrData;
            }

            if (boxCode == null || boxCode.trim().isEmpty()) {
                showErrorAndResume("유효하지 않은 박스 데이터입니다.");
                return;
            }

            final String finalBoxCode = boxCode.trim().toUpperCase();
            
            // 중복 스캔 체크
            for(BoxAdapter.BoxItem item : boxItems) {
                if(item.boxCode.equalsIgnoreCase(finalBoxCode)) {
                    isScanning = true;
                    return;
                }
            }

            // 바뀐 DTO 규격: boxCode만 전송
            InboundScanBoxRequest request = new InboundScanBoxRequest(finalBoxCode);

            showLoading(true);
            ApiService apiService = RetrofitClient.getApiService(this);
            Log.d(TAG, "가맹점 입고 API 호출 (Simplified): " + new Gson().toJson(request));
            
            apiService.scanInboundBoxes(request).enqueue(new Callback<ApiResponse<Void>>() {
                @Override
                public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                    showLoading(false);
                    if (response.isSuccessful()) {
                        Log.d(TAG, "가맹점 입고 성공: " + finalBoxCode);
                        boxItems.add(0, new BoxAdapter.BoxItem(finalBoxCode, "BOX-IN", "입고 성공"));
                        adapter.notifyItemInserted(0);
                        rvBoxList.scrollToPosition(0);
                        Toast.makeText(BoxScanActivity.this, "박스 입고 완료: " + finalBoxCode, Toast.LENGTH_SHORT).show();
                        isScanning = true;
                    } else {
                        String errorMsg = "(Code: " + response.code() + ")";
                        try {
                            if (response.errorBody() != null) errorMsg += "\n" + response.errorBody().string();
                        } catch (Exception e) {}
                        Log.e(TAG, "가맹점 입고 실패: " + errorMsg);
                        showErrorAndResume("박스 입고 실패: " + errorMsg);
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                    showLoading(false);
                    Log.e(TAG, "API 통신 오류", t);
                    showErrorAndResume("서버 통신 오류: " + t.getMessage());
                }
            });

        } else {
            // 출고(OUT) 모드 등: BOX 코드와 ORDER 코드 추출
            String boxCode = null;
            String orderCode = null;
            try {
                JsonObject jsonObject = new JsonParser().parse(qrData).getAsJsonObject();
                if (jsonObject.has("boxCode")) {
                    boxCode = jsonObject.get("boxCode").getAsString().trim().toUpperCase();
                }
                if (jsonObject.has("orderCode")) {
                    orderCode = jsonObject.get("orderCode").getAsString().trim().toUpperCase();
                }
            } catch (Exception e) {
                Log.d(TAG, "출고 QR 파싱 실패, raw 데이터 사용 시도");
            }

            if (boxCode == null || boxCode.isEmpty()) {
                boxCode = qrData.trim().toUpperCase();
            }
            if (orderCode == null || orderCode.isEmpty()) {
                orderCode = "ORDER-PENDING";
            }

            // 중복 체크
            for (BoxAdapter.BoxItem item : boxItems) {
                if (item.boxCode.equalsIgnoreCase(boxCode)) {
                    isScanning = true;
                    return;
                }
            }

            viewModel.setBoxData(boxCode, orderCode);
            boxItems.add(0, new BoxAdapter.BoxItem(boxCode, orderCode, "스캔된 박스"));
            adapter.notifyItemInserted(0);
            rvBoxList.scrollToPosition(0);
            Log.d(TAG, "박스 스캔 완료(OUT): " + boxCode + " | Order: " + orderCode);
            isScanning = true;
        }

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




    // 🚀 모드별 화면 이동 로직 (완전 보강)
    public void moveToDetail(String boxCode) {
        Intent intent;
        
        // 클릭된 박스에 해당하는 orderCode 찾기 (데이터 일관성 위해 ViewModel 체크)
        String orderCode = "ORDER-UNKNOWN";
        if (boxCode.equals(viewModel.getBoxCodeValue())) {
            orderCode = viewModel.getOrderCodeValue();
        }

        if ("OUT".equals(currentMode)) {
            Log.d(TAG, "출고 모드 감지: ScanActivity로 이동합니다.");
            intent = new Intent(BoxScanActivity.this, ScanActivity.class);
        } else {
            Log.d(TAG, "입고 모드 감지: InDetailActivity로 이동합니다.");
            intent = new Intent(BoxScanActivity.this, InDetailActivity.class);
        }

        // JSON 형식으로 조립해서 전달 (ScanActivity가 JSON 파싱함)
        JsonObject jsonExport = new JsonObject();
        jsonExport.addProperty("boxCode", boxCode);
        jsonExport.addProperty("orderCode", orderCode);
        
        intent.putExtra("selected_box", jsonExport.toString());
        intent.putExtra("mode", currentMode);
        startActivity(intent);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}