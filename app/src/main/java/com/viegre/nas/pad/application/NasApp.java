package com.viegre.nas.pad.application;

import com.blankj.utilcode.util.LogUtils;
import com.djangoogle.framework.applicaiton.BaseApplication;
import com.lzx.starrysky.StarrySky;
import com.lzx.starrysky.StarrySkyConfig;
import com.viegre.nas.pad.BuildConfig;
import com.viegre.nas.pad.kalle.converter.JsonConverter;
import com.viegre.nas.pad.manager.AMapLocationManager;
import com.yanzhenjie.kalle.Kalle;
import com.yanzhenjie.kalle.KalleConfig;

import org.litepal.LitePal;

import java.util.concurrent.TimeUnit;

/**
 * Created by レインマン on 2020/09/10 10:21 with Android Studio.
 */
public class NasApp extends BaseApplication {

	@Override
	public void onCreate() {
		super.onCreate();
		LitePal.initialize(this);
		initUtils();
		initKalle();
		initAMap();
		initStarrySky();
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
		                                     .converter(new JsonConverter()).build();
		Kalle.setConfig(kalleConfig);
	}

	/**
	 * 初始化高德地图
	 */
	private void initAMap() {
		AMapLocationManager.INSTANCE.initialize(this);
	}

	/**
	 * 初始化播放器
	 */
	private void initStarrySky() {
		StarrySkyConfig starrySkyConfig = new StarrySkyConfig.Builder().isOpenCache(true).isUserService(true).build();
		StarrySky.init(this, starrySkyConfig, null);
	}
}
