package com.viegre.nas.pad.entity;

import java.util.List;

public class DevicesFollowEntity {

    private int code;
    private String msg;
    private List<DataBeanXX> data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<DataBeanXX> getData() {
        return data;
    }

    public void setData(List<DataBeanXX> data) {
        this.data = data;
    }
}
