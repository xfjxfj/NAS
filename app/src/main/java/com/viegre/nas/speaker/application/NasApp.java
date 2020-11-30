package com.viegre.nas.speaker.application;

import android.app.Application;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.Utils;
import com.viegre.nas.speaker.BuildConfig;

/**
 * Created by Djangoogle on 2020/09/10 10:21 with Android Studio.
 */
public class NasApp extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		initUtils();
	}

	/**
	 * 初始化工具类设置
	 */
	private void initUtils() {
		Utils.init(this);
		LogUtils.getConfig().setLogSwitch(BuildConfig.DEBUG);
		LogUtils.getConfig().setConsoleSwitch(BuildConfig.DEBUG);
		LogUtils.getConfig().setGlobalTag(NasApp.class.getSimpleName());
		LogUtils.getConfig().setLogHeadSwitch(true);
		LogUtils.getConfig().setBorderSwitch(false);
	}
}
