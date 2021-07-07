package com.viegre.nas.pad.entity;

public class UserTokenTime {

    private String phone;
    private long token_start_time;
    private int token_hour_time;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public long getToken_start_time() {
        return token_start_time;
    }

    public void setToken_start_time(long token_start_time) {
        this.token_start_time = token_start_time;
    }

    public int getToken_hour_time() {
        return token_hour_time;
    }

    public void setToken_hour_time(int token_hour_time) {
        this.token_hour_time = token_hour_time;
    }
}
