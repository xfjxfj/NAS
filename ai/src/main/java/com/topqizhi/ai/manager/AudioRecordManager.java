package com.topqizhi.ai.manager;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

/**
 * Created by レインマン on 2021/05/06 13:10 with Android Studio.
 */
public enum AudioRecordManager {

	INSTANCE;

	private AudioRecord mAudioRecord;

	public void initialize() {
		int recordBufSize = AudioRecord.getMinBufferSize(16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
		mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
		                               16000,
		                               AudioFormat.CHANNEL_IN_MONO,
		                               AudioFormat.ENCODING_PCM_16BIT,
		                               recordBufSize);
	}

	public void startRecord() {
		if (null != mAudioRecord) {
			mAudioRecord.startRecording();
		}
	}

	public void stop() {
		if (null != mAudioRecord) {
			mAudioRecord.stop();
		}
	}

	public void release() {
		if (null != mAudioRecord) {
			mAudioRecord.stop();
			mAudioRecord.release();
			mAudioRecord = null;
		}
	}
}
