package com.example.student_registration.api;

import android.content.Context;

import com.example.student_registration.session.SessionManager;

import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;

public class ApiClient {

    // 10.0.2.2 is the Android emulator's alias for the host machine's
    // localhost. Point this at your real server for a physical device.
    private static final String BASE_URL = "http://10.0.2.2:3000/";

    private static Retrofit retrofit;

    public static ApiService getService(Context context) {
        if (retrofit == null) {
            SessionManager session = new SessionManager(context);

            Interceptor authInterceptor = new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request original = chain.request();
                    String token = session.getToken();
                    if (token == null) {
                        return chain.proceed(original);
                    }
                    Request authorised = original.newBuilder()
                            .header("Authorization", "Bearer " + token)
                            .build();
                    return chain.proceed(authorised);
                }
            };

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(authInterceptor)
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }
}