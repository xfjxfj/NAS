package com.viegre.nas.pad.entity;

import java.util.List;

public class DataBean {
    private List<BindListBean> bindList;
    private UserBean user;
    private String token;

    public List<BindListBean> getBindList() {
        return bindList;
    }

    public void setBindList(List<BindListBean> bindList) {
        this.bindList = bindList;
    }

    public UserBean getUser() {
        return user;
    }

    public void setUser(UserBean user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
