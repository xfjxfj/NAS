package com.viegre.nas.pad.entity;

import org.litepal.crud.LitePalSupport;

import java.io.Serializable;

/**
 * Created by レインマン on 2021/06/02 16:47 with Android Studio.
 */
public class FtpBanListEntity extends LitePalSupport implements Serializable {

	private String path;
	private String phoneNum;

	public FtpBanListEntity() {}

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
}
