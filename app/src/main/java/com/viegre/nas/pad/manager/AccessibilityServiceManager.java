package com.viegre.nas.pad.manager;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

import java.util.List;

/**
 * Created by レインマン on 2021/05/08 17:34 with Android Studio.
 */
public enum AccessibilityServiceManager {

	INSTANCE;

	/**
	 * 跳转到设置页面无障碍服务开启自定义辅助功能服务
	 *
	 * @param context
	 */
	public void gotoSettings(Context context) {
		Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

	/**
	 * 判断自定义辅助功能服务是否开启
	 *
	 * @param context
	 * @param className
	 * @return
	 */
	public boolean isOn(Context context, String className) {
		if (context == null) {
			return false;
		}
		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		if (null != activityManager) {
			List<ActivityManager.RunningServiceInfo> runningServices = activityManager.getRunningServices(100);// 获取正在运行的服务列表
			if (runningServices.isEmpty()) {
				return false;
			}
			for (int i = 0; i < runningServices.size(); i++) {
				ComponentName service = runningServices.get(i).service;
				if (service.getClassName().equals(className)) {
					return true;
				}
			}
		}
		return false;
	}
}
