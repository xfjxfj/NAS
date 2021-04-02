package com.viegre.nas.pad.entity;

/**
 * Created by レインマン on 2021/04/02 10:18 with Android Studio.
 */
public class FileEntity {

	public enum Type {
		STORAGE,
		DIR,
		FILE,
		UNKNOWN
	}

	private String name;
	private String path;
	private Type type;
	private boolean isCheck = false;

	public FileEntity(String name, String path, Type type) {
		this.name = name;
		this.path = path;
		this.type = type;
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

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public boolean isCheck() {
		return isCheck;
	}

	public void setCheck(boolean check) {
		isCheck = check;
	}
}
