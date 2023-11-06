package com.chatapp.chatapplication.retrofit;

import java.util.Map;

public class FCMMessage {
    private String to;
    private Map<String, String> data;

    public FCMMessage(String to, Map<String, String> data) {
        this.to = to;
        this.data = data;
    }
}

