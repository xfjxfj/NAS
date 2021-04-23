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
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.BusUtils;
import com.blankj.utilcode.util.DeviceUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.Utils;
import com.blankj.utilcode.util.ViewUtils;
import com.viegre.nas.pad.activity.WelcomeActivity;
import com.viegre.nas.pad.config.BusConfig;
import com.viegre.nas.pad.config.PathConfig;
import com.viegre.nas.pad.config.SPConfig;
import com.viegre.nas.pad.config.UrlConfig;
import com.viegre.nas.pad.entity.FtpCmdEntity;
import com.viegre.nas.pad.entity.FtpFavoritesEntity;
import com.viegre.nas.pad.entity.MQTTMsgEntity;
import com.viegre.nas.pad.entity.RecycleBinEntity;
import com.viegre.nas.pad.observer.FtpFileObserver;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.litepal.LitePal;

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
	private FtpFileObserver mFtpFileObserver;

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mFtpFileObserver = new FtpFileObserver(PathConfig.NAS.substring(0, PathConfig.NAS.length() - 1), FtpFileObserver.MULTI_DIRS_TYPE);
		mFtpFileObserver.startDirWatch();
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
		mFtpFileObserver.stopDirWatch();
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
			mMqttAndroidClient = new MqttAndroidClient(Utils.getApp(),
			                                           UrlConfig.MQTT_SERVER,
			                                           SPUtils.getInstance().getString(SPConfig.ANDROID_ID),
			                                           memoryPersistence);
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
				mMqttAndroidClient.subscribe("nas/device/" + SPUtils.getInstance().getString(SPConfig.ANDROID_ID), 2);
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
	 * @param mqttMsgEntity
	 */
	public void sendMQTTMsg(MQTTMsgEntity mqttMsgEntity) {
		MqttMessage mqttMessage = new MqttMessage();
		mqttMessage.setPayload(JSON.toJSONString(mqttMsgEntity).getBytes());//设置消息内容
		mqttMessage.setQos(2);//设置消息发送质量，可为0,1,2.
		mqttMessage.setRetained(false);//服务器是否保存最后一条消息，若保存，client再次上线时，将再次受到上次发送的最后一条消息。
		try {
			mMqttAndroidClient.publish("nas/user/" + mqttMsgEntity.getToId(), mqttMessage);//设置消息的topic，并发送。
		} catch (MqttException e) {
			LogUtils.eTag(TAG, "sendMqttMsg", e.toString());
		}
	}

	private void parseMessage(String message) {
		MQTTMsgEntity mqttMsgEntity = JSON.parseObject(message, MQTTMsgEntity.class);
		switch (mqttMsgEntity.getMsgType()) {
			case MQTTMsgEntity.TYPE_NOTIFY:
				switch (mqttMsgEntity.getAction()) {
					//设备信息
					case MQTTMsgEntity.MSG_DEVICE_INFO:
						sendMQTTMsg(getDeviceInfo(mqttMsgEntity.getFromId()));
						break;

					//公共/私人存储空间
					case MQTTMsgEntity.MSG_STORAGE_INFO:
						sendMQTTMsg(getStorageInfo(mqttMsgEntity.getFromId()));
						break;

					//设备绑定
					case MQTTMsgEntity.MSG_BIND_RESULT:
						String userName = JSON.parseObject(mqttMsgEntity.getParam()).getString("userName");
						String sn = JSON.parseObject(mqttMsgEntity.getParam()).getString("sn");
						//0 申请中、1 普通角色、2 拒绝、3 管理员
						int state = JSON.parseObject(mqttMsgEntity.getParam()).getInteger("state");
						LogUtils.iTag(TAG, "绑定结果: ", userName, sn, state);
						if (2 == state) {
							ToastUtils.showShort("管理员拒绝绑定");
						} else if (1 == state || 3 == state) {
//							SPUtils.getInstance().put(SPConfig.IS_BOUND, true);
							BusUtils.post(BusConfig.DEVICE_BOUND_RESULT);
							ActivityUtils.finishActivity(WelcomeActivity.class);
						}
						break;

					default:
						break;
				}
				break;

			case MQTTMsgEntity.TYPE_CMD:
				switch (mqttMsgEntity.getAction()) {
					//磁盘整理
					case MQTTMsgEntity.MSG_DISK_DEFRAGMENT:
						ViewUtils.runOnUiThreadDelayed(() -> sendMQTTMsg(diskDefragment(mqttMsgEntity.getFromId())), getRandomNum(10, 20) * 1000L);
						break;

					//还原
					case MQTTMsgEntity.MSG_RESTORE:
						Map<String, Object> restoreParamMap = new HashMap<>();
						ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<MQTTMsgEntity>() {
							@Override
							public MQTTMsgEntity doInBackground() throws ZipException {
								String restoreFilePath = JSON.parseObject(mqttMsgEntity.getParam()).getString("path");
								MQTTMsgEntity restoreMsg = getRestoreMsg(mqttMsgEntity.getFromId());
								new ZipFile(restoreFilePath).extractAll(PathConfig.NAS);
								restoreParamMap.put("result", true);
								restoreMsg.setParam(new JSONObject(restoreParamMap).toJSONString());
								return restoreMsg;
							}

							@Override
							public void onFail(Throwable t) {
								super.onFail(t);
								MQTTMsgEntity restoreMsg = getRestoreMsg(mqttMsgEntity.getFromId());
								restoreParamMap.put("result", false);
								restoreMsg.setParam(new JSONObject(restoreParamMap).toJSONString());
								sendMQTTMsg(restoreMsg);
							}

							@Override
							public void onSuccess(MQTTMsgEntity result) {
								sendMQTTMsg(result);
							}
						});
						break;

					//备份
					case MQTTMsgEntity.MSG_BACKUP:
						Map<String, Object> backupParamMap = new HashMap<>();
						ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<MQTTMsgEntity>() {
							@Override
							public MQTTMsgEntity doInBackground() throws ZipException {
								String backupDirPath = JSON.parseObject(mqttMsgEntity.getParam()).getString("path");
								String backupFileName = "备份" + TimeUtils.getNowString(new SimpleDateFormat("yyyyMMddHHmmss",
								                                                                           Locale.getDefault())) + ".zip";
								MQTTMsgEntity backupMsg = getBackupMsg(mqttMsgEntity.getFromId());
								ZipFile zipFile = new ZipFile(backupDirPath + backupFileName);
								zipFile.addFolder(FileUtils.getFileByPath(PathConfig.PRIVATE));
								zipFile.addFolder(FileUtils.getFileByPath(PathConfig.PUBLIC));
								backupParamMap.put("path", backupDirPath + backupFileName);
								backupParamMap.put("result", true);
								backupMsg.setParam(new JSONObject(backupParamMap).toJSONString());
								return backupMsg;
							}

							@Override
							public void onFail(Throwable t) {
								super.onFail(t);
								MQTTMsgEntity backupMsg = getBackupMsg(mqttMsgEntity.getFromId());
								backupParamMap.put("result", false);
								backupMsg.setParam(new JSONObject(backupParamMap).toJSONString());
								sendMQTTMsg(backupMsg);
							}

							@Override
							public void onSuccess(MQTTMsgEntity result) {
								sendMQTTMsg(result);
							}
						});
						break;

					//使用寿命检测
					case MQTTMsgEntity.MSG_LIFE_TEST:
						ViewUtils.runOnUiThreadDelayed(() -> sendMQTTMsg(lifeTest(mqttMsgEntity.getFromId())), getRandomNum(10, 20) * 1000L);
						break;

					//关机
					case MQTTMsgEntity.MSG_SHUT_DOWN:
						sendMQTTMsg(shutDown(mqttMsgEntity.getFromId()));
						break;

					//重启
					case MQTTMsgEntity.MSG_REBOOT:
						sendMQTTMsg(reboot(mqttMsgEntity.getFromId()));
						break;

					//ftp复制
					case MQTTMsgEntity.MSG_FTP_COPY:
						String destPath = JSON.parseObject(mqttMsgEntity.getParam()).getString("destPath");
						List<FtpCmdEntity> srcPathList = JSON.parseObject(mqttMsgEntity.getParam())
						                                     .getJSONArray("srcPathList")
						                                     .toJavaList(FtpCmdEntity.class);
						ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<Boolean>() {
							@Override
							public Boolean doInBackground() {
								if (!FileUtils.isFileExists(destPath) || !FileUtils.isDir(destPath)) {
									return false;
								}
								for (FtpCmdEntity ftpCmdEntity : srcPathList) {
									if (!FileUtils.isFileExists(ftpCmdEntity.getPath())) {
										continue;
									}
									FileUtils.copy(ftpCmdEntity.getPath(), destPath + FileUtils.getFileName(ftpCmdEntity.getPath()));
								}
								return true;
							}

							@Override
							public void onSuccess(Boolean result) {
								Map<String, Object> ftpCopyParamMap = new HashMap<>();
								ftpCopyParamMap.put("result", result);
								MQTTMsgEntity ftpCopyMsg = ftpCopy(mqttMsgEntity.getFromId());
								ftpCopyMsg.setParam(new JSONObject(ftpCopyParamMap).toJSONString());
								sendMQTTMsg(ftpCopyMsg);
							}
						});
						break;

					//ftp删除
					case MQTTMsgEntity.MSG_FTP_DELETE:
						List<FtpCmdEntity> delPathList = JSON.parseObject(mqttMsgEntity.getParam())
						                                     .getJSONArray("delPathList")
						                                     .toJavaList(FtpCmdEntity.class);
						ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<Boolean>() {
							@Override
							public Boolean doInBackground() {
								for (FtpCmdEntity ftpCmdEntity : delPathList) {
									File deleteFile = FileUtils.getFileByPath(ftpCmdEntity.getPath());
									if (!FileUtils.isFileExists(deleteFile)) {
										continue;
									}
									SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
									String deleteTime = TimeUtils.getNowString(sdf);
									String createTime = TimeUtils.millis2String(FileUtils.getFileLastModified(deleteFile), sdf);
									String type;
									if (ftpCmdEntity.getPath().startsWith(PathConfig.PRIVATE)) {
										type = "private";
									} else if (ftpCmdEntity.getPath().startsWith(PathConfig.PUBLIC)) {
										type = "public";
									} else {
										type = "unknown";
									}
									RecycleBinEntity recycleBinEntity = new RecycleBinEntity(deleteTime,
									                                                         createTime,
									                                                         type,
									                                                         ftpCmdEntity.getPath(),
									                                                         PathConfig.RECYCLE_BIN + FileUtils.getFileName(
											                                                         ftpCmdEntity.getPath()));
									FileUtils.move(recycleBinEntity.getPathBeforeDelete(), recycleBinEntity.getPathAfterDelete());
									recycleBinEntity.save();
								}
								return true;
							}

							@Override
							public void onSuccess(Boolean result) {
								Map<String, Object> ftpDeleteParamMap = new HashMap<>();
								ftpDeleteParamMap.put("result", result);
								MQTTMsgEntity ftpDeleteMsg = ftpDelete(mqttMsgEntity.getFromId());
								ftpDeleteMsg.setParam(new JSONObject(ftpDeleteParamMap).toJSONString());
								sendMQTTMsg(ftpDeleteMsg);
							}
						});
						break;

					//ftp删除列表
					case MQTTMsgEntity.MSG_FTP_DELETE_LIST:
						ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<List<RecycleBinEntity>>() {
							@Override
							public List<RecycleBinEntity> doInBackground() {
								return LitePal.findAll(RecycleBinEntity.class);
							}

							@Override
							public void onSuccess(List<RecycleBinEntity> result) {
								Map<String, Object> ftpDeleteListMap = new HashMap<>();
								ftpDeleteListMap.put("delPathList", result);
								MQTTMsgEntity ftpDeleteListMsg = ftpDeleteList(mqttMsgEntity.getFromId());
								ftpDeleteListMsg.setParam(new JSONObject(ftpDeleteListMap).toJSONString());
								sendMQTTMsg(ftpDeleteListMsg);
							}
						});
						break;

					//ftp还原列表
					case MQTTMsgEntity.MSG_FTP_RESTORE_LIST:
						List<FtpCmdEntity> rstPathList = JSON.parseObject(mqttMsgEntity.getParam())
						                                     .getJSONArray("rstPathList")
						                                     .toJavaList(FtpCmdEntity.class);
						ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<Boolean>() {
							@Override
							public Boolean doInBackground() {
								if (rstPathList.isEmpty()) {
									return false;
								}
								for (FtpCmdEntity ftpCmdEntity : rstPathList) {
									RecycleBinEntity recycleBinEntity = LitePal.where("pathBeforeDelete = ?", ftpCmdEntity.getPath())
									                                           .findFirst(RecycleBinEntity.class);
									if (null == recycleBinEntity) {
										continue;
									}
									FileUtils.move(recycleBinEntity.getPathAfterDelete(), recycleBinEntity.getPathBeforeDelete());
									recycleBinEntity.delete();
								}
								return true;
							}

							@Override
							public void onSuccess(Boolean result) {
								Map<String, Object> ftpRestoreParamMap = new HashMap<>();
								ftpRestoreParamMap.put("result", result);
								MQTTMsgEntity ftpDeleteMsg = ftpRestoreList(mqttMsgEntity.getFromId());
								ftpDeleteMsg.setParam(new JSONObject(ftpRestoreParamMap).toJSONString());
								sendMQTTMsg(ftpDeleteMsg);
							}
						});
						break;

					//ftp文件清除
					case MQTTMsgEntity.MSG_FTP_ERASE:
						boolean erase = JSON.parseObject(mqttMsgEntity.getParam()).getBoolean("erase");
						List<FtpCmdEntity> erasePathList = JSON.parseObject(mqttMsgEntity.getParam())
						                                       .getJSONArray("erasePathList")
						                                       .toJavaList(FtpCmdEntity.class);
						ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<Boolean>() {
							@Override
							public Boolean doInBackground() {
								if (erase) {
									FileUtils.deleteAllInDir(PathConfig.RECYCLE_BIN);
									LitePal.deleteAll(RecycleBinEntity.class);
									return true;
								}
								if (null == erasePathList || erasePathList.isEmpty()) {
									return false;
								} else {
									for (FtpCmdEntity ftpCmdEntity : erasePathList) {
										FileUtils.delete(ftpCmdEntity.getPath());
										RecycleBinEntity recycleBinEntity = LitePal.where("pathBeforeDelete = ?", ftpCmdEntity.getPath())
										                                           .findFirst(RecycleBinEntity.class);
										if (null != recycleBinEntity) {
											recycleBinEntity.delete();
										}
									}
								}
								return true;
							}

							@Override
							public void onSuccess(Boolean result) {
								Map<String, Object> ftpEraseParamMap = new HashMap<>();
								ftpEraseParamMap.put("result", result);
								MQTTMsgEntity ftpEraseMsg = ftpErase(mqttMsgEntity.getFromId());
								ftpEraseMsg.setParam(new JSONObject(ftpEraseParamMap).toJSONString());
								sendMQTTMsg(ftpEraseMsg);
							}
						});
						break;

					//ftp文件收藏
					case MQTTMsgEntity.MSG_FTP_FAVORITES:
						boolean isAdd = JSON.parseObject(mqttMsgEntity.getParam()).getBoolean("isAdd");
						List<FtpFavoritesEntity> favoritesPathList = JSON.parseObject(mqttMsgEntity.getParam())
						                                                 .getJSONArray("favoritesPathList")
						                                                 .toJavaList(FtpFavoritesEntity.class);
						ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<Boolean>() {
							@Override
							public Boolean doInBackground() {
								if (null == favoritesPathList || favoritesPathList.isEmpty()) {
									return false;
								} else {
									SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
									for (FtpFavoritesEntity ftpFavoritesEntity : favoritesPathList) {
										if (isAdd) {
											ftpFavoritesEntity.setTime(TimeUtils.millis2String(FileUtils.getFileLastModified(ftpFavoritesEntity.getPath()),
											                                                   sdf));
											ftpFavoritesEntity.setSize(FileUtils.getSize(ftpFavoritesEntity.getPath()));
											ftpFavoritesEntity.save();
										} else {
											FtpFavoritesEntity ftpFavorites = LitePal.where("path = ?", ftpFavoritesEntity.getPath())
											                                         .findFirst(FtpFavoritesEntity.class);
											if (null != ftpFavorites) {
												ftpFavorites.delete();
											}
										}
									}
								}
								return true;
							}

							@Override
							public void onSuccess(Boolean result) {
								Map<String, Object> ftpFavoritesParamMap = new HashMap<>();
								ftpFavoritesParamMap.put("result", result);
								MQTTMsgEntity ftpFavoritesMsg = ftpFavorites(mqttMsgEntity.getFromId());
								ftpFavoritesMsg.setParam(new JSONObject(ftpFavoritesParamMap).toJSONString());
								sendMQTTMsg(ftpFavoritesMsg);
							}
						});
						break;

					//ftp收藏列表
					case MQTTMsgEntity.MSG_FTP_FAVORITES_LIST:
						ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<List<FtpFavoritesEntity>>() {
							@Override
							public List<FtpFavoritesEntity> doInBackground() {
								return LitePal.findAll(FtpFavoritesEntity.class);
							}

							@Override
							public void onSuccess(List<FtpFavoritesEntity> result) {
								Map<String, Object> ftpFavoritesListMap = new HashMap<>();
								ftpFavoritesListMap.put("favoritesPathList", result);
								MQTTMsgEntity ftpFavoritesListMsg = ftpFavoritesList(mqttMsgEntity.getFromId());
								ftpFavoritesListMsg.setParam(new JSONObject(ftpFavoritesListMap).toJSONString());
								sendMQTTMsg(ftpFavoritesListMsg);
							}
						});
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
	 * @param toId 目标ClientId
	 * @return MQTT消息
	 */
	private MQTTMsgEntity getDeviceInfo(String toId) {
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("name", "模拟器");
		paramMap.put("mac", DeviceUtils.getMacAddress());
		paramMap.put("ip", NetworkUtils.getIPAddress(true));
		paramMap.put("cpu", "Cortex-A53 8核 1.5GHz");
		ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
		activityManager.getMemoryInfo(memoryInfo);
		paramMap.put("ram", memoryInfo.totalMem);
		paramMap.put("status", "正常");
		paramMap.put("runningTime", SystemClock.elapsedRealtime());
		paramMap.put("driveSn", "");
		paramMap.put("driveStatus", "");
		paramMap.put("driveModel", "");
		paramMap.put("driveCapacity", "");
		JSONObject paramJson = new JSONObject(paramMap);
		return new MQTTMsgEntity(MQTTMsgEntity.TYPE_NOTIFY, MQTTMsgEntity.MSG_DEVICE_INFO, toId, paramJson.toJSONString());
	}

	/**
	 * 获取公共/私人存储空间信息
	 *
	 * @param toId 目标ClientId
	 * @return MQTT消息
	 */
	private MQTTMsgEntity getStorageInfo(String toId) {
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("total", FileUtils.getFsTotalSize(PathConfig.NAS));
		paramMap.put("publicUsed", getDirLength(FileUtils.getFileByPath(PathConfig.PUBLIC)));
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
				paramMap.put("privateUsed", privateDirJson.toJSONString());
			}
		}
		JSONObject paramJson = new JSONObject(paramMap);
		return new MQTTMsgEntity(MQTTMsgEntity.TYPE_NOTIFY, MQTTMsgEntity.MSG_STORAGE_INFO, toId, paramJson.toJSONString());
	}

	/**
	 * 磁盘整理
	 *
	 * @param toId 目标ClientId
	 * @return MQTT消息
	 */
	private MQTTMsgEntity diskDefragment(String toId) {
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("result", true);
		JSONObject paramJson = new JSONObject(paramMap);
		return new MQTTMsgEntity(MQTTMsgEntity.TYPE_CMD, MQTTMsgEntity.MSG_DISK_DEFRAGMENT, toId, paramJson.toJSONString());
	}

	/**
	 * 还原
	 *
	 * @param toId 目标ClientId
	 * @return MQTT消息
	 */
	private MQTTMsgEntity getRestoreMsg(String toId) {
		return new MQTTMsgEntity(MQTTMsgEntity.TYPE_CMD, MQTTMsgEntity.MSG_RESTORE, toId);
	}

	/**
	 * 备份
	 *
	 * @param toId 目标ClientId
	 * @return MQTT消息
	 */
	private MQTTMsgEntity getBackupMsg(String toId) {
		return new MQTTMsgEntity(MQTTMsgEntity.TYPE_CMD, MQTTMsgEntity.MSG_BACKUP, toId);
	}

	/**
	 * 使用寿命检测
	 *
	 * @param toId 目标ClientId
	 * @return MQTT消息
	 */
	private MQTTMsgEntity lifeTest(String toId) {
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("days",
		             TimeUtils.getTimeSpan(System.currentTimeMillis(),
		                                   TimeUtils.string2Millis("20210401", new SimpleDateFormat("yyyyMMdd", Locale.getDefault())),
		                                   TimeConstants.DAY));
		JSONObject paramJson = new JSONObject(paramMap);
		return new MQTTMsgEntity(MQTTMsgEntity.TYPE_CMD, MQTTMsgEntity.MSG_LIFE_TEST, toId, paramJson.toJSONString());
	}

	/**
	 * 关机
	 *
	 * @param toId 目标ClientId
	 * @return MQTT消息
	 */
	private MQTTMsgEntity shutDown(String toId) {
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("result", true);
		JSONObject paramJson = new JSONObject(paramMap);
		return new MQTTMsgEntity(MQTTMsgEntity.TYPE_CMD, MQTTMsgEntity.MSG_SHUT_DOWN, toId, paramJson.toJSONString());
	}

	/**
	 * 重启
	 *
	 * @param toId 目标ClientId
	 * @return MQTT消息
	 */
	private MQTTMsgEntity reboot(String toId) {
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("result", true);
		JSONObject paramJson = new JSONObject(paramMap);
		return new MQTTMsgEntity(MQTTMsgEntity.TYPE_CMD, MQTTMsgEntity.MSG_REBOOT, toId, paramJson.toJSONString());
	}

	/**
	 * ftp复制
	 *
	 * @param toId 目标ClientId
	 * @return MQTT消息
	 */
	private MQTTMsgEntity ftpCopy(String toId) {
		return new MQTTMsgEntity(MQTTMsgEntity.TYPE_CMD, MQTTMsgEntity.MSG_FTP_COPY, toId);
	}

	/**
	 * ftp删除
	 *
	 * @param toId 目标ClientId
	 * @return MQTT消息
	 */
	private MQTTMsgEntity ftpDelete(String toId) {
		return new MQTTMsgEntity(MQTTMsgEntity.TYPE_CMD, MQTTMsgEntity.MSG_FTP_DELETE, toId);
	}

	/**
	 * ftp删除列表
	 *
	 * @param toId 目标ClientId
	 * @return MQTT消息
	 */
	private MQTTMsgEntity ftpDeleteList(String toId) {
		return new MQTTMsgEntity(MQTTMsgEntity.TYPE_CMD, MQTTMsgEntity.MSG_FTP_DELETE_LIST, toId);
	}

	/**
	 * ftp还原列表
	 *
	 * @param toId 目标ClientId
	 * @return MQTT消息
	 */
	private MQTTMsgEntity ftpRestoreList(String toId) {
		return new MQTTMsgEntity(MQTTMsgEntity.TYPE_CMD, MQTTMsgEntity.MSG_FTP_RESTORE_LIST, toId);
	}

	/**
	 * ftp文件清除
	 *
	 * @param toId 目标ClientId
	 * @return MQTT消息
	 */
	private MQTTMsgEntity ftpErase(String toId) {
		return new MQTTMsgEntity(MQTTMsgEntity.TYPE_CMD, MQTTMsgEntity.MSG_FTP_ERASE, toId);
	}

	/**
	 * ftp文件收藏
	 *
	 * @param toId 目标ClientId
	 * @return MQTT消息
	 */
	private MQTTMsgEntity ftpFavorites(String toId) {
		return new MQTTMsgEntity(MQTTMsgEntity.TYPE_CMD, MQTTMsgEntity.MSG_FTP_FAVORITES, toId);
	}

	/**
	 * ftp收藏列表
	 *
	 * @param toId 目标ClientId
	 * @return MQTT消息
	 */
	private MQTTMsgEntity ftpFavoritesList(String toId) {
		return new MQTTMsgEntity(MQTTMsgEntity.TYPE_CMD, MQTTMsgEntity.MSG_FTP_FAVORITES_LIST, toId);
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
