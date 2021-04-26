package com.topqizhi.ai.manager;

import android.app.Application;
import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.widget.Toast;

/**
 * Created by Djangoogle on 2020/10/22 15:11 with Android Studio.
 */
public enum VolumeManager {

	INSTANCE;

	private Application mApplication;
	private AudioManager mAudioManager;
	private int mMaxVolume, mCurrentVolumel = -1;

	public void initialize(Application application) {
		mApplication = application;
		if (null == mAudioManager) {
			mAudioManager = (AudioManager) mApplication.getSystemService(Context.AUDIO_SERVICE);
			mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		}
	}

	public AudioManager getAudioManager() {
		return mAudioManager;
	}

	/**
	 * 增加音量
	 */
	public void enhanceVolume() {
		if (mCurrentVolumel < mMaxVolume) {
			mCurrentVolumel++;
			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mCurrentVolumel, 0);
		} else {
			new Handler().post(() -> Toast.makeText(mApplication, "以达到最大音量", Toast.LENGTH_SHORT).show());
		}
	}

	/**
	 * 减少音量
	 */
	public void lowerVolume() {
		if (mCurrentVolumel > 0) {
			mCurrentVolumel--;
			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mCurrentVolumel, 0);
		} else {
			new Handler().post(() -> Toast.makeText(mApplication, "以达到最小音量", Toast.LENGTH_SHORT).show());
		}
	}

	public int getCurrentVolumel() {
		return mCurrentVolumel;
	}

	public void setCurrentVolumel(int currentVolumel) {
		mCurrentVolumel = currentVolumel;
	}
}
