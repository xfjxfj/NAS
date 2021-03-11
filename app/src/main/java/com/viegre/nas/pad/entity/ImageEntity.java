package com.viegre.nas.pad.entity;

import org.litepal.crud.LitePalSupport;

/**
 * Created by レインマン on 2021/01/24 11:35 PM with Android Studio.
 */
public class ImageEntity extends LitePalSupport {

	private String path;
	private String album;

	public ImageEntity(String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getAlbum() {
		return album;
	}

	public void setAlbum(String album) {
		this.album = album;
	}
}
