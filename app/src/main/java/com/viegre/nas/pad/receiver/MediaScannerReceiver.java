package com.viegre.nas.pad.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.blankj.utilcode.util.LogUtils;

/**
 * Created by レインマン on 2021/01/28 11:19 with Android Studio.
 */
public class MediaScannerReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (Intent.ACTION_MEDIA_SCANNER_STARTED.equals(intent.getAction())) {
			LogUtils.iTag(MediaScannerReceiver.class.getSimpleName(), "媒体库扫描开始");
		} else if (Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(intent.getAction())) {
			LogUtils.iTag(MediaScannerReceiver.class.getSimpleName(), "媒体库扫描完毕");
		}
	}
}
