package com.viegre.nas.pad.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import com.topqizhi.ai.manager.AIUIManager;
import com.topqizhi.ai.manager.MscManager;
import com.viegre.nas.pad.manager.SkillManager;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

/**
 * Created by レインマン on 2021/04/25 14:42 with Android Studio.
 */
public class MscService extends Service {

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		initNotificationChannel();
		AIUIManager.INSTANCE.addAIUIResultListener(SkillManager.INSTANCE::parseSkillMsg);
		AIUIManager.INSTANCE.startListening();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		MscManager.INSTANCE.stopListening();
		MscManager.INSTANCE.release();
		AIUIManager.INSTANCE.release();
	}

	private void initNotificationChannel() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			String CHANNEL_ID = "nas_channel_msc";
			NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_DEFAULT);
			((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
			Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID).setContentTitle("").setContentText("").build();
			startForeground(1, notification);
		}
	}
}
