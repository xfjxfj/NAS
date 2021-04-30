package com.viegre.nas.pad.entity;

public class ConstactBean {
    private String phone;
    private String userid;

    public ConstactBean(String phone, String userid) {
        this.phone = phone;
        this.userid = userid;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getPhone() {
        return phone;
    }

    public String getUserid() {
        return userid;
    }

    @Override
    public String toString() {
        return "ConstactBean{" +
                "phone='" + phone + '\'' +
                ", userid='" + userid + '\'' +
                '}';
    }
}
