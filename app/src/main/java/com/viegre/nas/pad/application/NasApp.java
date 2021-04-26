package com.viegre.nas.pad.application;

import android.app.ActivityManager;
import android.content.Context;
import android.provider.Settings;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.SPUtils;
import com.didichuxing.doraemonkit.DoraemonKit;
import com.djangoogle.framework.applicaiton.BaseApplication;
import com.topqizhi.ai.manager.AIUIManager;
import com.topqizhi.ai.manager.MscManager;
import com.topqizhi.ai.manager.VolumeManager;
import com.viegre.nas.pad.BuildConfig;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.config.SPConfig;
import com.viegre.nas.pad.kalle.converter.JsonConverter;
import com.viegre.nas.pad.manager.AMapLocationManager;
import com.viegre.nas.pad.service.AppService;
import com.yanzhenjie.kalle.Kalle;
import com.yanzhenjie.kalle.KalleConfig;

import org.litepal.LitePal;
import org.primftpd.log.CsvLoggerFactory;

import java.util.concurrent.TimeUnit;

import cn.wildfire.chat.kit.Config;
import cn.wildfire.chat.kit.WfcUIKit;
import cn.wildfire.chat.kit.conversation.message.viewholder.MessageViewHolderManager;
import cn.wildfire.chat.kit.third.location.viewholder.LocationMessageContentViewHolder;
import cn.wildfirechat.push.PushService;

/**
 * Created by レインマン on 2020/09/10 10:21 with Android Studio.
 */
public class NasApp extends BaseApplication {

	@Override
	public void onCreate() {
		super.onCreate();
		DoraemonKit.install(this, "6f1d65f538aa1fe2c813e712c95b773d");
		initUtils();
		initAndroidId();
		LitePal.initialize(this);
		MscManager.INSTANCE.initialize(this);
		AIUIManager.INSTANCE.initialize(this);
		VolumeManager.INSTANCE.initialize(this);
		CsvLoggerFactory.CONTEXT = this;
		initKalle();
		initAMap();
		initIM();
	}

	/**
	 * 音视频初始化
	 */
	private void initIM() {
		if (getCurProcessName(this).equals(BuildConfig.APPLICATION_ID)) {
			// 如果uikit是以aar的方式引入 ，那么需要在此对Config里面的属性进行配置，如：
			// Config.IM_SERVER_HOST = "im.example.com";
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

	private void setupWFCDirs() {
		FileUtils.createOrExistsDir(Config.VIDEO_SAVE_DIR);
		FileUtils.createOrExistsDir(Config.AUDIO_SAVE_DIR);
		FileUtils.createOrExistsDir(Config.FILE_SAVE_DIR);
		FileUtils.createOrExistsDir(Config.PHOTO_SAVE_DIR);
	}

	public static String getCurProcessName(Context context) {
		int pid = android.os.Process.myPid();

		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

		for (ActivityManager.RunningAppProcessInfo appProcess : activityManager.getRunningAppProcesses()) {

			if (appProcess.pid == pid) {
				return appProcess.processName;
			}
		}
		return null;
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

	/**
	 * 初始化Kalle
	 */
	private void initKalle() {
//		CacheStore cacheStore = DiskCacheStore.newBuilder("/sdcard").password("t0PqIzHI@COM").build();
		KalleConfig kalleConfig = KalleConfig.newBuilder()
		                                     .connectionTimeout(15, TimeUnit.SECONDS)
		                                     .readTimeout(15, TimeUnit.SECONDS)
		                                     .converter(new JsonConverter())
		                                     .build();
		Kalle.setConfig(kalleConfig);
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
