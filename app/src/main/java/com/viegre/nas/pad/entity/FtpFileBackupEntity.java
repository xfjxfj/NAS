package com.viegre.nas.pad.entity;

import org.litepal.crud.LitePalSupport;

import java.io.Serializable;

/**
 * Created by レインマン on 2021/06/18 11:02 with Android Studio.
 */
public class FtpFileBackupEntity extends LitePalSupport implements Serializable {

	private String path;
	private String phoneNum;

	public FtpFileBackupEntity() {}

	public FtpFileBackupEntity(String path, String phoneNum) {
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
