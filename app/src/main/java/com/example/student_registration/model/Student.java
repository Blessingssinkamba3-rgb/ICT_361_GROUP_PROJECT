package com.example.student_registration.model;

import com.google.gson.annotations.SerializedName;

public class Student {
    @SerializedName("studentId") private String studentId;
    @SerializedName("studentNumber") private String studentNumber;
    @SerializedName("fullName") private String fullName;
    @SerializedName("programme") private String programme;
    @SerializedName("group") private String group;
    @SerializedName("isActive") private boolean active;
    @SerializedName("version") private int version;
    @SerializedName("updatedAt") private String updatedAt;

    public String getStudentId() { return studentId; }
    public String getStudentNumber() { return studentNumber; }
    public String getFullName() { return fullName; }
    public String getProgramme() { return programme; }
    public String getGroup() { return group; }
    public boolean isActive() { return active; }
    public int getVersion() { return version; }
    public String getUpdatedAt() { return updatedAt; }
}