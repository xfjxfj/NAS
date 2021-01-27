package com.viegre.nas.pad.entity;

import org.litepal.crud.LitePalSupport;

import java.io.Serializable;

/**
 * Created by レインマン on 2021/01/25 1:19 AM with Android Studio.
 */
public class VideoEntity extends LitePalSupport implements Serializable {

	private String name;
	private String suffix;
	private String path;

	public VideoEntity(String name, String suffix, String path) {
		this.name = name;
		this.suffix = suffix;
		this.path = path;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSuffix() {
		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
}
