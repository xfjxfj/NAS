package com.viegre.nas.pad.entity;

import java.io.Serializable;

/**
 * Created by レインマン on 2021/04/21 15:28 with Android Studio.
 */
public class FtpCmdEntity implements Serializable {

	private String path;

	public FtpCmdEntity() {}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
}
