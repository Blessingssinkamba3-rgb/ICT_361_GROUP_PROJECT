package com.example.student_registration.repository;

import android.content.Context;

import com.example.student_registration.api.ApiClient;
import com.example.student_registration.api.ApiErrorParser;
import com.example.student_registration.api.ApiService;
import com.example.student_registration.model.ApiErrorBody;
import com.example.student_registration.model.GroupOccupancy;
import com.example.student_registration.model.NewStudentRequest;
import com.example.student_registration.model.Student;
import com.example.student_registration.model.StudentListResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentRepository {

    public interface ListCallback {
        void onSuccess(StudentListResponse result);
        void onApiError(ApiErrorBody error);
        void onNetworkError(String message);
    }

    public interface GroupsCallback {
        void onSuccess(List<GroupOccupancy> groups);
        void onError(String message);
    }

    public interface CreateCallback {
        void onSuccess(Student student);
        void onApiError(ApiErrorBody error);
        void onNetworkError(String message);
    }

    private final ApiService api;

    public StudentRepository(Context context) {
        api = ApiClient.getService(context);
    }

    public void searchRoster(String search, String programme, String group, int page, int limit, ListCallback callback) {
        api.listStudents(emptyToNull(search), emptyToNull(programme), emptyToNull(group), page, limit)
                .enqueue(new Callback<StudentListResponse>() {
                    @Override
                    public void onResponse(Call<StudentListResponse> call, Response<StudentListResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            callback.onSuccess(response.body());
                        } else {
                            callback.onApiError(ApiErrorParser.parse(response));
                        }
                    }

                    @Override
                    public void onFailure(Call<StudentListResponse> call, Throwable t) {
                        callback.onNetworkError(t.getMessage());
                    }
                });
    }

    public void getGroupOccupancy(GroupsCallback callback) {
        api.getGroupOccupancy().enqueue(new Callback<List<GroupOccupancy>>() {
            @Override
            public void onResponse(Call<List<GroupOccupancy>> call, Response<List<GroupOccupancy>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Couldn't load group occupancy");
                }
            }

            @Override
            public void onFailure(Call<List<GroupOccupancy>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void createStudent(String fullName, String studentNumber, String programme, String group, CreateCallback callback) {
        api.createStudent(new NewStudentRequest(fullName, studentNumber, programme, group))
                .enqueue(new Callback<Student>() {
                    @Override
                    public void onResponse(Call<Student> call, Response<Student> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            callback.onSuccess(response.body());
                        } else {
                            callback.onApiError(ApiErrorParser.parse(response));
                        }
                    }

                    @Override
                    public void onFailure(Call<Student> call, Throwable t) {
                        callback.onNetworkError(t.getMessage());
                    }
                });
    }

    private static String emptyToNull(String value) {
        return (value == null || value.trim().isEmpty()) ? null : value.trim();
    }
}