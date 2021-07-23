package com.viegre.nas.pad.entity;

import java.io.Serializable;

/**
 * Created by レインマン on 2021/05/19 09:17 with Android Studio.
 */
public class FtpCategoryEntity implements Serializable {

	private String name;
	private String path;
	private String createTime;
	private long size;
	private String src;
	private boolean pick = false;

	public FtpCategoryEntity() {}

	public FtpCategoryEntity(String name, String path, String createTime, long size, String src, boolean pick) {
		this.name = name;
		this.path = path;
		this.createTime = createTime;
		this.size = size;
		this.src = src;
		this.pick = pick;
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

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public String getSrc() {
		return src;
	}

	public void setSrc(String src) {
		this.src = src;
	}

	public boolean isPick() {
		return pick;
	}

	public void setPick(boolean pick) {
		this.pick = pick;
	}
}
