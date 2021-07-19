package com.topqizhi.andserver.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by レインマン on 2021/07/16 10:49 with Android Studio.
 */
public class HttpFile implements Parcelable {

	private String name;
	private String path;
	private boolean isDir;
	private long size;
	private String createTime;

	public HttpFile() {}

	public HttpFile(String name, String path, boolean isDir, long size, String createTime) {
		this.name = name;
		this.path = path;
		this.isDir = isDir;
		this.size = size;
		this.createTime = createTime;
	}

	protected HttpFile(Parcel in) {
		name = in.readString();
		path = in.readString();
		isDir = in.readByte() != 0;
		size = in.readLong();
		createTime = in.readString();
	}

	public static final Creator<HttpFile> CREATOR = new Creator<HttpFile>() {
		@Override
		public HttpFile createFromParcel(Parcel in) {
			return new HttpFile(in);
		}

		@Override
		public HttpFile[] newArray(int size) {
			return new HttpFile[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int i) {
		parcel.writeString(name);
		parcel.writeString(path);
		parcel.writeByte((byte) (isDir ? 1 : 0));
		parcel.writeLong(size);
		parcel.writeString(createTime);
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

	public boolean isDir() {
		return isDir;
	}

	public void setDir(boolean dir) {
		isDir = dir;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
}
