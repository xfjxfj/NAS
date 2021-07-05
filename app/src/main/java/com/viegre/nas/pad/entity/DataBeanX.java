package com.viegre.nas.pad.entity;

import java.util.List;

public class DataBeanX {
    private List<BindListBeanX> bindList;
    private UserBeanX user;
    private String token;

    public List<BindListBeanX> getBindList() {
        return bindList;
    }

    public void setBindList(List<BindListBeanX> bindList) {
        this.bindList = bindList;
    }

    public UserBeanX getUser() {
        return user;
    }

    public void setUser(UserBeanX user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
