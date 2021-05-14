package com.viegre.nas.pad.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by レインマン on 2021/05/13 19:36 with Android Studio.
 */
public class LaunchReceiver extends BroadcastReceiver {

	public void onReceive(Context context, Intent intent) {
//		String action = intent.getAction();
//		if (action != null && action.equals(Intent.ACTION_BOOT_COMPLETED)) {
//			try {
//				PackageManager pm = context.getPackageManager();
//				ApplicationInfo ai = pm.getApplicationInfo(AppUtils.getAppPackageName(), 0);
//				if (ai != null) {
//					@SuppressLint("WrongConstant")
//					UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
//					IBinder b = ServiceManager.getService(Context.USB_SERVICE);
//					IUsbManager service = IUsbManager.Stub.asInterface(b);
//					HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
//					for (UsbDevice device : deviceList.values()) {
//						if (device.getVendorId() == 0x0403) {
//							service.grantDevicePermission(device, ai.uid);
//							service.setDevicePackage(device, AppUtils.getAppPackageName());
//						}
//					}
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
	}
}
