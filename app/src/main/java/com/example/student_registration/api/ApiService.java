package com.example.student_registration.api;

import com.example.student_registration.model.GroupOccupancy;
import com.example.student_registration.model.LoginRequest;
import com.example.student_registration.model.LoginResponse;
import com.example.student_registration.model.NewStudentRequest;
import com.example.student_registration.model.Student;
import com.example.student_registration.model.StudentListResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("api/auth/logout")
    Call<Void> logout();

    @GET("api/lecturer/students")
    Call<StudentListResponse> listStudents(
            @Query("search") String search,
            @Query("programme") String programme,
            @Query("group") String group,
            @Query("page") int page,
            @Query("limit") int limit
    );

    @POST("api/lecturer/students")
    Call<Student> createStudent(@Body NewStudentRequest request);

    @DELETE("api/lecturer/students/{id}")
    Call<Void> deleteStudent(@Path("id") String studentId);

    @GET("api/groups")
    Call<List<GroupOccupancy>> getGroupOccupancy();

    @GET("api/students/me")
    Call<Student> getMyProfile();
}