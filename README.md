# 📱 CHAIN-G (Android)

<img width="300" alt="Gemini_Generated_Image_8j8ddp8j8ddp8j8d-Photoroom" src="https://github.com/user-attachments/assets/6f623ede-7a0d-4fcd-a74f-3bec56de2c63" />

> CONNECT GOOD, VALUE CHAIN
>
**CHAIN-G** Android 애플리케이션은 스마트 공급망 관리 시스템의 현장 업무를 지원하는 모바일 도구입니다.  
공장 및 가맹점 관리자가 스마트폰을 이용해 제품 바코드를 스캔함으로써 실시간으로 재고를 처리하고 판매 내역을 기록할 수 있는 **스캔 중심의 현장 지원 앱**입니다.

<br>

## 🛠 기술 스택

| Category | Stack |
| :--- | :--- |
| **Language** | Java |
| **UI Components** | Google Material Design, XML |
| **Scanning** | Google ML Kit Barcode Scanning, CameraX |
| **Networking** | Retrofit2, GSON |
| **Development Tool** | Android Studio |

<br>

## 📂 프로젝트 구조

```bash
android/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/chain_g/
│   │   │   ├── auth/                         # 로그인 및 인증 로직
│   │   │   ├── common/                       # 공통 유틸 및 상단 바
│   │   │   ├── BoxScanActivity.java          # 박스 스캔 처리
│   │   │   ├── SaleScanActivity.java         # 판매 스캔 처리
│   │   │   ├── FacManagerMainActivity.java   # 공장 관리자 메인
│   │   │   └── FranManagerMainActivity.java  # 가맹점 관리자 메인
│   │   └── res/              # XML 레이아웃 및 리소스
└── build.gradle              # 프로젝트 수준 빌드 설정
```

<br>

## 🚀 주요 기능

- **실시간 바코드/QR 스캔**: Google ML Kit와 CameraX를 연동하여 빠르고 정확하게 제품/박스 코드를 인식합니다.
- **재고 입출고 정보 확인**: 스캔한 박스의 정보를 기반으로 상세 입고/출고 품목을 즉시 확인하고 수량을 체크합니다.
- **판매 등록 관리**: 가맹점에서 상품 판매 시 바코드 스캔을 통해 판매 데이터를 본사 시스템과 즉시 동기화합니다.
- **역할 기반 인터페이스**: 로그인한 사용자의 권한(공장/가맹점)에 따라 다른 메인 기능 탭과 서비스를 제공합니다.

<br>

## 🖥️ 주요 화면
- **로그인 및 실시간 바코드/QR 스캔**
<img width="200" alt="image" src="https://github.com/user-attachments/assets/a8d1522e-2d08-46e2-9ffc-6881725a0b0e" />

<br>

## 👥 팀 협업 규칙

### ⭐ 개발 규칙 및 컨벤션

- **Activity 및 Layout**: 기능별로 명확히 분리하며, 재사용 가능한 커스텀 뷰는 `common` 패키지에서 관리합니다.
- **API 연동**: Retrofit 인터페이스를 통해 백엔드 API 명세와 일치하도록 데이터 모델을 동기화합니다.
- **주석 준수**: 복잡한 스캔 로직이나 카메라 프리뷰 관련 코드는 주석을 통해 의도를 명시합니다.
