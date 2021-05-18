package com.viegre.nas.pad.manager;

import com.viegre.nas.pad.config.BusConfig;
import com.viegre.nas.pad.entity.AudioEntity;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by レインマン on 2021/01/25 11:04 with Android Studio.
 */
public enum AudioPlayListManager {

	INSTANCE;

	private final List<AudioEntity> mList = new ArrayList<>();
	private int mPosition = -1, mPreviousPosition = -1;

	public List<AudioEntity> getList() {
		return mList;
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
		if (mPosition < 0) {
			mPreviousPosition = position;
		} else {
			mPreviousPosition = mPosition;
		}
		mPosition = position;
		EventBus.getDefault().post(BusConfig.UPDATE_AUDIO_PLAY_LIST);
	}

	public int getPreviousPosition() {
		return mPreviousPosition;
	}
}
