package com.example.student_registration.api;

import com.example.student_registration.model.ApiErrorBody;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Response;

public final class ApiErrorParser {

    private static final Gson GSON = new Gson();

    private ApiErrorParser() {}

    public static ApiErrorBody parse(Response<?> response) {
        ResponseBody errorBody = response.errorBody();
        if (errorBody == null) {
            return fallback(response.message());
        }
        try {
            ApiErrorBody parsed = GSON.fromJson(errorBody.string(), ApiErrorBody.class);
            return parsed != null ? parsed : fallback(response.message());
        } catch (IOException | com.google.gson.JsonSyntaxException e) {
            return fallback(response.message());
        }
    }

    private static ApiErrorBody fallback(String message) {
        return GSON.fromJson(
                "{\"code\":\"UNKNOWN\",\"message\":\"" + (message == null ? "Unknown error" : message) + "\"}",
                ApiErrorBody.class
        );
    }
}