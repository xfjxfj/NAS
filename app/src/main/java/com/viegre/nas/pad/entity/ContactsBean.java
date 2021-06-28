package com.viegre.nas.pad.entity;

public class ContactsBean{

    private String userid;
    private String userimg;
    private String username;
    private String userphone;
    private String sn;

    public ContactsBean(String userid, String userimg, String username, String userphone) {
        this.userid = userid;
        this.userimg = userimg;
        this.username = username;
        this.userphone = userphone;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getUserimg() {
        return userimg;
    }

    public void setUserimg(String userimg) {
        this.userimg = userimg;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserphone() {
        return userphone;
    }

    public void setUserphone(String userphone) {
        this.userphone = userphone;
    }
}