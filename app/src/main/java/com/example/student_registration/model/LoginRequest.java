package com.example.student_registration.model;

import com.google.gson.annotations.SerializedName;

public class LoginRequest {
    @SerializedName("loginKey") private final String loginKey;
    @SerializedName("password") private final String password;

    public LoginRequest(String loginKey, String password) {
        this.loginKey = loginKey;
        this.password = password;
    }
}