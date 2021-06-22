package com.viegre.nas.pad.entity;

public class DevicesFriendsListBean {
    public String callId;
    public String sn;
    public String name;

    @Override
    public String toString() {
        return "DevicesFriendsListBean{" +
                "callId='" + callId + '\'' +
                ", sn='" + sn + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    public String getCallId() {
        return callId;
    }

    public void setCallId(String callId) {
        this.callId = callId;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
