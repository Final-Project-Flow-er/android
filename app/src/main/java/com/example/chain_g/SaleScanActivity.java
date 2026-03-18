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
import com.example.chain_g.dto.request.FranchiseSellItemRequest;
import com.example.chain_g.dto.request.FranchiseSellRequest;
import com.example.chain_g.dto.response.ScannedForSaleResponse;
import com.example.chain_g.dto.response.ScannedItemForSaleResponse;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SaleScanActivity extends AppCompatActivity {

    private static final String TAG = "SaleScanActivity";
    private static final int PERMISSION_REQUEST_CODE = 1003;
    private PreviewView previewView;
    private ExecutorService cameraExecutor;
    private volatile boolean isScanning = true;

    private RecyclerView rvSaleList;
    private ProductAdapter adapter;
    private List<ProductAdapter.ProductItem> productItems = new ArrayList<>();
    private List<String> serialCodes = new ArrayList<>();
    private List<ScannedItemForSaleResponse> lastScannedResponseList = new ArrayList<>();

    private TextView tvTotalQuantity, tvTotalAmount;
    private android.app.ProgressDialog progressDialog;

    private Integer currentTotalQuantity = 0;
    private BigDecimal currentTotalAmount = BigDecimal.ZERO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sale_scan);

        previewView = findViewById(R.id.previewView);
        rvSaleList = findViewById(R.id.rv_sale_list);
        tvTotalQuantity = findViewById(R.id.tv_total_quantity);
        tvTotalAmount = findViewById(R.id.tv_total_amount);

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

        // 리사이클러뷰 설정
        adapter = new ProductAdapter(productItems, this::handleDelete);
        rvSaleList.setLayoutManager(new LinearLayoutManager(this));
        rvSaleList.setAdapter(adapter);

        cameraExecutor = Executors.newSingleThreadExecutor();

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
        }

        TextView btnHome = findViewById(R.id.btn_home);
        ImageButton btnBack = findViewById(R.id.btn_back);
        TextView btnPerformSale = findViewById(R.id.btn_perform_sale);

        if (btnHome != null) {
            btnHome.setOnClickListener(v -> {
                Intent intent = new Intent(SaleScanActivity.this, FranManagerMainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnPerformSale != null) {
            btnPerformSale.setOnClickListener(v -> performSale());
        }
    }

    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                finish();
            }
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

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
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
                            String code = rawValue.trim();
                            
                            // QR 데이터에서 시리얼 코드 미리 추출하여 중복 체크 강화
                            String extractedCode = code.toUpperCase();
                            try {
                                com.google.gson.JsonObject jsonObject = new com.google.gson.JsonParser().parse(code).getAsJsonObject();
                                if (jsonObject.has("serialCode")) {
                                    extractedCode = jsonObject.get("serialCode").getAsString().trim().toUpperCase();
                                }
                            } catch (Exception ignored) {}

                            synchronized (this) {
                                if (isScanning) {
                                    boolean alreadyInList = false;
                                    for (String existing : serialCodes) {
                                        if (existing.equalsIgnoreCase(extractedCode)) {
                                            alreadyInList = true;
                                            break;
                                        }
                                    }

                                    if (!alreadyInList) {
                                        isScanning = false;
                                        final String dataToPass = code;
                                        runOnUiThread(() -> handleScannedQr(dataToPass));
                                        break;
                                    }
                                }
                            }
                        }
                    }
                })
                .addOnCompleteListener(task -> imageProxy.close());
    }

    private void handleScannedQr(String qrData) {
        String extractedSerialCode = qrData;
        try {
            com.google.gson.JsonObject jsonObject = new com.google.gson.JsonParser().parse(qrData).getAsJsonObject();
            if (jsonObject.has("serialCode")) {
                extractedSerialCode = jsonObject.get("serialCode").getAsString().trim().toUpperCase();
            }
        } catch (Exception e) {
            Log.d(TAG, "QR 데이터가 JSON 형식이 아니므로 일반 텍스트로 처리합니다.");
            extractedSerialCode = qrData.trim().toUpperCase();
        }

        for (String existing : serialCodes) {
            if (existing.equalsIgnoreCase(extractedSerialCode)) {
                isScanning = true;
                return;
            }
        }

        serialCodes.add(extractedSerialCode);
        fetchScannedSalesList();
    }

    private void fetchScannedSalesList() {
        if (serialCodes.isEmpty()) {
            productItems.clear();
            lastScannedResponseList.clear();
            adapter.notifyDataSetChanged();
            updateTotals(0, BigDecimal.ZERO);
            isScanning = true;
            return;
        }

        showLoading(true);
        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.getScannedSalesList(serialCodes).enqueue(new Callback<ApiResponse<ScannedForSaleResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<ScannedForSaleResponse>> call, Response<ApiResponse<ScannedForSaleResponse>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    ScannedForSaleResponse data = response.body().getData();
                    
                    lastScannedResponseList.clear();
                    lastScannedResponseList.addAll(data.getRequestList());
                    
                    productItems.clear();
                    for (ScannedItemForSaleResponse item : lastScannedResponseList) {
                        ProductAdapter.ProductItem pItem = new ProductAdapter.ProductItem(
                                item.getSerialCode(),
                                item.getProductCode(),
                                item.getProductName(),
                                item.getUnitPrice()
                        );
                        pItem.isScanned = true;
                        productItems.add(pItem);
                    }
                    adapter.notifyDataSetChanged();
                    rvSaleList.scrollToPosition(productItems.size() - 1);
                    
                    updateTotals(data.getTotalQuantity(), data.getTotalAmount());
                } else {
                    serialCodes.remove(serialCodes.size() - 1);
                    Toast.makeText(SaleScanActivity.this, "상품 정보를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
                isScanning = true;
            }

            @Override
            public void onFailure(Call<ApiResponse<ScannedForSaleResponse>> call, Throwable t) {
                showLoading(false);
                serialCodes.remove(serialCodes.size() - 1);
                Toast.makeText(SaleScanActivity.this, "서버 통신 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                isScanning = true;
            }
        });
    }

    private void performSale() {
        if (lastScannedResponseList.isEmpty()) {
            Toast.makeText(this, "판매할 상품이 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);
        List<FranchiseSellItemRequest> requestItems = new ArrayList<>();
        for (ScannedItemForSaleResponse item : lastScannedResponseList) {
            requestItems.add(new FranchiseSellItemRequest(
                    item.getProductId(),
                    item.getProductCode(),
                    item.getProductName(),
                    1,
                    item.getUnitPrice(),
                    item.getSerialCode()
            ));
        }

        FranchiseSellRequest request = new FranchiseSellRequest(
                currentTotalQuantity,
                currentTotalAmount,
                requestItems
        );

        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.createSale(request).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                showLoading(false);
                if (response.isSuccessful()) {
                    Toast.makeText(SaleScanActivity.this, "판매 처리가 완료되었습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(SaleScanActivity.this, "판매 처리 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                showLoading(false);
                Toast.makeText(SaleScanActivity.this, "서버 통신 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateTotals(int quantity, BigDecimal amount) {
        currentTotalQuantity = quantity;
        currentTotalAmount = amount;
        tvTotalQuantity.setText("총 수량 : " + quantity);
        DecimalFormat formatter = new DecimalFormat("#,###");
        if (amount != null) {
            tvTotalAmount.setText("총 금액 : " + formatter.format(amount));
        } else {
            tvTotalAmount.setText("총 금액 : 0");
        }
    }

    private void handleDelete(int position) {
        serialCodes.remove(position);
        fetchScannedSalesList();
    }

    private void showLoading(boolean show) {
        if (show) {
            if (progressDialog == null) {
                progressDialog = new android.app.ProgressDialog(this);
                progressDialog.setMessage("처리 중...");
                progressDialog.setCancelable(false);
            }
            progressDialog.show();
        } else if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}