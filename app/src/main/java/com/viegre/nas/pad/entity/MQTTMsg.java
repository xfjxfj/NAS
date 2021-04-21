package com.viegre.nas.pad.entity;

import com.blankj.utilcode.util.PhoneUtils;

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
	public final static String MSG_FTP_COPY = "ftpCopy";//ftp复制
	public final static String MSG_FTP_DELETE = "ftpDelete";//ftp删除
	public final static String MSG_FTP_DELETE_LIST = "ftpDeleteList";//ftp删除列表
	public final static String MSG_FTP_RESTORE_LIST = "ftpRestoreList";//ftp还原列表
	public final static String MSG_FTP_ERASE = "ftpErase";//ftp文件清除

	private String msgType;
	private String action;
	private String fromId;
	private String toId;
	private long timeStamp;
	private String param;

	public MQTTMsg() {}

	public MQTTMsg(String msgType, String action, String toId) {
		this.msgType = msgType;
		this.action = action;
		this.fromId = PhoneUtils.getSerial();
		this.toId = toId;
		this.timeStamp = System.currentTimeMillis();
	}

	public MQTTMsg(String msgType, String action, String toId, String param) {
		this.msgType = msgType;
		this.action = action;
		this.fromId = PhoneUtils.getSerial();
		this.toId = toId;
		this.timeStamp = System.currentTimeMillis();
		this.param = param;
	}

	public String getMsgType() {
		return msgType;
	}

	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
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

	public long getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	public String getParam() {
		return param;
	}

	public void setParam(String param) {
		this.param = param;
	}
}
