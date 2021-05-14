package com.viegre.nas.pad.entity;

import com.google.gson.annotations.SerializedName;

public class RecordListBean2 {

    private String targetId;
    @SerializedName("Direction")
    private String direction;
    @SerializedName("AudioOnly")
    private boolean audioOnly;
    @SerializedName("ConnectTime")
    private long connectTime;
    @SerializedName("EndTime")
    private long endTime;
    @SerializedName("CallId")
    private String callId;
    @SerializedName("MessageUid")
    private long messageUid;
    @SerializedName("CallTime")
    private String callTime;
    @SerializedName("TurnOnTime")
    private String turnOnTime;
    @SerializedName("TurnOn")
    private boolean turnOn;

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public boolean isAudioOnly() {
        return audioOnly;
    }

    public void setAudioOnly(boolean audioOnly) {
        this.audioOnly = audioOnly;
    }

    public long getConnectTime() {
        return connectTime;
    }

    public void setConnectTime(long connectTime) {
        this.connectTime = connectTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public String getCallId() {
        return callId;
    }

    public void setCallId(String callId) {
        this.callId = callId;
    }

    public long getMessageUid() {
        return messageUid;
    }

    public void setMessageUid(long messageUid) {
        this.messageUid = messageUid;
    }

    public String getCallTime() {
        return callTime;
    }

    public void setCallTime(String callTime) {
        this.callTime = callTime;
    }

    public String getTurnOnTime() {
        return turnOnTime;
    }

    public void setTurnOnTime(String turnOnTime) {
        this.turnOnTime = turnOnTime;
    }

    public boolean isTurnOn() {
        return turnOn;
    }

    public void setTurnOn(boolean turnOn) {
        this.turnOn = turnOn;
    }

    @Override
    public String toString() {
        return "RecordListBean{" +
                "targetId='" + targetId + '\'' +
                ", direction='" + direction + '\'' +
                ", audioOnly=" + audioOnly +
                ", connectTime=" + connectTime +
                ", endTime=" + endTime +
                ", callId='" + callId + '\'' +
                ", messageUid=" + messageUid +
                ", callTime='" + callTime + '\'' +
                ", turnOnTime='" + turnOnTime + '\'' +
                ", turnOn=" + turnOn +
                '}';
    }
}
