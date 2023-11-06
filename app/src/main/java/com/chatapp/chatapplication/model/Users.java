package com.chatapp.chatapplication.model;

public class Users {
    private String id;
    private String imageUrl;
    private String userName;
    private String status;
    private String search;

    public Users(String id, String imageUrl, String userName,String status, String search) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.userName = userName;
        this.status = status;
        this.search = search;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Users() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }
}
