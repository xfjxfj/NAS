package com.viegre.nas.pad.entity;

import org.litepal.crud.LitePalSupport;

import java.io.Serializable;

/**
 * Created by レインマン on 2021/04/22 15:49 with Android Studio.
 */
public class FtpFileFavoritesEntity extends LitePalSupport implements Serializable {

	private String path;

	public FtpFileFavoritesEntity(String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
}
