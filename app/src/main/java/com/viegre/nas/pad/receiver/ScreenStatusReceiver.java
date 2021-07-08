package com.viegre.nas.pad.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.blankj.utilcode.util.LogUtils;
import com.viegre.nas.pad.activity.ScreenSaverActivity;

/**
 * Created by レインマン on 2021/03/09 17:17 with Android Studio.
 */
public class ScreenStatusReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {//屏幕休眠
			LogUtils.dTag("ScreenStatusReceiver", Intent.ACTION_SCREEN_OFF);
			try {
				Intent i = new Intent();
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				i.setClass(context, ScreenSaverActivity.class);
				context.startActivity(i);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
