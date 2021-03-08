package com.viegre.nas.pad.entity;

import org.litepal.crud.LitePalSupport;

/**
 * Created by レインマン on 2021/03/08 14:50 with Android Studio.
 */
public class ProtocolEntity extends LitePalSupport {

	private String name;
	private String url;

	public ProtocolEntity(String name, String url) {
		this.name = name;
		this.url = url;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
