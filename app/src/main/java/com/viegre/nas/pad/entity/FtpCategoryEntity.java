package com.viegre.nas.pad.entity;

import java.io.Serializable;

/**
 * Created by レインマン on 2021/05/19 09:17 with Android Studio.
 */
public class FtpCategoryEntity implements Serializable {

	private String name;
	private String path;
	private String createTime;
	private String size;

	public FtpCategoryEntity() {}

	public FtpCategoryEntity(String name, String path, String createTime, String size) {
		this.name = name;
		this.path = path;
		this.createTime = createTime;
		this.size = size;
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

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}
}
