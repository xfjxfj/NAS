package com.viegre.nas.pad.entity;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

/**
 * Created by レインマン on 2021/01/19 17:28 with Android Studio.
 */
public class AudioEntity extends LitePalSupport {

	private int _id;
	private String name;
	private String suffix;
	private String artist;
	private String albumName;
	private String albumImage;
	private int duration;
	private String path;
	@Column(ignore = true) private boolean isChecked;

	public AudioEntity(int _id, String name, String suffix, String artist, String albumName, String albumImage, int duration, String path, boolean isChecked) {
		this._id = _id;
		this.name = name;
		this.suffix = suffix;
		this.artist = artist;
		this.albumName = albumName;
		this.albumImage = albumImage;
		this.duration = duration;
		this.path = path;
		this.isChecked = isChecked;
	}

	public int get_id() {
		return _id;
	}

	public void set_id(int _id) {
		this._id = _id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSuffix() {
		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public String getAlbumName() {
		return albumName;
	}

	public void setAlbumName(String albumName) {
		this.albumName = albumName;
	}

	public String getAlbumImage() {
		return albumImage;
	}

	public void setAlbumImage(String albumImage) {
		this.albumImage = albumImage;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public boolean isChecked() {
		return isChecked;
	}

	public void setChecked(boolean checked) {
		isChecked = checked;
	}
}
