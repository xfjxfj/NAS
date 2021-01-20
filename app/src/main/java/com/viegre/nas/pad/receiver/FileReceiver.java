package com.viegre.nas.pad.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.blankj.utilcode.util.BusUtils;
import com.viegre.nas.pad.config.BusConfig;

/**
 * Created by レインマン on 2021/01/20 9:52 PM with Android Studio.
 */
public class FileReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		if (Intent.ACTION_MEDIA_SCANNER_STARTED.equals(intent.getAction())) {
			BusUtils.post(BusConfig.MEDIA_SCANNER_STARTED);
		} else if (Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(intent.getAction())) {
			BusUtils.post(BusConfig.MEDIA_SCANNER_FINISHED);
		}
	}
}
