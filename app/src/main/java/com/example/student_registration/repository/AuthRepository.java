package com.example.student_registration.repository;

import android.content.Context;

import com.example.student_registration.api.ApiClient;
import com.example.student_registration.api.ApiErrorParser;
import com.example.student_registration.api.ApiService;
import com.example.student_registration.model.ApiErrorBody;
import com.example.student_registration.model.LoginRequest;
import com.example.student_registration.model.LoginResponse;
import com.example.student_registration.session.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {

    public interface LoginCallback {
        void onSuccess(LoginResponse response);
        void onApiError(ApiErrorBody error);
        void onNetworkError(String message);
    }

    private final ApiService api;
    private final SessionManager session;

    public AuthRepository(Context context) {
        api = ApiClient.getService(context);
        session = new SessionManager(context);
    }

    public void login(String loginKey, String password, LoginCallback callback) {
        api.login(new LoginRequest(loginKey, password)).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse body = response.body();
                    String studentId = body.getStudent() != null ? body.getStudent().getStudentId() : null;
                    session.save(body.getToken(), body.getRole(), body.getAccountId(), body.getDisplayName(), studentId);
                    callback.onSuccess(body);
                } else {
                    callback.onApiError(ApiErrorParser.parse(response));
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                callback.onNetworkError(t.getMessage());
            }
        });
    }

    public void logout() {
        session.clear();
        api.logout().enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> response) { /* no-op */ }
            @Override public void onFailure(Call<Void> call, Throwable t) { /* server unreachable — fine, already cleared locally */ }
        });
    }
}