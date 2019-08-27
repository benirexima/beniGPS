package com.example.benigps.Model;

public class Model {

    private String username;
    private String password;
    private String deviceId;
    private String role;
    private String refreshToken;


    public Model(String username, String password, String deviceId, String role, String refreshToken) {
        this.username = username;
        this.password = password;
        this.deviceId = deviceId;
        this.role = role;
        this.refreshToken = refreshToken;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    @Override
    public String toString() {
        return "Model{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", role='" + role + '\'' +
                ", refreshToken='" + refreshToken + '\'' +
                '}';
    }
}
