package com.example.kickoff;

public class User {
    private String username;
    private String email;
    private String group;
    public User() {}
    public User(String username, String email, String group) {
        this.username = username;
        this.email = email;
        this.group = group;
    }

    // getters
    public String getUsername() {
        return username;
    }
    public String getEmail() {
        return email;
    }
    public String getGroup() {
        return group;
    }
    public void setGroup(String group) {
        this.group = group;
    }
}
