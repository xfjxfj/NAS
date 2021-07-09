package com.viegre.nas.pad.entity;

public class DataBeanXX {
    private String phone;
    private Object nickName;
    private Object avatar;
    private Object picData;
    private String callId;
    private StatusBean status;
    private String boundTime;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Object getNickName() {
        return nickName;
    }

    public void setNickName(Object nickName) {
        this.nickName = nickName;
    }

    public Object getAvatar() {
        return avatar;
    }

    public void setAvatar(Object avatar) {
        this.avatar = avatar;
    }

    public Object getPicData() {
        return picData;
    }

    public void setPicData(Object picData) {
        this.picData = picData;
    }

    public String getCallId() {
        return callId;
    }

    public void setCallId(String callId) {
        this.callId = callId;
    }

    public StatusBean getStatus() {
        return status;
    }

    public void setStatus(StatusBean status) {
        this.status = status;
    }

    public String getBoundTime() {
        return boundTime;
    }

    public void setBoundTime(String boundTime) {
        this.boundTime = boundTime;
    }
}
