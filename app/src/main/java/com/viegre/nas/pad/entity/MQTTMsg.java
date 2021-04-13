package com.viegre.nas.pad.entity;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by レインマン on 2021/04/12 14:07 with Android Studio.
 */
public class MQTTMsg {

	public final static String TYPE_NOTIFY = "notify";
	public final static String TYPE_CMD = "cmd";

	public final static String MSG_DEVICE_INFO = "deviceInfo";//设备信息
	public final static String MSG_STORAGE_INFO = "storageInfo";//公共/私人存储空间
	public final static String MSG_DISK_DEFRAGMENT = "diskDefragment";//磁盘整理
	public final static String MSG_RESTORE = "restore";//还原
	public final static String MSG_BACKUP = "backup";//备份
	public final static String MSG_LIFE_TEST = "lifeTest";//使用寿命检测
	public final static String MSG_SHUT_DOWN = "shutDown";//关机
	public final static String MSG_REBOOT = "reboot";//重启

	private String msgType;
	private String msg;
	private String fromId;
	private String toId;
	private long timestamp;
	private final JSONObject jsonObject;

	public MQTTMsg(String toId, JSONObject jsonObject) {
		this.toId = toId;
		this.jsonObject = jsonObject;
	}

	public MQTTMsg(String msgType, String msg, String fromId, String toId, long timestamp) {
		this.msgType = msgType;
		this.msg = msg;
		this.fromId = fromId;
		this.toId = toId;
		this.timestamp = timestamp;
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("msgType", msgType);
		jsonObject.put("msg", msg);
		jsonObject.put("fromId", fromId);
		jsonObject.put("toId", toId);
		jsonObject.put("timestamp", timestamp);
		this.jsonObject = jsonObject;
	}

	public String getMsgType() {
		return msgType;
	}

	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getFromId() {
		return fromId;
	}

	public void setFromId(String fromId) {
		this.fromId = fromId;
	}

	public String getToId() {
		return toId;
	}

	public void setToId(String toId) {
		this.toId = toId;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public JSONObject getJsonObject() {
		return jsonObject;
	}
}
