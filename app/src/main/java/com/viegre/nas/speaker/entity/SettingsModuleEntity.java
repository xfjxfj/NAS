package com.viegre.nas.speaker.entity;

import com.viegre.nas.speaker.entity.base.BaseEntity;

/**
 * Created by Djangoogle on 2020/12/16 16:58 with Android Studio.
 */
public class SettingsModuleEntity extends BaseEntity {

	private int resId;
	private String name;
	private String tag;
	private boolean selected = false;

	public SettingsModuleEntity(int resId, String name, String tag) {
		this.resId = resId;
		this.name = name;
		this.tag = tag;
	}

	public int getResId() {
		return resId;
	}

	public void setResId(int resId) {
		this.resId = resId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}
}
