package com.viegre.nas.pad.entity;

import org.litepal.crud.LitePalSupport;

import java.io.Serializable;

/**
 * Created by Djangoogle on 2021/01/13 18:54 with Android Studio.
 */
public class GuideResourceEntity extends LitePalSupport implements Serializable {

	private String filename;
	private String url;
	private boolean isImage;

	public GuideResourceEntity(String filename, String url, boolean isImage) {
		this.filename = filename;
		this.url = url;
		this.isImage = isImage;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
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
