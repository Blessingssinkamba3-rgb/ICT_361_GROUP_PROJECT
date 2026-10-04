package com.example.student_registration;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.student_registration.repository.AuthRepository;
import com.example.student_registration.session.SessionManager;

public class StudentHomeActivity extends AppCompatActivity {

    private AuthRepository authRepository;
    private SessionManager sessionManager;

    private TextView textWelcome;
    private ImageButton buttonSignOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_home);

        authRepository = new AuthRepository(this);
        sessionManager = new SessionManager(this);

        textWelcome = findViewById(R.id.textWelcome);
        buttonSignOut = findViewById(R.id.buttonSignOut);

        String name = sessionManager.getDisplayName();
        if (name != null && !name.trim().isEmpty()) {
            textWelcome.setText("Welcome back, " + name);
        } else {
            textWelcome.setText("Welcome back");
        }

        buttonSignOut.setOnClickListener(v -> {
            authRepository.logout();
            Intent intent = new Intent(StudentHomeActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}