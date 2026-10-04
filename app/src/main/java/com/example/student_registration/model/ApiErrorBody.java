package com.example.student_registration.model;

import com.google.gson.annotations.SerializedName;

public class ApiErrorBody {
    @SerializedName("code") private String code;
    @SerializedName("message") private String message;
    @SerializedName("details") private Object details;

    public ApiErrorBody() {}

    public ApiErrorBody(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public ApiErrorBody(String code, String message, Object details) {
        this.code = code;
        this.message = message;
        this.details = details;
    }

    public String getCode() {
        return code != null ? code : "UNKNOWN";
    }

    public String getMessage() {
        return (message != null && !message.trim().isEmpty()) ? message : "An error occurred";
    }

    public Object getDetails() {
        return details;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setDetails(Object details) {
        this.details = details;
    }
}
