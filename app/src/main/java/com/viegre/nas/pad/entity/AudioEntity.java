package com.viegre.nas.pad.entity;

import org.litepal.crud.LitePalSupport;

import java.io.Serializable;

/**
 * Created by レインマン on 2021/01/19 17:28 with Android Studio.
 */
public class AudioEntity extends LitePalSupport implements Serializable {

	private String fileName;
	private String title;
	private int duration;
	private String artist;
	private String album;
	private String year;
	private String type;
	private String size;
	private String fileUrl;

	public AudioEntity() {}

	public AudioEntity(String fileName, String title, int duration, String artist, String album, String year, String type, String size, String fileUrl) {
		this.fileName = fileName;
		this.title = title;
		this.duration = duration;
		this.artist = artist;
		this.album = album;
		this.year = year;
		this.type = type;
		this.size = size;
		this.fileUrl = fileUrl;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
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

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getFileUrl() {
		return fileUrl;
	}

	public void setFileUrl(String fileUrl) {
		this.fileUrl = fileUrl;
	}
}
