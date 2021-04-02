package com.viegre.nas.pad.entity;

/**
 * Created by レインマン on 2021/04/02 10:18 with Android Studio.
 */
public class ExternalStorageEntity {

	private String name;
	private String path;

	public ExternalStorageEntity(String name, String path) {
		this.name = name;
		this.path = path;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
}
