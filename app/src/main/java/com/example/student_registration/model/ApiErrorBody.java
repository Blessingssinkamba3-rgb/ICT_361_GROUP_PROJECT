package com.example.student_registration.model;

import com.google.gson.annotations.SerializedName;

public class ApiErrorBody {
    @SerializedName("code") private String code;
    @SerializedName("message") private String message;
    @SerializedName("details") private Object details;

    public String getCode() { return code; }
    public String getMessage() { return message; }
    public Object getDetails() { return details; }
}