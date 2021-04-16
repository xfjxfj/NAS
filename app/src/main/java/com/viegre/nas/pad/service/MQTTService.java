package com.viegre.nas.pad.service;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.constant.TimeConstants;
import com.blankj.utilcode.util.DeviceUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.PhoneUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.Utils;
import com.blankj.utilcode.util.ViewUtils;
import com.viegre.nas.pad.config.PathConfig;
import com.viegre.nas.pad.entity.MQTTMsg;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

/**
 * Created by レインマン on 2021/04/12 09:40 with Android Studio.
 */
public class MQTTService extends Service {

	private final String TAG = MQTTService.class.getSimpleName();

	private MqttConnectOptions mMqttConnectOptions;
	private MqttAndroidClient mMqttAndroidClient;

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		initNotificationChannel();
		initMqttAndroidClient();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private void initMqttConnectOptions() {
		mMqttConnectOptions = new MqttConnectOptions();
		mMqttConnectOptions.setAutomaticReconnect(true);
		mMqttConnectOptions.setCleanSession(true);//是否清空客户端的连接记录。若为true，则断开后，broker将自动清除该客户端连接信息
		mMqttConnectOptions.setConnectionTimeout(60);
		mMqttConnectOptions.setKeepAliveInterval(60);
		mMqttConnectOptions.setUserName("study_pen");
		mMqttConnectOptions.setPassword("lMz^zBrG2Gtu".toCharArray());
		mMqttConnectOptions.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);//选择MQTT版本
	}

	private void initMqttAndroidClient() {
		initMqttConnectOptions();
		try {
			MemoryPersistence memoryPersistence = new MemoryPersistence();
			mMqttAndroidClient = new MqttAndroidClient(Utils.getApp(), "tcp://39.108.236.191:1883", PhoneUtils.getSerial(), memoryPersistence);
			mMqttAndroidClient.setCallback(mMqttCallbackExtended);
			mMqttAndroidClient.connect(mMqttConnectOptions);
		} catch (MqttException e) {
			LogUtils.eTag(TAG, e.toString());
		}
	}

	private final MqttCallbackExtended mMqttCallbackExtended = new MqttCallbackExtended() {
		@Override
		public void connectComplete(boolean reconnect, String serverURI) {
			LogUtils.iTag(TAG, "connectComplete", Thread.currentThread().getId(), serverURI);
			try {
				mMqttAndroidClient.subscribe("nas/device/" + PhoneUtils.getSerial(), 2);
			} catch (MqttException e) {
				LogUtils.eTag(TAG, e.toString());
			}
		}

		@Override
		public void connectionLost(Throwable cause) {
			LogUtils.eTag(TAG, "connectionLost: " + cause.toString());
		}

		@Override
		public void messageArrived(String topic, MqttMessage message) {
			try {
				if (null != message) {
					String json = new String(message.getPayload());
					LogUtils.iTag(TAG, "messageArrived", " topic: " + topic, "message: " + json);
					parseMessage(json);
				} else {
					LogUtils.eTag(TAG, "messageArrived: 消息为空");
				}
			} catch (Exception e) {
				LogUtils.eTag(TAG, e.toString());
			}
		}

		@Override
		public void deliveryComplete(IMqttDeliveryToken token) {
			if (null != token) {
				try {
					LogUtils.iTag(TAG, "deliveryComplete", new String(token.getMessage().getPayload()));
				} catch (MqttException e) {
					LogUtils.eTag(TAG, "connectionLost", e.toString());
				}
			} else {
				LogUtils.eTag(TAG, "deliveryComplete: token为空");
			}
		}
	};

	private void initNotificationChannel() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			String CHANNEL_ID = "nas_channel_mqtt";
			NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_DEFAULT);
			((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
			Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID).setContentTitle("").setContentText("").build();
			startForeground(1, notification);
		}
	}

	/**
	 * 发送消息
	 *
	 * @param mqttMsg
	 */
	public void sendMQTTMsg(MQTTMsg mqttMsg) {
		MqttMessage mqttMessage = new MqttMessage();
		mqttMessage.setPayload(mqttMsg.getJsonObject().toJSONString().getBytes());//设置消息内容
		mqttMessage.setQos(2);//设置消息发送质量，可为0,1,2.
		mqttMessage.setRetained(false);//服务器是否保存最后一条消息，若保存，client再次上线时，将再次受到上次发送的最后一条消息。
		try {
			mMqttAndroidClient.publish("nas/phone/" + mqttMsg.getToId(), mqttMessage);//设置消息的topic，并发送。
		} catch (MqttException e) {
			LogUtils.eTag(TAG, "sendMqttMsg", e.toString());
		}
	}

	private void parseMessage(String message) {
		MQTTMsg mqttMsg = JSON.parseObject(message, MQTTMsg.class);
		switch (mqttMsg.getMsgType()) {
			case MQTTMsg.TYPE_NOTIFY:
				switch (mqttMsg.getMsg()) {
					//设备信息
					case MQTTMsg.MSG_DEVICE_INFO:
						sendMQTTMsg(getDeviceInfo(mqttMsg.getFromId()));
						break;

					//公共/私人存储空间
					case MQTTMsg.MSG_STORAGE_INFO:
						sendMQTTMsg(getStorageInfo(mqttMsg.getFromId()));
						break;

					default:
						break;
				}
				break;

			case MQTTMsg.TYPE_CMD:
				switch (mqttMsg.getMsg()) {
					//磁盘整理
					case MQTTMsg.MSG_DISK_DEFRAGMENT:
						ViewUtils.runOnUiThreadDelayed(() -> sendMQTTMsg(diskDefragment(mqttMsg.getFromId())), getRandomNum(10, 20) * 1000L);
						break;

					//还原
					case MQTTMsg.MSG_RESTORE:
						ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<MQTTMsg>() {
							@Override
							public MQTTMsg doInBackground() throws ZipException {
								String restoreFilePath = mqttMsg.getPath();
								MQTTMsg restoreMsg = getRestoreMsg(mqttMsg.getFromId());
								new ZipFile(restoreFilePath).extractAll(PathConfig.NAS);
								restoreMsg.getJsonObject().put("result", true);
								return restoreMsg;
							}

							@Override
							public void onFail(Throwable t) {
								super.onFail(t);
								MQTTMsg restoreMsg = getRestoreMsg(mqttMsg.getFromId());
								restoreMsg.getJsonObject().put("result", false);
								sendMQTTMsg(restoreMsg);
							}

							@Override
							public void onSuccess(MQTTMsg result) {
								sendMQTTMsg(result);
							}
						});
						break;

					//备份
					case MQTTMsg.MSG_BACKUP:
						ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<MQTTMsg>() {
							@Override
							public MQTTMsg doInBackground() throws ZipException {
								String backupDirPath = mqttMsg.getPath();
								String backupFileName = "备份" + TimeUtils.getNowString() + ".zip";
								MQTTMsg backupMsg = getBackupMsg(mqttMsg.getFromId());
								ZipFile zipFile = new ZipFile(backupDirPath + backupFileName);
								zipFile.addFolder(FileUtils.getFileByPath(PathConfig.PUBLIC));
								zipFile.addFolder(FileUtils.getFileByPath(PathConfig.PUBLIC));
								backupMsg.getJsonObject().put("path", backupDirPath + backupFileName);
								backupMsg.getJsonObject().put("result", true);
								return backupMsg;
							}

							@Override
							public void onFail(Throwable t) {
								super.onFail(t);
								MQTTMsg backupMsg = getBackupMsg(mqttMsg.getFromId());
								backupMsg.getJsonObject().put("result", false);
								sendMQTTMsg(backupMsg);
							}

							@Override
							public void onSuccess(MQTTMsg result) {
								sendMQTTMsg(result);
							}
						});
						break;

					//使用寿命检测
					case MQTTMsg.MSG_LIFE_TEST:
						ViewUtils.runOnUiThreadDelayed(() -> sendMQTTMsg(lifeTest(mqttMsg.getFromId())), getRandomNum(10, 20) * 1000L);
						break;

					//关机
					case MQTTMsg.MSG_SHUT_DOWN:
						sendMQTTMsg(shutDown(mqttMsg.getFromId()));
						break;

					//重启
					case MQTTMsg.MSG_REBOOT:
						sendMQTTMsg(reboot(mqttMsg.getFromId()));
						break;

					default:
						break;
				}
				break;

			default:
				break;
		}
	}

	/**
	 * 获取设备信息
	 *
	 * @param toId
	 * @return
	 */
	private MQTTMsg getDeviceInfo(String toId) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("msgType", MQTTMsg.TYPE_NOTIFY);
		jsonObject.put("msg", MQTTMsg.MSG_DEVICE_INFO);
		jsonObject.put("fromId", PhoneUtils.getSerial());
		jsonObject.put("toId", toId);
		jsonObject.put("timestamp", System.currentTimeMillis());
		jsonObject.put("name", "模拟器");
		jsonObject.put("mac", DeviceUtils.getMacAddress());
		jsonObject.put("ip", NetworkUtils.getIPAddress(true));
		jsonObject.put("cpu", "Cortex-A53 8核 1.5GHz");
		ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
		activityManager.getMemoryInfo(memoryInfo);
		jsonObject.put("ram", memoryInfo.totalMem);
		jsonObject.put("status", "正常");
		jsonObject.put("runningTime", SystemClock.elapsedRealtime());
		jsonObject.put("driveSn", "");
		jsonObject.put("driveStatus", "");
		jsonObject.put("driveModel", "");
		jsonObject.put("driveCapacity", "");
		return new MQTTMsg(toId, jsonObject);
	}

	/**
	 * 获取公共/私人存储空间信息
	 *
	 * @param toId
	 * @return
	 */
	private MQTTMsg getStorageInfo(String toId) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("msgType", MQTTMsg.TYPE_NOTIFY);
		jsonObject.put("msg", MQTTMsg.MSG_STORAGE_INFO);
		jsonObject.put("fromId", PhoneUtils.getSerial());
		jsonObject.put("toId", toId);
		jsonObject.put("timestamp", System.currentTimeMillis());
		jsonObject.put("total", FileUtils.getFsTotalSize(PathConfig.NAS));
		jsonObject.put("publicUsed", getDirLength(FileUtils.getFileByPath(PathConfig.PUBLIC)));
		List<File> privateDirList = FileUtils.listFilesInDir(PathConfig.PRIVATE);
		if (!privateDirList.isEmpty()) {
			Map<String, Object> privateDirMap = new HashMap<>();
			for (File file : privateDirList) {
				if (FileUtils.isDir(file)) {
					privateDirMap.put(file.getName(), getDirLength(file));
				}
			}
			if (!privateDirMap.isEmpty()) {
				JSONObject privateDirJson = new JSONObject(privateDirMap);
				jsonObject.put("privateUsed", privateDirJson.toJSONString());
			}
		}
		return new MQTTMsg(toId, jsonObject);
	}

	/**
	 * 磁盘整理
	 *
	 * @param toId
	 * @return
	 */
	private MQTTMsg diskDefragment(String toId) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("msgType", MQTTMsg.TYPE_CMD);
		jsonObject.put("msg", MQTTMsg.MSG_DISK_DEFRAGMENT);
		jsonObject.put("fromId", PhoneUtils.getSerial());
		jsonObject.put("toId", toId);
		jsonObject.put("timestamp", System.currentTimeMillis());
		jsonObject.put("result", true);
		return new MQTTMsg(toId, jsonObject);
	}

	/**
	 * 还原
	 *
	 * @param toId
	 * @return
	 */
	private MQTTMsg getRestoreMsg(String toId) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("msgType", MQTTMsg.TYPE_CMD);
		jsonObject.put("msg", MQTTMsg.MSG_RESTORE);
		jsonObject.put("fromId", PhoneUtils.getSerial());
		jsonObject.put("toId", toId);
		jsonObject.put("timestamp", System.currentTimeMillis());
		return new MQTTMsg(toId, jsonObject);
	}

	/**
	 * 备份
	 *
	 * @param toId
	 * @return
	 */
	private MQTTMsg getBackupMsg(String toId) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("msgType", MQTTMsg.TYPE_CMD);
		jsonObject.put("msg", MQTTMsg.MSG_BACKUP);
		jsonObject.put("fromId", PhoneUtils.getSerial());
		jsonObject.put("toId", toId);
		jsonObject.put("timestamp", System.currentTimeMillis());
		return new MQTTMsg(toId, jsonObject);
	}

	/**
	 * 使用寿命检测
	 *
	 * @param toId
	 * @return
	 */
	private MQTTMsg lifeTest(String toId) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("msgType", MQTTMsg.TYPE_CMD);
		jsonObject.put("msg", MQTTMsg.MSG_LIFE_TEST);
		jsonObject.put("fromId", PhoneUtils.getSerial());
		jsonObject.put("toId", toId);
		jsonObject.put("timestamp", System.currentTimeMillis());
		jsonObject.put("days",
		               TimeUtils.getTimeSpan(System.currentTimeMillis(),
		                                     TimeUtils.string2Millis("20210401", new SimpleDateFormat("yyyyMMdd", Locale.getDefault())),
		                                     TimeConstants.DAY));
		return new MQTTMsg(toId, jsonObject);
	}

	/**
	 * 关机
	 *
	 * @param toId
	 * @return
	 */
	private MQTTMsg shutDown(String toId) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("msgType", MQTTMsg.TYPE_CMD);
		jsonObject.put("msg", MQTTMsg.MSG_SHUT_DOWN);
		jsonObject.put("fromId", PhoneUtils.getSerial());
		jsonObject.put("toId", toId);
		jsonObject.put("timestamp", System.currentTimeMillis());
		jsonObject.put("result", true);
		return new MQTTMsg(toId, jsonObject);
	}

	/**
	 * 重启
	 *
	 * @param toId
	 * @return
	 */
	private MQTTMsg reboot(String toId) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("msgType", MQTTMsg.TYPE_CMD);
		jsonObject.put("msg", MQTTMsg.MSG_REBOOT);
		jsonObject.put("fromId", PhoneUtils.getSerial());
		jsonObject.put("toId", toId);
		jsonObject.put("timestamp", System.currentTimeMillis());
		jsonObject.put("result", true);
		return new MQTTMsg(toId, jsonObject);
	}

	/**
	 * 生成一个startNum 到 endNum之间的随机数(不包含endNum的随机数)
	 *
	 * @param startNum
	 * @param endNum
	 * @return
	 */
	private int getRandomNum(int startNum, int endNum) {
		if (endNum > startNum) {
			Random random = new Random();
			return random.nextInt(endNum - startNum) + startNum;
		}
		return 0;
	}

	private long getDirLength(final File dir) {
		long len = 0;
		File[] files = dir.listFiles();
		if (files != null && files.length > 0) {
			for (File file : files) {
				if (file.isDirectory()) {
					len += getDirLength(file);
				} else {
					len += file.length();
				}
			}
		}
		return len;
	}
}
