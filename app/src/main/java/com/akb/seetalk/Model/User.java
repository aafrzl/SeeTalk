package com.akb.seetalk.Model;

public class User {

    private String id;
    private String username;
    private String imageURL;
    private String status;
    private String email;
    private String bio;


    public User(String id, String username, String imageURL, String status, String email, String bio) {
        this.id = id;
        this.username = username;
        this.imageURL = imageURL;
        this.status = status;
        this.email = email;
        this.bio = bio;

    }

    public User() {

    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}

