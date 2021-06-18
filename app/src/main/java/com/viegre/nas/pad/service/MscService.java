package com.viegre.nas.pad.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.topqizhi.ai.manager.AIUIManager;
import com.topqizhi.ai.manager.MscManager;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.config.BusConfig;
import com.viegre.nas.pad.manager.SkillManager;
import com.viegre.nas.pad.util.IotGateway;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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
		EventBus.getDefault().register(this);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		createNotificationChannel();
		AIUIManager.INSTANCE.addAIUIResultListener(SkillManager.INSTANCE::parseSkillMsg);
		AIUIManager.INSTANCE.startListening();
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		MscManager.INSTANCE.stopListening();
		MscManager.INSTANCE.release();
		AIUIManager.INSTANCE.release();
		EventBus.getDefault().unregister(this);
	}

	/**
	 * 智能家居控制个性化数据同步
	 */
	@Subscribe(threadMode = ThreadMode.BACKGROUND, sticky = true)
	public void iotControl(String event) {
		if (!"AIUI_CONNECTED_TO_SERVER".equals(event)) {
			return;
		}
		try {
			IotGateway.getAllModel();
			IotGateway.getAllDevice();
			IotGateway.getAllArea();
			IotGateway.uploadAreaEntity(AIUIManager.INSTANCE.getAIUIAgent());
			IotGateway.uploadDeviceEntity(AIUIManager.INSTANCE.getAIUIAgent());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
	public void mscControl(String event) {
		switch (event) {
			case BusConfig.START_MSC:
				AIUIManager.INSTANCE.startListening();
				break;

			case BusConfig.STOP_MSC:
				AIUIManager.INSTANCE.stopVoiceNlp();
				MscManager.INSTANCE.stopListening();
				break;

			default:
				break;
		}
	}

	@SuppressLint("NewApi")
	private void createNotificationChannel() {
		String CHANNEL_ID = "nas_channel_msc";
		NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_NONE);
		((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(notificationChannel);
		Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID).setContentTitle("")
		                                                                            .setContentText("")
		                                                                            .setSmallIcon(R.drawable.ic_launcher_foreground)
		                                                                            .setChannelId(CHANNEL_ID)
		                                                                            .build();
		startForeground(0x01, notification);
	}
}
