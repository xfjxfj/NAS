package com.viegre.nas.pad.entity;

import org.litepal.crud.LitePalSupport;

import java.io.Serializable;

/**
 * Created by Djangoogle on 2021/01/13 18:54 with Android Studio.
 */
public class GuideResourceEntity extends LitePalSupport implements Serializable {

	private String fileName;
	private String url;
	private boolean isImage;

	public GuideResourceEntity(String fileName, String url, boolean isImage) {
		this.fileName = fileName;
		this.url = url;
		this.isImage = isImage;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public boolean isImage() {
		return isImage;
	}

	public void setImage(boolean image) {
		isImage = image;
	}
}
