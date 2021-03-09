package com.viegre.nas.pad.activity;

import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.content.Context;
import android.os.PowerManager;

import com.djangoogle.framework.activity.BaseActivity;
import com.viegre.nas.pad.databinding.ActivityScreenSaverBinding;

/**
 * Created by レインマン on 2021/03/09 17:35 with Android Studio.
 */
public class ScreenSaverActivity extends BaseActivity<ActivityScreenSaverBinding> {

	private PowerManager.WakeLock mWakeLock;
	private KeyguardManager.KeyguardLock mKeyguardLock;

	@SuppressLint("ClickableViewAccessibility")
	@Override
	protected void initialize() {
		initWakeLock();
		mViewBinding.getRoot().setOnTouchListener((view, motionEvent) -> {
			mKeyguardLock.disableKeyguard();
			finish();
			return false;
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (null != mWakeLock) {
			mWakeLock.acquire();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (null != mWakeLock && mWakeLock.isHeld()) {
			mWakeLock.release();
		}
	}

	@SuppressLint("WakelockTimeout")
	private void initWakeLock() {
		PowerManager powerManager = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
		mWakeLock = powerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE,
		                                     "nas:screensaver");
		KeyguardManager keyguardManager = (KeyguardManager) getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
		mKeyguardLock = keyguardManager.newKeyguardLock("nas:keyguardLocker");
	}

	@Override
	public void onBackPressed() {
		finish();
	}
}
