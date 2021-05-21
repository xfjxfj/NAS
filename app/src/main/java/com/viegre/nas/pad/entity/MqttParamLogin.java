package com.viegre.nas.pad.entity;

public class MqttParamLogin {

    private String token;
    private String sn;
    private String phone;
    private String state;
    private String boundTime;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getBoundTime() {
        return boundTime;
    }

    public void setBoundTime(String boundTime) {
        this.boundTime = boundTime;
    }
}
