package com.viegre.nas.pad.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.provider.Settings;

import com.blankj.utilcode.util.SPUtils;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.config.SPConfig;
import com.viegre.nas.pad.receiver.ScreenStatusReceiver;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

/**
 * Created by レインマン on 2021/03/09 18:12 with Android Studio.
 */
public class ScreenSaverService extends Service {

	private ScreenStatusReceiver mScreenStatusReceiver;

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		registerScreenStatusReceiver();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		createNotificationChannel();
		initSaver();
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		unregisterScreenStatusReceiver();
		super.onDestroy();
	}

	private void registerScreenStatusReceiver() {
		mScreenStatusReceiver = new ScreenStatusReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
		registerReceiver(mScreenStatusReceiver, intentFilter);
	}

	private void unregisterScreenStatusReceiver() {
		if (null != mScreenStatusReceiver) {
			unregisterReceiver(mScreenStatusReceiver);
		}
	}

	private void initSaver() {
		boolean screenSaverSwitch = SPUtils.getInstance().getBoolean(SPConfig.SCREEN_SAVER_SWITCH, true);
		if (screenSaverSwitch) {
			int delay = SPUtils.getInstance().getInt(SPConfig.SCREEN_SAVER_DELAY, 5);
			Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, delay * 60 * 1000);
		} else {
			Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, Integer.MAX_VALUE);
		}
	}

	@SuppressLint("NewApi")
	private void createNotificationChannel() {
		String CHANNEL_ID = "nas_channel_screen_saver";
		NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_NONE);
		((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(notificationChannel);
		Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID).setContentTitle("")
		                                                                            .setContentText("")
		                                                                            .setSmallIcon(R.drawable.ic_launcher_foreground)
		                                                                            .setChannelId(CHANNEL_ID)
		                                                                            .build();
		startForeground(0x03, notification);
	}
}
