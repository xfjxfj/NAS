package com.viegre.nas.pad.service;

import android.accessibilityservice.AccessibilityService;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.Utils;
import com.topqizhi.ai.manager.AIUIManager;
import com.topqizhi.ai.manager.AudioRecordManager;
import com.topqizhi.ai.manager.MscManager;

import java.util.List;

/**
 * Created by レインマン on 2021/05/06 14:43 with Android Studio.
 */
public class WakeupService extends AccessibilityService {

	@Override
	public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {}

	@Override
	public void onInterrupt() {}

	@Override
	protected void onServiceConnected() {
		super.onServiceConnected();
		//获取ActivityManager
		ActivityManager mAm = (ActivityManager) Utils.getApp().getSystemService(Context.ACTIVITY_SERVICE);
		// 获得当前运行的task
		List<ActivityManager.RunningTaskInfo> taskList = mAm.getRunningTasks(100);
		for (ActivityManager.RunningTaskInfo rti : taskList) {
			//找到当前应用的task，并启动task的栈顶activity，达到程序切换到前台
			if (rti.topActivity.getPackageName().equals(Utils.getApp().getPackageName())) {
				//判断app进程是否存活
				Log.i("NotificationReceiver", "the app process is alive");
				try {
					Intent resultIntent = new Intent(Utils.getApp(), Class.forName(rti.topActivity.getClassName()));
					resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
					Utils.getApp().startActivity(resultIntent);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
//				AIUIManager.INSTANCE.startListening();
				return;
			}
		}
		//若没有找到运行的task，用户结束了task或被系统释放，则重新启动mainactivity
		ActivityUtils.startLauncherActivity(Utils.getApp().getPackageName());
	}

	@Override
	protected boolean onKeyEvent(KeyEvent event) {
		if (KeyEvent.ACTION_DOWN == event.getAction() && KeyEvent.KEYCODE_F11 == event.getKeyCode()) {
			if (MscManager.INSTANCE.isListenHardWakeup()) {
				AudioRecordManager.INSTANCE.stop();
				AIUIManager.INSTANCE.startHardListening();
			}
		}
		return super.onKeyEvent(event);
	}
}
