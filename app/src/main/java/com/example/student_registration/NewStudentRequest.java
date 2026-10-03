// NewStudentRequest.java
package com.example.student_registration.model;

import com.google.gson.annotations.SerializedName;

public class NewStudentRequest {
    @SerializedName("fullName") private final String fullName;
    @SerializedName("studentNumber") private final String studentNumber;
    @SerializedName("programme") private final String programme;
    @SerializedName("group") private final String group;

    public NewStudentRequest(String fullName, String studentNumber, String programme, String group) {
        this.fullName = fullName;
        this.studentNumber = studentNumber;
        this.programme = programme;
        this.group = group;
    }
}