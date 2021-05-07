package com.topqizhi.ai.manager;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.iflytek.cloud.Setting;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.VoiceWakeuper;
import com.iflytek.cloud.WakeuperListener;
import com.iflytek.cloud.WakeuperResult;
import com.iflytek.cloud.util.ResourceUtil;
import com.topqizhi.ai.R;
import com.topqizhi.ai.entity.WakeuperResultEntity;
import com.topqizhi.ai.impl.WakeuperResultListener;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by レインマン on 2021/04/13 15:49 with Android Studio.
 */
public enum MscManager {

	INSTANCE;

	public static final boolean IS_HARD_WAKEUP = true;

	private static final String TAG = MscManager.class.getSimpleName();

	/**
	 * 主要分为两种：唤醒（wakeup），唤醒识别（oneshot）
	 * 默认：wakeup
	 */
	private static final String IVW_SST = "wakeup";

	/**
	 * 唤醒门限值
	 * 门限值越高，则要求匹配度越高，才能唤醒
	 * 值范围：[0，3000]
	 * 默认值：1450
	 */
	private static final int IVW_THRESHOLD = 1300;

	/**
	 * 持续唤醒
	 * 持续唤醒支持参数：
	 * 0：单次唤醒
	 * 1：循环唤醒
	 * 默认值：0
	 */
	private static final String KEEP_ALIVE = "0";

	/**
	 * 唤醒资源路径
	 * 唤醒需要使用本地资源，通过此参数设置本地资源所在的路径
	 * 值范围：有效的资源文件路径
	 * 默认值：null
	 */
	private static final String IVW_RES_PATH = null;

	/**
	 * 唤醒闭环优化模式
	 * 优化模式支持参数：
	 * 0：关闭优化功能
	 * 1：开启优化功能
	 * 默认值：0
	 */
	private static final String IVW_NET_MODE = "0";

	//语音唤醒对象
	private VoiceWakeuper mVoiceWakeuper;
	//语音唤醒是否在运行
	private boolean mIsVoiceWakeuperRunning = false;
	//是否监听硬件唤醒
	private boolean mIsListenHardWakeup = false;

	public void initialize(Context applicationContext) {
		String param = SpeechConstant.APPID + "=" + applicationContext.getString(R.string.app_id) + "," + SpeechConstant.ENGINE_MODE + "=" + SpeechConstant.MODE_MSC;
		SpeechUtility.createUtility(applicationContext, param);
		Setting.setShowLog(false);
		mVoiceWakeuper = VoiceWakeuper.createWakeuper(applicationContext, i -> Log.i(TAG, "createWakeuper: " + i));
		if (null != mVoiceWakeuper) {
			//清空参数
			mVoiceWakeuper.setParameter(SpeechConstant.PARAMS, null);
			//唤醒门限值，根据资源携带的唤醒词个数按照“id:门限;id:门限”的格式传入
			mVoiceWakeuper.setParameter(SpeechConstant.IVW_THRESHOLD, "0:" + (IS_HARD_WAKEUP ? 3000 : IVW_THRESHOLD));
			//设置唤醒模式
			mVoiceWakeuper.setParameter(SpeechConstant.IVW_SST, IVW_SST);
			//设置持续进行唤醒
			mVoiceWakeuper.setParameter(SpeechConstant.KEEP_ALIVE, KEEP_ALIVE);
			//设置闭环优化网络模式
			mVoiceWakeuper.setParameter(SpeechConstant.IVW_NET_MODE, IVW_NET_MODE);
			//设置唤醒资源路径
			mVoiceWakeuper.setParameter(SpeechConstant.IVW_RES_PATH, getResource(applicationContext));
		}
	}

	public void startListening(WakeuperResultListener wakeuperResultListener) {
		if (mIsVoiceWakeuperRunning) {
			return;
		}
		if (null != mVoiceWakeuper) {
			mIsVoiceWakeuperRunning = true;
			mVoiceWakeuper.startListening(new WakeuperListener() {
				@Override
				public void onBeginOfSpeech() {}

				@Override
				public void onResult(WakeuperResult wakeuperResult) {
					Log.d(TAG, "onResult");
					try {
						String text = wakeuperResult.getResultString();
						JSONObject object;
						object = new JSONObject(text);
						WakeuperResultEntity wakeuperResultEntity = new WakeuperResultEntity(true,
						                                                                     text,
						                                                                     object.optString("sst"),
						                                                                     object.optString("id"),
						                                                                     object.optString("score"),
						                                                                     object.optString("bos"),
						                                                                     object.optString("eos"),
						                                                                     object.optString("keyword"));
						wakeuperResultListener.result(wakeuperResultEntity);
					} catch (JSONException e) {
						WakeuperResultEntity wakeuperResultEntity = new WakeuperResultEntity(false, "结果解析出错");
						wakeuperResultListener.result(wakeuperResultEntity);
						e.printStackTrace();
					} finally {
						mIsVoiceWakeuperRunning = false;
					}
				}

				@Override
				public void onError(SpeechError speechError) {
					WakeuperResultEntity wakeuperResultEntity = new WakeuperResultEntity(false,
					                                                                     speechError.getPlainDescription(true));
					wakeuperResultListener.result(wakeuperResultEntity);
					mIsVoiceWakeuperRunning = false;
				}

				@Override
				public void onEvent(int i, int i1, int i2, Bundle bundle) {}

				@Override
				public void onVolumeChanged(int i) {}
			});
		} else {
			mIsVoiceWakeuperRunning = false;
		}
	}

	public void stopListening() {
		mIsListenHardWakeup = false;
		if (null != mVoiceWakeuper) {
			mVoiceWakeuper.stopListening();
		}
	}

	public void release() {
		mVoiceWakeuper = VoiceWakeuper.getWakeuper();
		if (null != mVoiceWakeuper) {
			mVoiceWakeuper.destroy();
		}
	}

	private String getResource(Context context) {
		return ResourceUtil.generateResourcePath(context, ResourceUtil.RESOURCE_TYPE.assets, "ivw/ivw.jet");
	}

	public boolean isListenHardWakeup() {
		return mIsListenHardWakeup;
	}

	public void setListenHardWakeup(boolean listenHardWakeup) {
		mIsListenHardWakeup = listenHardWakeup;
	}
}
