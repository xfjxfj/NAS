package com.viegre.nas.speaker.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.blankj.utilcode.util.SPUtils;
import com.viegre.nas.speaker.config.SPConfig;

/**
 * Created by Djangoogle on 2020/11/26 16:54 with Android Studio.
 */
public class BootReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		//判断是否为开机启动
		if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
			SPUtils.getInstance().put(SPConfig.SP_IS_BOOT, true);
		}
	}
}
