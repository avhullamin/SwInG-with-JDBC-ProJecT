package com.CraigClono;

public class User {
    public int userId;
    public String username;
    public String phone;
    public String email;

    public User(int userId, String username, String phone, String email) {
        this.userId = userId;
        this.username = username;
        this.phone = phone;
        this.email = email;
    }

    public User() {}
}