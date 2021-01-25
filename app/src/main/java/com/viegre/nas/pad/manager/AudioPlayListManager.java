package com.viegre.nas.pad.manager;

import com.viegre.nas.pad.entity.AudioEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by レインマン on 2021/01/25 11:04 with Android Studio.
 */
public enum AudioPlayListManager {

	INSTANCE;

	private final List<AudioEntity> mList = new ArrayList<>();
	private int mPosition = -1;

	public List<AudioEntity> getList() {
		return mList;
	}

	public void resetPostion() {
		mPosition = 0;
	}

	public void addPostion() {
		mPosition++;
	}

	public void cutPostion() {
		mPosition--;
	}

	public int getPosition() {
		return mPosition;
	}

	public void setPosition(int position) {
		mPosition = position;
	}
}
