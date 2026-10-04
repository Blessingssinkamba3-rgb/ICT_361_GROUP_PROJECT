package com.example.student_registration;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.student_registration.session.SessionManager;
import com.example.student_registration.viewmodel.LoginViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private LoginViewModel viewModel;
    private TextInputEditText editLoginKey;
    private TextInputEditText editPassword;
    private MaterialButton buttonSignIn;
    private View progressLogin;
    private TextView textLoginError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SessionManager session = new SessionManager(this);
        if (session.isLoggedIn()) {
            routeToHome(session.isLecturer());
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        editLoginKey = findViewById(R.id.editLoginKey);
        editPassword = findViewById(R.id.editPassword);
        buttonSignIn = findViewById(R.id.buttonSignIn);
        progressLogin = findViewById(R.id.progressLogin);
        textLoginError = findViewById(R.id.textLoginError);

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        buttonSignIn.setOnClickListener(v -> {
            String loginKey = editLoginKey.getText() != null ? editLoginKey.getText().toString() : "";
            String password = editPassword.getText() != null ? editPassword.getText().toString() : "";
            viewModel.signIn(loginKey, password);
        });

        viewModel.getUiState().observe(this, state -> {
            progressLogin.setVisibility(state.loading ? View.VISIBLE : View.GONE);
            buttonSignIn.setEnabled(!state.loading);

            if (state.errorMessage != null) {
                textLoginError.setText(state.errorMessage);
                textLoginError.setVisibility(View.VISIBLE);
            } else {
                textLoginError.setVisibility(View.GONE);
            }

            if (state.success != null) {
                routeToHome("lecturer".equals(state.success.getRole()));
                finish();
            }
        });
    }

    private void routeToHome(boolean isLecturer) {
        Intent intent = new Intent(this,
                isLecturer ? LecturerHomeActivity.class : StudentHomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}