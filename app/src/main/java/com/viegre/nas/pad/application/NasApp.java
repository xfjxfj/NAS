package com.viegre.nas.pad.application;

import android.app.ActivityManager;
import android.content.Context;
import android.provider.Settings;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.SPUtils;
import com.djangoogle.framework.applicaiton.BaseApplication;
import com.djangoogle.framework.manager.OkHttpManager;
import com.shuyu.gsyvideoplayer.player.PlayerFactory;
import com.topqizhi.ai.manager.AIUIManager;
import com.topqizhi.ai.manager.AudioRecordManager;
import com.topqizhi.ai.manager.MscManager;
import com.topqizhi.ai.manager.VolumeManager;
import com.viegre.nas.pad.BuildConfig;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.config.SPConfig;
import com.viegre.nas.pad.manager.AMapLocationManager;
import com.viegre.nas.pad.service.AppService;

import org.litepal.LitePal;

import cn.wildfire.chat.kit.Config;
import cn.wildfire.chat.kit.WfcUIKit;
import cn.wildfire.chat.kit.conversation.message.viewholder.MessageViewHolderManager;
import cn.wildfire.chat.kit.third.location.viewholder.LocationMessageContentViewHolder;
import cn.wildfirechat.push.PushService;
import rxhttp.RxHttpPlugins;
import tv.danmaku.ijk.media.exo2.Exo2PlayerManager;

/**
 * Created by レインマン on 2020/09/10 10:21 with Android Studio.
 */
public class NasApp extends BaseApplication {

	@Override
	public void onCreate() {
		super.onCreate();
		initUtils();
		initAndroidId();
		LitePal.initialize(this);
		initRxHttp();
		MscManager.INSTANCE.initialize(this);
		AIUIManager.INSTANCE.initialize(this);
		VolumeManager.INSTANCE.initialize(this);
		PlayerFactory.setPlayManager(Exo2PlayerManager.class);
		AudioRecordManager.INSTANCE.initialize();
		initAMap();
		initIM();
	}

	/**
	 * 音视频初始化
	 */
	private void initIM() {
		AppService.validateConfig(this);
		if (BuildConfig.APPLICATION_ID.equals(getCurProcessName(this))) {
			//如果uikit是以aar的方式引入 ，那么需要在此对Config里面的属性进行配置，如：
			//Config.IM_SERVER_HOST = "im.example.com";
			WfcUIKit wfcUIKit = WfcUIKit.getWfcUIKit();
			wfcUIKit.init(this);
			wfcUIKit.setAppServiceProvider(AppService.Instance());
			PushService.init(this, BuildConfig.APPLICATION_ID);
			MessageViewHolderManager.getInstance()
			                        .registerMessageViewHolder(LocationMessageContentViewHolder.class,
			                                                   R.layout.conversation_item_location_send,
			                                                   R.layout.conversation_item_location_send);
			setupWFCDirs();
		}
	}

	private String getCurProcessName(Context context) {
		int pid = android.os.Process.myPid();

		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

		for (ActivityManager.RunningAppProcessInfo appProcess : activityManager.getRunningAppProcesses()) {

			if (appProcess.pid == pid) {
				return appProcess.processName;
			}
		}
		return null;
	}

	private void setupWFCDirs() {
		FileUtils.createOrExistsDir(Config.VIDEO_SAVE_DIR);
		FileUtils.createOrExistsDir(Config.AUDIO_SAVE_DIR);
		FileUtils.createOrExistsDir(Config.FILE_SAVE_DIR);
		FileUtils.createOrExistsDir(Config.PHOTO_SAVE_DIR);
	}

	/**
	 * 初始化工具类设置
	 */
	private void initUtils() {
		LogUtils.getConfig().setLogSwitch(BuildConfig.DEBUG);
		LogUtils.getConfig().setConsoleSwitch(BuildConfig.DEBUG);
		LogUtils.getConfig().setGlobalTag(NasApp.class.getSimpleName());
		LogUtils.getConfig().setLogHeadSwitch(true);
		LogUtils.getConfig().setBorderSwitch(false);
	}

	private void initRxHttp() {
		RxHttpPlugins.init(OkHttpManager.INSTANCE.getOkHttpClient()).setDebug(BuildConfig.DEBUG);
	}

	/**
	 * 初始化高德地图
	 */
	private void initAMap() {
		AMapLocationManager.INSTANCE.initialize(this);
	}

	private void initAndroidId() {
		if (!SPUtils.getInstance().contains(SPConfig.ANDROID_ID)) {
			SPUtils.getInstance().put(SPConfig.ANDROID_ID, Settings.System.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
		}
	}
}
