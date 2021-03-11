package com.viegre.nas.pad.entity;

import org.litepal.crud.LitePalSupport;

/**
 * Created by レインマン on 2021/03/11 8:22 PM with Android Studio.
 */
public class ImageAlbumEntity extends LitePalSupport {

	private String _id;
	private String name;
	private long coverID;
	public int count = 0;

	public String get_id() {
		return _id;
	}

	public void set_id(String _id) {
		this._id = _id;
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
}
