package com.viegre.nas.pad.entity;

import org.litepal.crud.LitePalSupport;

import java.io.Serializable;

/**
 * Created by レインマン on 2021/04/22 15:49 with Android Studio.
 */
public class FtpFavoritesEntity extends LitePalSupport implements Serializable {

	private String path;
	private String time;
	private String size;

	public FtpFavoritesEntity() {}

	public FtpFavoritesEntity(String path, String time, String size) {
		this.path = path;
		this.time = time;
		this.size = size;
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

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}
}
