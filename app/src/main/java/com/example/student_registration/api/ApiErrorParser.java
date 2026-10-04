package com.example.student_registration.api;

import com.example.student_registration.model.ApiErrorBody;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Response;

public final class ApiErrorParser {

    private static final Gson GSON = new Gson();

    private ApiErrorParser() {}

    public static ApiErrorBody parse(Response<?> response) {
        if (response == null) {
            return fallback("Unknown error");
        }

        ResponseBody errorBody = response.errorBody();
        if (errorBody == null) {
            return fallback(response.message());
        }

        try {
            String jsonString = errorBody.string();
            ApiErrorBody parsed = GSON.fromJson(jsonString, ApiErrorBody.class);

            if (parsed == null) {
                return fallback(response.message());
            }

            // Ensure fallback for missing fields in parsed JSON
            if (parsed.getCode() == null || "UNKNOWN".equals(parsed.getCode())) {
                if (response.code() > 0) {
                    parsed.setCode("HTTP_" + response.code());
                }
            }
            if (parsed.getMessage() == null || "An error occurred".equals(parsed.getMessage())) {
                String httpMessage = response.message();
                if (httpMessage != null && !httpMessage.trim().isEmpty()) {
                    parsed.setMessage(httpMessage);
                }
            }

            return parsed;
        } catch (IOException | JsonSyntaxException e) {
            return fallback(response.message());
        }
    }

    private static ApiErrorBody fallback(String message) {
        String msg = (message == null || message.trim().isEmpty()) ? "Unknown error" : message;
        return new ApiErrorBody("UNKNOWN", msg);
    }
}
