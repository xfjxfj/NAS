package com.viegre.nas.pad.entity;

import java.io.Serializable;

/**
 * Created by レインマン on 2021/04/26 10:17 with Android Studio.
 */
public class ExternalDriveEntity implements Serializable {

	private String name;
	private String path;

	public ExternalDriveEntity() {}

	public ExternalDriveEntity(String name, String path) {
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
