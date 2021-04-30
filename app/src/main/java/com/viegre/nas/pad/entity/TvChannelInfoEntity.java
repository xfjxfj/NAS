package com.viegre.nas.pad.entity;

import java.io.Serializable;

/**
 * Created by レインマン on 2021/04/29 15:54 with Android Studio.
 */
public class TvChannelInfoEntity implements Serializable {

	private String channelName;
	private String channelId;

	public TvChannelInfoEntity() {}

	public String getChannelName() {
		return channelName;
	}

	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}

	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}
}
