package com.viegre.nas.pad.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.blankj.utilcode.util.BusUtils;
import com.blankj.utilcode.util.LogUtils;
import com.viegre.nas.pad.config.BusConfig;

/**
 * Created by レインマン on 2021/01/22 09:41 with Android Studio.
 */
public class SdcardStateReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		String data = intent.getDataString();
		if (null == action || null == data) {
			return;
		}
		if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
			BusUtils.post(BusConfig.MEDIA_MOUNTED, data);
			LogUtils.iTag(BusConfig.MEDIA_MOUNTED, data + ": 挂载了");
		} else if (Intent.ACTION_MEDIA_UNMOUNTED.equals(action)) {
			BusUtils.post(BusConfig.MEDIA_UNMOUNTED, data);
			LogUtils.iTag(BusConfig.MEDIA_UNMOUNTED, data + ": 卸载了");
		}
	}
}
