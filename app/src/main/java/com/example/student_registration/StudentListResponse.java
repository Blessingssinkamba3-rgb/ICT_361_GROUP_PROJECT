// StudentListResponse.java
package com.example.student_registration.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class StudentListResponse {
    @SerializedName("items") private List<Student> items;
    @SerializedName("page") private int page;
    @SerializedName("limit") private int limit;
    @SerializedName("total") private int total;

    public List<Student> getItems() { return items; }
    public int getPage() { return page; }
    public int getLimit() { return limit; }
    public int getTotal() { return total; }
}