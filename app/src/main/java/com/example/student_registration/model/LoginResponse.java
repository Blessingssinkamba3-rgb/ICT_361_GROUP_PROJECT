package com.example.student_registration.model;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("token") private String token;
    @SerializedName("role") private String role;
    @SerializedName("accountId") private String accountId;
    @SerializedName("displayName") private String displayName;
    @SerializedName("student") private Student student;

    public String getToken() { return token; }
    public String getRole() { return role; }
    public String getAccountId() { return accountId; }
    public String getDisplayName() { return displayName; }
    public Student getStudent() { return student; }
}