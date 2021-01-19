package com.viegre.nas.pad.entity;

import org.litepal.crud.LitePalSupport;

import java.io.Serializable;

/**
 * Created by レインマン on 2021/01/19 17:28 with Android Studio.
 */
public class AudioEntity extends LitePalSupport implements Serializable {

	private String number;
	private String name;
	private String singer;
	private String album;
	private String duration;

	public AudioEntity(String number, String name, String singer, String album, String duration) {
		this.number = number;
		this.name = name;
		this.singer = singer;
		this.album = album;
		this.duration = duration;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSinger() {
		return singer;
	}

	public void setSinger(String singer) {
		this.singer = singer;
	}

	public String getAlbum() {
		return album;
	}

	public void setAlbum(String album) {
		this.album = album;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}
}
