package com.viegre.nas.pad.entity;

import org.litepal.crud.LitePalSupport;

import java.util.TreeSet;

/**
 * Created by レインマン on 2021/03/11 8:22 PM with Android Studio.
 */
public class ImageAlbumEntity extends LitePalSupport {

	private String bucketId;
	private String name;
	private long coverID;
	private final TreeSet<String> imageSet = new TreeSet<>();
	private boolean check = false;

	public String getBucketId() {
		return bucketId;
	}

	public void setBucketId(String bucketId) {
		this.bucketId = bucketId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getCoverID() {
		return coverID;
	}

	public void setCoverID(long coverID) {
		this.coverID = coverID;
	}

	public TreeSet<String> getImageSet() {
		return imageSet;
	}

	public boolean isCheck() {
		return check;
	}

	public void setCheck(boolean check) {
		this.check = check;
	}
}
