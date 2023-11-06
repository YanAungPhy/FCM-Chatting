package com.chatapp.chatapplication.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Data {
    @SerializedName("noti_data")
    @Expose
    private String notiData;

    public String getNotiData() {
        return notiData;
    }

    public void setNotiData(String notiData) {
        this.notiData = notiData;
    }
}
