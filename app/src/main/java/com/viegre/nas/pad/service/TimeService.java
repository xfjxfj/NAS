package com.viegre.nas.pad.service;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import com.viegre.nas.pad.receiver.TimeChangeReceiver;

import androidx.annotation.Nullable;

public class TimeService extends Service {

	private static final String TAG = "TimeService";
	private TimeChangeReceiver mTimeChangeReceiver;

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate: ");
		registerTimeChangeReceiver();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unRegisterTimeChangeReceiver();
	}

	private void registerTimeChangeReceiver() {
		mTimeChangeReceiver = new TimeChangeReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_TIME_TICK);
		registerReceiver(mTimeChangeReceiver, intentFilter);
	}

	private void unRegisterTimeChangeReceiver() {
		if (null != mTimeChangeReceiver) {
			unregisterReceiver(mTimeChangeReceiver);
		}
	}
}
