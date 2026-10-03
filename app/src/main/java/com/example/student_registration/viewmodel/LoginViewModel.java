package com.example.student_registration.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.student_registration.model.ApiErrorBody;
import com.example.student_registration.model.LoginResponse;
import com.example.student_registration.repository.AuthRepository;

public class LoginViewModel extends AndroidViewModel {

    public static final class UiState {
        public final boolean loading;
        public final String errorMessage;
        public final LoginResponse success;

        private UiState(boolean loading, String errorMessage, LoginResponse success) {
            this.loading = loading;
            this.errorMessage = errorMessage;
            this.success = success;
        }

        static UiState idle() { return new UiState(false, null, null); }
        static UiState loading() { return new UiState(true, null, null); }
        static UiState error(String message) { return new UiState(false, message, null); }
        static UiState success(LoginResponse response) { return new UiState(false, null, response); }
    }

    private final AuthRepository authRepository;
    private final MutableLiveData<UiState> uiState = new MutableLiveData<>(UiState.idle());

    public LoginViewModel(@NonNull Application application) {
        super(application);
        authRepository = new AuthRepository(application);
    }

    public LiveData<UiState> getUiState() {
        return uiState;
    }

    public void signIn(String loginKey, String password) {
        if (loginKey == null || loginKey.trim().isEmpty() || password == null || password.isEmpty()) {
            uiState.setValue(UiState.error("Enter your student number/email and password."));
            return;
        }

        uiState.setValue(UiState.loading());
        authRepository.login(loginKey.trim(), password, new AuthRepository.LoginCallback() {
            @Override
            public void onSuccess(LoginResponse response) {
                uiState.postValue(UiState.success(response));
            }

            @Override
            public void onApiError(ApiErrorBody error) {
                uiState.postValue(UiState.error(error.getMessage()));
            }

            @Override
            public void onNetworkError(String message) {
                uiState.postValue(UiState.error("Can't reach the server. Check your connection and try again."));
            }
        });
    }
}