package com.viegre.nas.pad.entity;

import org.litepal.crud.LitePalSupport;

/**
 * Created by レインマン on 2021/01/25 1:19 AM with Android Studio.
 */
public class VideoEntity extends LitePalSupport {

	private String displayName;
	private String path;

	public VideoEntity(String displayName, String path) {
		this.displayName = displayName;
		this.path = path;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
}
