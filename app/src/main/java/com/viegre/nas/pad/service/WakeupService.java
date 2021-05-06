package com.viegre.nas.pad.service;

import android.accessibilityservice.AccessibilityService;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

import com.topqizhi.ai.manager.AIUIManager;
import com.topqizhi.ai.manager.AudioRecordManager;
import com.topqizhi.ai.manager.MscManager;

/**
 * Created by レインマン on 2021/05/06 14:43 with Android Studio.
 */
public class WakeupService extends AccessibilityService {

	@Override
	public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {}

	@Override
	public void onInterrupt() {}

	@Override
	protected boolean onKeyEvent(KeyEvent event) {
		if (KeyEvent.ACTION_DOWN == event.getAction() && KeyEvent.KEYCODE_F11 == event.getKeyCode()) {
			AudioRecordManager.INSTANCE.setTime(System.currentTimeMillis());
			if (MscManager.INSTANCE.isListenHardWakeup()) {
				AudioRecordManager.INSTANCE.stop();
				AIUIManager.INSTANCE.startHardListening();
			}
		}
		return super.onKeyEvent(event);
	}
}
