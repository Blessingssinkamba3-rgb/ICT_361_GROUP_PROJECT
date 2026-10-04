package com.example.student_registration.model;

import com.google.gson.annotations.SerializedName;

public class GroupOccupancy {
    @SerializedName("group") private String group;
    @SerializedName("capacity") private int capacity;
    @SerializedName("occupied") private int occupied;

    public String getGroup() { return group; }
    public int getCapacity() { return capacity; }
    public int getOccupied() { return occupied; }
    public boolean isFull() { return occupied >= capacity; }
}