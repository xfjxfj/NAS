package com.topqizhi.ai.entity;

import java.io.Serializable;

/**
 * Created by レインマン on 2021/04/13 16:45 with Android Studio.
 */
public class WakeuperResultEntity implements Serializable {

	private boolean isSuccess;
	//全文
	private String raw;
	/**
	 * 操作类型
	 * 本次业务标识：wakeup表示语音唤醒，oneshot表示唤醒+识别
	 */
	private String sst;
	//唤醒词id
	private String id;
	//当前唤醒得分，只有当分数大于等于设置的门限值时才会回调唤醒结果
	private String score;
	//当前唤醒音频的前端点，即当前唤醒音频在写入的总音频中的开始时间位置，单位:ms
	private String bos;
	//当前唤醒音频的尾端点，即当前唤醒音频在写入的总音频中的结束时间位置，单位:ms
	private String eos;
	//当前唤醒词，若是中文唤醒词会自动以拼音形式显示
	private String keyword;

	public WakeuperResultEntity(boolean isSuccess, String raw) {
		this.isSuccess = isSuccess;
		this.raw = raw;
	}

	public WakeuperResultEntity(boolean isSuccess, String raw, String sst, String id, String score, String bos, String eos, String keyword) {
		this.isSuccess = isSuccess;
		this.raw = raw;
		this.sst = sst;
		this.id = id;
		this.score = score;
		this.bos = bos;
		this.eos = eos;
		this.keyword = keyword;
	}

	public boolean isSuccess() {
		return isSuccess;
	}

	public void setSuccess(boolean success) {
		this.isSuccess = success;
	}

	public String getRaw() {
		return raw;
	}

	public void setRaw(String raw) {
		this.raw = raw;
	}

	public String getSst() {
		return sst;
	}

	public void setSst(String sst) {
		this.sst = sst;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getScore() {
		return score;
	}

	public void setScore(String score) {
		this.score = score;
	}

	public String getBos() {
		return bos;
	}

	public void setBos(String bos) {
		this.bos = bos;
	}

	public String getEos() {
		return eos;
	}

	public void setEos(String eos) {
		this.eos = eos;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
}
