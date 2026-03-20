package com.example.chain_g.common;

import android.content.Context;
import com.example.chain_g.auth.dto.response.LoginResponse;
import com.example.chain_g.auth.jwt.TokenManager;
import okhttp3.Authenticator;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import android.util.Log;

import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "http://192.168.90.43:8080/";
    private static Retrofit retrofit = null;
    private static ApiService apiService = null;

    public static ApiService getApiService(Context context) {
        if (retrofit == null) {
            Context appContext = context.getApplicationContext();

            Interceptor authInterceptor = chain -> {
                String accessToken = TokenManager.getAccessToken(appContext);
                Request.Builder builder = chain.request().newBuilder();
                if (accessToken != null) {
                    builder.addHeader("Authorization", "Bearer " + accessToken);
                }
                return chain.proceed(builder.build());
            };

            Authenticator tokenAuthenticator = (route, response) -> {
                if (responseCount(response) >= 2) {
                    return null;
                }

                String refreshToken = TokenManager.getRefreshToken(appContext);
                if (refreshToken == null) return null;

                ApiService reissueService = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                        .create(ApiService.class);

                retrofit2.Response<ApiResponse<LoginResponse>> reissueResponse =
                        reissueService.reissue("Bearer " + refreshToken).execute();

                if (reissueResponse.isSuccessful() && reissueResponse.body() != null) {
                    LoginResponse data = reissueResponse.body().getData();
                    TokenManager.saveTokens(appContext, data.getAccessToken(), data.getRefreshToken());

                    return response.request().newBuilder()
                            .header("Authorization", "Bearer " + data.getAccessToken())
                            .build();
                }
                return null;
            };

            // 커스텀 로그 인터셉터 (바디 로그 추가)
            Interceptor loggingInterceptor = chain -> {
                Request request = chain.request();
                Log.d("API_LOG", "--> " + request.method() + " " + request.url());
                Log.d("API_LOG", "Headers: " + request.headers());
                
                if (request.body() != null) {
                    okio.Buffer buffer = new okio.Buffer();
                    request.body().writeTo(buffer);
                    Log.d("API_LOG", "Body: " + buffer.readUtf8());
                }

                Response response = chain.proceed(request);
                
                Log.d("API_LOG", "<-- " + response.code() + " " + response.request().url());
                return response;
            };


            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(authInterceptor)
                    .addInterceptor(loggingInterceptor)
                    .authenticator(tokenAuthenticator)
                    .build();



            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            apiService = retrofit.create(ApiService.class);
        }
        return apiService;
    }

    // 재시도 횟수를 계산
    private static int responseCount(Response response) {
        int result = 1;
        while ((response = response.priorResponse()) != null) {
            result++;
        }
        return result;
    }
}
