package com.viegre.nas.pad.entity;

import org.litepal.crud.LitePalSupport;

import java.io.Serializable;

/**
 * Created by レインマン on 2021/01/19 17:28 with Android Studio.
 */
public class AudioEntity extends LitePalSupport implements Serializable {

	private String number;
	private String name;
	private String artist;
	private String album;
	private String duration;

	public AudioEntity(String number, String name, String artist, String album, String duration) {
		this.number = number;
		this.name = name;
		this.artist = artist;
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

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}
}
