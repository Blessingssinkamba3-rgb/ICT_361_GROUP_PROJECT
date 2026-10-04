package com.example.student_registration;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.student_registration.model.LoginResponse;
import com.example.student_registration.session.SessionManager;
import com.example.student_registration.viewmodel.LoginViewModel;
import com.google.android.material.button.MaterialButton;

public class LoginActivity extends AppCompatActivity {

    private LoginViewModel viewModel;
    private SessionManager sessionManager;

    private EditText editLoginKey;
    private EditText editPassword;
    private MaterialButton buttonSignIn;
    private ProgressBar progressLogin;
    private TextView textLoginError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            navigateToHome();
            return;
        }

        setContentView(R.layout.activity_login);

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        editLoginKey = findViewById(R.id.editLoginKey);
        editPassword = findViewById(R.id.editPassword);
        buttonSignIn = findViewById(R.id.buttonSignIn);
        progressLogin = findViewById(R.id.progressLogin);
        textLoginError = findViewById(R.id.textLoginError);

        buttonSignIn.setOnClickListener(v -> {
            String loginKey = editLoginKey.getText() != null ? editLoginKey.getText().toString() : "";
            String password = editPassword.getText() != null ? editPassword.getText().toString() : "";
            viewModel.signIn(loginKey, password);
        });

        viewModel.getUiState().observe(this, state -> {
            if (state == null) return;

            if (state.loading) {
                progressLogin.setVisibility(View.VISIBLE);
                textLoginError.setVisibility(View.GONE);
                buttonSignIn.setEnabled(false);
            } else if (state.errorMessage != null) {
                progressLogin.setVisibility(View.GONE);
                textLoginError.setText(state.errorMessage);
                textLoginError.setVisibility(View.VISIBLE);
                buttonSignIn.setEnabled(true);
            } else if (state.success != null) {
                progressLogin.setVisibility(View.GONE);
                buttonSignIn.setEnabled(true);
                navigateToHome();
            } else {
                progressLogin.setVisibility(View.GONE);
                textLoginError.setVisibility(View.GONE);
                buttonSignIn.setEnabled(true);
            }
        });
    }

    private void navigateToHome() {
        Intent intent;
        if (sessionManager.isLecturer()) {
            intent = new Intent(this, LecturerHomeActivity.class);
        } else {
            intent = new Intent(this, StudentHomeActivity.class);
        }
        startActivity(intent);
        finish();
    }
}