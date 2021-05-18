package com.viegre.nas.pad.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;

import com.blankj.utilcode.util.LogUtils;
import com.viegre.nas.pad.config.BusConfig;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by レインマン on 2021/04/07 10:41 AM with Android Studio.
 */
public class UsbReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		switch (intent.getAction()) {
			case UsbManager.ACTION_USB_DEVICE_ATTACHED://USB设备插入
				LogUtils.iTag("UsbReceiver", "USB设备插入");
				EventBus.getDefault().post(BusConfig.USB_DEVICE_ATTACHED);
				break;

			case UsbManager.ACTION_USB_DEVICE_DETACHED://USB设备拔出
				LogUtils.iTag("UsbReceiver", "USB设备拔出");
				EventBus.getDefault().post(BusConfig.USB_DEVICE_DETACHED);
				break;
		}
	}
}
