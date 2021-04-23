package com.viegre.nas.pad.entity;

import org.litepal.crud.LitePalSupport;

import java.io.Serializable;

/**
 * Created by レインマン on 2021/04/22 18:14 with Android Studio.
 */
public class FtpFileObserverEntity extends LitePalSupport implements Serializable {

	private String path;
	private String time;

	public FtpFileObserverEntity(String path, String time) {
		this.path = path;
		this.time = time;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}
}
