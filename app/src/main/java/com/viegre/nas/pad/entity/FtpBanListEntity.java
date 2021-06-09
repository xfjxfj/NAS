package com.viegre.nas.pad.entity;

import org.litepal.crud.LitePalSupport;

import java.io.Serializable;

/**
 * Created by レインマン on 2021/06/02 16:47 with Android Studio.
 */
public class FtpBanListEntity extends LitePalSupport implements Serializable {

	private String path;
	private String phoneNum;
	private String type;

	public FtpBanListEntity() {}

	public FtpBanListEntity(String path, String phoneNum, String type) {
		this.path = path;
		this.phoneNum = phoneNum;
		this.type = type;
	}

	public FtpBanListEntity(String path, String phoneNum) {
		this.path = path;
		this.phoneNum = phoneNum;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getPhoneNum() {
		return phoneNum;
	}

	public void setPhoneNum(String phoneNum) {
		this.phoneNum = phoneNum;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
