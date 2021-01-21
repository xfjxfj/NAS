package com.viegre.nas.pad.entity;

import org.litepal.crud.LitePalSupport;

/**
 * Created by レインマン on 2021/01/19 17:28 with Android Studio.
 */
public class AudioEntity extends LitePalSupport {

	private int _id;
	private String displayName;
	private String artist;
	private String album;
	private int duration;
	private String path;
	private boolean isSelected;

	public AudioEntity(int _id, String displayName, String artist, String album, int duration, String path) {
		this._id = _id;
		this.displayName = displayName;
		this.artist = artist;
		this.album = album;
		this.duration = duration;
		this.path = path;
	}

	public int get_id() {
		return _id;
	}

	public void set_id(int _id) {
		this._id = _id;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public String getAlbum() {
		return album;
	}

	public void setAlbum(String album) {
		this.album = album;
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

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean selected) {
		isSelected = selected;
	}
}
