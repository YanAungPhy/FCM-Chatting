package com.chatapp.chatapplication.model;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Noti_Obj {
    @SerializedName("priority")
    @Expose
    private String priority;
    @SerializedName("data")
    @Expose
    private Data data;
    @SerializedName("notification")
    @Expose
    private Notification notification;
    @SerializedName("to")
    @Expose
    private String to;

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public Notification getNotification() {
        return notification;
    }

    public void setNotification(Notification notification) {
        this.notification = notification;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }
}
