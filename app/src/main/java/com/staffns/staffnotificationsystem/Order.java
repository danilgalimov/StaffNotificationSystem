package com.staffns.staffnotificationsystem;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Order {

    @SerializedName("place")
    @Expose
    private String place;

    @SerializedName("phone_number")
    @Expose
    private String phone_number;

    @SerializedName("description")
    @Expose
    private String description;

    @SerializedName("full_name")
    @Expose
    private String full_name;

    @SerializedName("id")
    @Expose
    private int id;

    public Order(String place, String phone_number, String description, String full_name, int id) {
        this.place = place;
        this.phone_number = phone_number;
        this.description = description;
        this.full_name = full_name;
        this.id = id;
    }

    String getFull_name() {
        return full_name;
    }

    String getPlace() {
        return place;
    }

    String getPhone_number() {
        return phone_number;
    }

    String getDescription() {
        return description;
    }

    int getId() {
        return  id;
    }
}