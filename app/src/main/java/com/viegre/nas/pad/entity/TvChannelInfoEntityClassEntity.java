package com.viegre.nas.pad.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by レインマン on 2021/04/29 15:56 with Android Studio.
 */
public class TvChannelInfoEntityClassEntity implements Serializable {

	private final List<TvChannelInfoEntity> channel = new ArrayList<>();
	private String channelCategory;

	public TvChannelInfoEntityClassEntity() {}

	public List<TvChannelInfoEntity> getChannel() {
		return channel;
	}

	public String getChannelCategory() {
		return channelCategory;
	}

	public void setChannelCategory(String channelCategory) {
		this.channelCategory = channelCategory;
	}
}
