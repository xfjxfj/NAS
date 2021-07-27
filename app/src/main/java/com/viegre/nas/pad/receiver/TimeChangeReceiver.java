package com.viegre.nas.pad.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.blankj.utilcode.util.SPUtils;
import com.google.gson.Gson;
import com.viegre.nas.pad.config.SPConfig;
import com.viegre.nas.pad.entity.UserTokenTime;
import com.viegre.nas.pad.interceptor.TokenInterceptor;

import java.util.concurrent.TimeUnit;

public class TimeChangeReceiver extends BroadcastReceiver {

	private static final String TAG = "TimeChangeReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "onReceive: " + "时间发生变化");
		String jsonTokenInfo = SPUtils.getInstance().getString(SPConfig.TOKEN_TIME);
		if (!"".equals(jsonTokenInfo)) {
			UserTokenTime userTokenTime = new Gson().fromJson(jsonTokenInfo, UserTokenTime.class);
			Long token_hour_time = Long.valueOf(userTokenTime.getToken_hour_time() * 60);
			long minute = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - userTokenTime.getToken_start_time());
			if (minute > token_hour_time) {
				TokenInterceptor.showTips();
			}
		}
	}
}
