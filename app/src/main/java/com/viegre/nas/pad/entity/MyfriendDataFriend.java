package com.viegre.nas.pad.entity;

public class MyfriendDataFriend {
    private String callId;
    private String friendName;
    private String ico;
    private String userSn;
    private String type;


    public MyfriendDataFriend(String callId, String friendName, String ico, String userSn, String type) {
        this.callId = callId;
        this.friendName = friendName;
        this.ico = ico;
        this.userSn = userSn;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCallId() {
        return callId;
    }

    public void setCallId(String callId) {
        this.callId = callId;
    }

    public String getFriendName() {
        return friendName;
    }

    public void setFriendName(String friendName) {
        this.friendName = friendName;
    }

    public String getIco() {
        return ico;
    }

    public void setIco(String ico) {
        this.ico = ico;
    }

    public String getUserSn() {
        return userSn;
    }

    public void setUserSn(String userSn) {
        this.userSn = userSn;
    }
}
