package com.viegre.nas.pad.service;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.MediaStore;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.constant.TimeConstants;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ConvertUtils;
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
import com.google.common.collect.Lists;
import com.viegre.nas.pad.activity.LoginActivity;
import com.viegre.nas.pad.activity.WelcomeActivity;
import com.viegre.nas.pad.config.BusConfig;
import com.viegre.nas.pad.config.PathConfig;
import com.viegre.nas.pad.config.SPConfig;
import com.viegre.nas.pad.config.UrlConfig;
import com.viegre.nas.pad.entity.ExternalDriveEntity;
import com.viegre.nas.pad.entity.FtpCategoryEntity;
import com.viegre.nas.pad.entity.FtpCmdEntity;
import com.viegre.nas.pad.entity.FtpFileEntity;
import com.viegre.nas.pad.entity.FtpFileQueryEntity;
import com.viegre.nas.pad.entity.FtpFileQueryPaginationEntity;
import com.viegre.nas.pad.entity.MQTTMsgEntity;
import com.viegre.nas.pad.task.VoidTask;
import com.viegre.nas.pad.util.MediaScanner;
import com.yanzhenjie.andserver.AndServer;
import com.yanzhenjie.andserver.Server;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import org.apache.ftpserver.ConnectionConfigFactory;
import org.apache.ftpserver.DataConnectionConfigurationFactory;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.greenrobot.eventbus.EventBus;
import org.litepal.LitePal;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import custom.fileobserver.FileListener;
import custom.fileobserver.FileWatcher;
import rxhttp.RxHttpPlugins;

/**
 * Created by レインマン on 2021/04/12 09:40 with Android Studio.
 */
public class MQTTService extends Service {

	private final String TAG = MQTTService.class.getSimpleName();

	private Server mServer;
	private MqttConnectOptions mMqttConnectOptions;
	private MqttAndroidClient mMqttAndroidClient;
	private FileWatcher mFileWatcher;
	private FtpServer mFtpServer;

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mServer = AndServer.webServer(this).port(8080).timeout(15, TimeUnit.SECONDS).build();
		initFileWatcher();
		initFtp();
		initNotificationChannel();
		initMqttAndroidClient();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		startFtpServer();
		startAndServer();
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		stopFtpServer();
		stopAndServer();
		if (null != mFileWatcher) {
			mFileWatcher.stopWatching();
		}
		super.onDestroy();
	}

	private void startAndServer() {
		if (null != mServer && !mServer.isRunning()) {
			mServer.startup();
		}
	}

	private void stopAndServer() {
		if (null != mServer && mServer.isRunning()) {
			mServer.shutdown();
		}
	}

	private void startFtpServer() {
		try {
			if (null != mFtpServer && mFtpServer.isStopped()) {
				mFtpServer.start();
			}
		} catch (FtpException e) {
			e.printStackTrace();
		}
	}

	private void stopFtpServer() {
		if (null != mFtpServer) {
			mFtpServer.stop();
			mFtpServer = null;
		}
	}

	private void initFtp() {
		try {
			FtpServerFactory ftpServerFactory = new FtpServerFactory();
			BaseUser baseUser = new BaseUser();
			baseUser.setName("resu");
			baseUser.setPassword("tOPq!zH1");
			baseUser.setHomeDirectory(PathConfig.NAS);

			List<Authority> authorities = new ArrayList<>();
			authorities.add(new WritePermission());
			baseUser.setAuthorities(authorities);
			ftpServerFactory.getUserManager().save(baseUser);

			ListenerFactory listenerFactory = new ListenerFactory();
			listenerFactory.setPort(2121);//设置端口号，非ROOT不可使用1024以下的端口
			DataConnectionConfigurationFactory dataConnectionConfigurationFactory = new DataConnectionConfigurationFactory();
			dataConnectionConfigurationFactory.setPassivePorts("20000-30000");
			listenerFactory.setIdleTimeout(0);
			dataConnectionConfigurationFactory.setIdleTime(0);
			listenerFactory.setDataConnectionConfiguration(dataConnectionConfigurationFactory.createDataConnectionConfiguration());

			ConnectionConfigFactory connectionConfigFactory = new ConnectionConfigFactory();
			connectionConfigFactory.setMaxLogins(10000);
			connectionConfigFactory.setAnonymousLoginEnabled(false);
			connectionConfigFactory.setMaxLoginFailures(5);
			connectionConfigFactory.setLoginFailureDelay(2000);
			ftpServerFactory.setConnectionConfig(connectionConfigFactory.createConnectionConfig());

			mFtpServer = ftpServerFactory.createServer();
			ftpServerFactory.addListener("default", listenerFactory.createListener());
		} catch (FtpException e) {
			e.printStackTrace();
		}
	}

	private void initFileWatcher() {
		mFileWatcher = new FileWatcher(PathConfig.NAS.substring(0, PathConfig.NAS.length() - 1), true, FileWatcher.ALL_EVENTS);
		mFileWatcher.setFileListener(new FileListener() {
			@Override
			public void onFileOpen(String path) {
				MediaScannerConnection.scanFile(Utils.getApp(), new String[]{path}, null, null);
			}

			@Override
			public void onFileCreated(String path) {
				MediaScannerConnection.scanFile(Utils.getApp(), new String[]{path}, null, null);
			}

			@Override
			public void onFileDeleted(String path) {
				MediaScannerConnection.scanFile(Utils.getApp(), new String[]{path}, null, null);
			}

			@Override
			public void onFileModified(String path) {
				MediaScannerConnection.scanFile(Utils.getApp(), new String[]{path}, null, null);
			}

			@Override
			public void onFileRenamed(String oldPath, String newPath) {
				MediaScannerConnection.scanFile(Utils.getApp(), new String[]{oldPath, newPath}, null, null);
			}
		});
		mFileWatcher.startWatching();
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
			                                           "nas/" + SPUtils.getInstance().getString(SPConfig.ANDROID_ID),
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
			LogUtils.iTag(TAG, "Mqtt连接成功, sn = " + SPUtils.getInstance().getString(SPConfig.ANDROID_ID) + ", serverUri = " + serverURI);
			try {
				mMqttAndroidClient.subscribe("nas/device/" + SPUtils.getInstance().getString(SPConfig.ANDROID_ID), 1);
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
					String json = new String(message.getPayload(), StandardCharsets.UTF_8);
					LogUtils.iTag(TAG, "收到消息, topic = " + topic);
					LogUtils.json(TAG, json);
					parseMessage(json);
				} else {
					LogUtils.eTag(TAG, "消息为空");
				}
			} catch (Exception e) {
				LogUtils.eTag(TAG, e.toString());
			}
		}

		@Override
		public void deliveryComplete(IMqttDeliveryToken token) {
			if (null != token) {
				try {
					LogUtils.iTag(TAG, "消息发送完毕");
					LogUtils.json(TAG, new String(token.getMessage().getPayload()));
				} catch (MqttException e) {
					LogUtils.eTag(TAG, "deliveryComplete: connectionLost", e.toString());
				}
			} else {
				LogUtils.eTag(TAG, "deliveryComplete: token为空");
			}
		}
	};

	private void initNotificationChannel() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			String CHANNEL_ID = "nas_channel_mqtt";
			@SuppressLint("WrongConstant")
			NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_NONE);
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
		mqttMessage.setQos(1);//设置消息发送质量，可为0,1,2.
		mqttMessage.setRetained(false);//服务器是否保存最后一条消息，若保存，client再次上线时，将再次受到上次发送的最后一条消息。
		try {
			mMqttAndroidClient.publish("nas/user/" + mqttMsgEntity.getToId(), mqttMessage);//设置消息的topic，并发送。
		} catch (MqttException e) {
			LogUtils.eTag(TAG, "sendMqttMsg", e.toString());
		}
	}

	@SuppressLint("NewApi")
	private void parseMessage(String message) {
		MQTTMsgEntity mqttMsgEntity = JSON.parseObject(message, MQTTMsgEntity.class);
		switch (mqttMsgEntity.getMsgType()) {
			case MQTTMsgEntity.TYPE_NOTIFY:
				switch (mqttMsgEntity.getAction()) {
					//登录设备
					case MQTTMsgEntity.MSG_SCAN_LOGIN:
						String token = JSON.parseObject(mqttMsgEntity.getParam()).getString("token");
						String phone = JSON.parseObject(mqttMsgEntity.getParam()).getString("phone");
						RxHttpPlugins.init(RxHttpPlugins.getOkHttpClient()).setOnParamAssembly(param -> param.addHeader("token", token));
						SPUtils.getInstance().put(SPConfig.PHONE, phone);
						SPUtils.getInstance().put("token", token);
//                        PopupManager.INSTANCE.showCustomXPopup(this, new LoginTimePopup(this));
						ActivityUtils.finishActivity(LoginActivity.class);
						break;

					//设备信息
					case MQTTMsgEntity.MSG_DEVICE_INFO:
						sendMQTTMsg(deviceInfo(mqttMsgEntity.getFromId()));
						break;

					//公共/私人存储空间
					case MQTTMsgEntity.MSG_STORAGE_INFO:
						sendMQTTMsg(storageInfo(mqttMsgEntity.getFromId()));
						break;

					//外部设备列表
					case MQTTMsgEntity.MSG_EXTERNAL_DRIVE_LIST:
						ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<List<ExternalDriveEntity>>() {
							@Override
							public List<ExternalDriveEntity> doInBackground() {
								List<ExternalDriveEntity> list = new ArrayList<>();
								list.add(new ExternalDriveEntity("USB Storage", "/storage/3C3E71843E71384A/"));
								return list;
							}

							@Override
							public void onSuccess(List<ExternalDriveEntity> result) {
								Map<String, Object> externalDriveListMap = new HashMap<>();
								externalDriveListMap.put("externalDriveList", result);
								MQTTMsgEntity externalDriveListMsg = externalDriveList(mqttMsgEntity.getFromId());
								externalDriveListMsg.setParam(new JSONObject(externalDriveListMap).toJSONString());
								sendMQTTMsg(externalDriveListMsg);
							}
						});
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
							EventBus.getDefault().postSticky(BusConfig.DEVICE_BOUND);
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
								String backupFileName = "备份" + TimeUtils.getNowString(new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())) + ".zip";
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

					//ftp复制/移动/重命名
					case MQTTMsgEntity.MSG_FTP_LOCATION:
						String type = JSON.parseObject(mqttMsgEntity.getParam()).getString("type");
						String destPath = JSON.parseObject(mqttMsgEntity.getParam()).getString("destPath");
						List<FtpCmdEntity> srcPathList = JSON.parseObject(mqttMsgEntity.getParam())
						                                     .getJSONArray("srcPathList")
						                                     .toJavaList(FtpCmdEntity.class);
						ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<Boolean>() {
							@Override
							public Boolean doInBackground() {
//								if (!FileUtils.isFileExists(destPath) || !FileUtils.isDir(destPath)) {
//									return false;
//								}
								srcPathList.forEach(ftpCmdEntity -> {
									if (FileUtils.isFileExists(ftpCmdEntity.getPath())) {
										switch (type) {
											case FtpFileEntity.CP:
												FileUtils.copy(ftpCmdEntity.getPath(), destPath + FileUtils.getFileName(ftpCmdEntity.getPath()));
												MediaScannerConnection.scanFile(Utils.getApp(),
												                                new String[]{destPath + FileUtils.getFileName(ftpCmdEntity.getPath())},
												                                null,
												                                null);
												break;

											case FtpFileEntity.MV:
												FileUtils.move(ftpCmdEntity.getPath(), destPath + FileUtils.getFileName(ftpCmdEntity.getPath()));
												MediaScannerConnection.scanFile(Utils.getApp(),
												                                new String[]{ftpCmdEntity.getPath(), destPath + FileUtils.getFileName(
														                                ftpCmdEntity.getPath())},
												                                null,
												                                null);
												FtpFileEntity ftpFileEntity = LitePal.where("path = ? AND state = ?",
												                                            ftpCmdEntity.getPath(),
												                                            FtpFileEntity.State.NORMAL)
												                                     .findFirst(FtpFileEntity.class);
												if (null != ftpFileEntity) {
													ftpFileEntity.setPath(destPath + FileUtils.getFileName(ftpCmdEntity.getPath()));
													ftpFileEntity.saveOrUpdate();
												}
												break;

											case FtpFileEntity.RE:
												FileUtils.rename(ftpCmdEntity.getPath(), FileUtils.getFileName(destPath));
												new MediaScanner(Utils.getApp(), null).scanFile(new File(ftpCmdEntity.getPath()));
												new MediaScanner(Utils.getApp(), null).scanFile(new File(destPath));
												FtpFileEntity ffe = LitePal.where("path = ? AND state = ?",
												                                  ftpCmdEntity.getPath(),
												                                  FtpFileEntity.State.NORMAL).findFirst(FtpFileEntity.class);
												if (null != ffe) {
													ffe.setPath(destPath);
													ffe.saveOrUpdate();
												}
												break;

											default:
												break;
										}
									}
								});
								return true;
							}

							@Override
							public void onSuccess(Boolean result) {
								Map<String, Object> ftpCopyParamMap = new HashMap<>();
								ftpCopyParamMap.put("result", result);
								MQTTMsgEntity ftpCopyMsg = ftpLocation(mqttMsgEntity.getFromId());
								ftpCopyMsg.setParam(new JSONObject(ftpCopyParamMap).toJSONString());
								sendMQTTMsg(ftpCopyMsg);
							}
						});
						break;

					//ftp删除
					case MQTTMsgEntity.MSG_FTP_DELETE:
						List<FtpCmdEntity> delPathList = JSON.parseObject(mqttMsgEntity.getParam()).getJSONArray("delPathList").toJavaList(FtpCmdEntity.class);
						ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<Boolean>() {
							@Override
							public Boolean doInBackground() {
								delPathList.forEach(ftpCmdEntity -> {
									if (FileUtils.isFileExists(ftpCmdEntity.getPath())) {
										FtpFileEntity ffe = LitePal.where("path = ? AND phoneNum = ?",
										                                  ftpCmdEntity.getPath(),
										                                  mqttMsgEntity.getFromId()).findFirst(FtpFileEntity.class);
										String recycledPath = PathConfig.RECYCLE_BIN + FileUtils.getFileName(ftpCmdEntity.getPath());
										SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
										String createTime = TimeUtils.millis2String(FileUtils.getFileLastModified(ftpCmdEntity.getPath()), sdf);
										String deleteTime = TimeUtils.getNowString(sdf);
										String type = getFtpFileType(ftpCmdEntity.getPath());
										if (null == ffe) {
											FtpFileEntity ftpFileEntity = new FtpFileEntity(ftpCmdEntity.getPath(),
											                                                recycledPath,
											                                                createTime,
											                                                deleteTime,
											                                                mqttMsgEntity.getFromId(),
											                                                type,
											                                                FtpFileEntity.State.RECYCLED);
											ftpFileEntity.save();
											FileUtils.move(ftpFileEntity.getPath(), ftpFileEntity.getRecycledPath());
											MediaScannerConnection.scanFile(Utils.getApp(),
											                                new String[]{ftpFileEntity.getPath(), ftpFileEntity.getRecycledPath()},
											                                null,
											                                null);
										} else {
											ffe.setRecycledPath(recycledPath);
											ffe.setCreateTime(TimeUtils.getNowString(sdf));
											ffe.setDeleteTime(deleteTime);
											ffe.setType(type);
											ffe.setState(FtpFileEntity.State.RECYCLED);
											ffe.saveOrUpdate();
											FileUtils.move(ffe.getPath(), ffe.getRecycledPath());
											MediaScannerConnection.scanFile(Utils.getApp(),
											                                new String[]{ffe.getPath(), ffe.getRecycledPath()},
											                                null,
											                                null);
										}
									}
								});
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
						ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<List<FtpFileEntity>>() {
							@Override
							public List<FtpFileEntity> doInBackground() {
								return LitePal.where("phoneNum = ? AND state = ?", mqttMsgEntity.getFromId(), FtpFileEntity.State.RECYCLED)
								              .find(FtpFileEntity.class);
							}

							@Override
							public void onSuccess(List<FtpFileEntity> result) {
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
								rstPathList.forEach(ftpCmdEntity -> {
									FtpFileEntity ftpFileEntity = LitePal.where("path = ? AND phoneNum = ? AND state = ?",
									                                            ftpCmdEntity.getPath(),
									                                            mqttMsgEntity.getFromId(),
									                                            FtpFileEntity.State.RECYCLED).findFirst(FtpFileEntity.class);
									if (null != ftpFileEntity) {
										FileUtils.move(ftpFileEntity.getRecycledPath(), ftpFileEntity.getPath());
										ftpFileEntity.setState(FtpFileEntity.State.NORMAL);
										ftpFileEntity.saveOrUpdate();
									}
								});
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
						List<FtpCmdEntity> erasePathList = JSON.parseObject(mqttMsgEntity.getParam()).getJSONArray("erasePathList").toJavaList(FtpCmdEntity.class);
						ThreadUtils.executeByCached(new VoidTask() {
							@Override
							public Void doInBackground() {
								if (erase) {
									List<FtpFileEntity> eraseList = LitePal.where("phoneNum = ? AND state = ?",
									                                              mqttMsgEntity.getFromId(),
									                                              FtpFileEntity.State.RECYCLED).find(FtpFileEntity.class);
									if (!eraseList.isEmpty()) {
										eraseList.forEach(ftpFileEntity -> {
											FileUtils.delete(ftpFileEntity.getRecycledPath());
											ftpFileEntity.delete();
										});
									}
								} else {
									if (null != erasePathList && !erasePathList.isEmpty()) {
										erasePathList.forEach(ftpCmdEntity -> {
											FileUtils.delete(ftpCmdEntity.getPath());
											LitePal.deleteAll(FtpFileEntity.class,
											                  "path = ? AND phoneNum = ? AND state = ?",
											                  ftpCmdEntity.getPath(),
											                  mqttMsgEntity.getFromId(),
											                  FtpFileEntity.State.RECYCLED);
										});
									}
								}

								Map<String, Object> ftpEraseParamMap = new HashMap<>();
								ftpEraseParamMap.put("result", true);
								MQTTMsgEntity ftpEraseMsg = ftpErase(mqttMsgEntity.getFromId());
								ftpEraseMsg.setParam(new JSONObject(ftpEraseParamMap).toJSONString());
								sendMQTTMsg(ftpEraseMsg);
								return null;
							}
						});
						break;

					//ftp文件收藏
					case MQTTMsgEntity.MSG_FTP_FAVORITES:
						boolean isAdd = JSON.parseObject(mqttMsgEntity.getParam()).getBoolean("isAdd");
						List<FtpFileEntity> ftpFileEntityList = JSON.parseObject(mqttMsgEntity.getParam())
						                                            .getJSONArray("favoritesPathList")
						                                            .toJavaList(FtpFileEntity.class);
						ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<Boolean>() {
							@Override
							public Boolean doInBackground() {
								if (null == ftpFileEntityList || ftpFileEntityList.isEmpty()) {
									return false;
								} else {
									SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
									ftpFileEntityList.forEach(ftpFileEntity -> {
										if (isAdd) {
											FtpFileEntity ffe = LitePal.where("path = ? AND pick = ? AND phoneNum = ?",
											                                  ftpFileEntity.getPath(),
											                                  FtpFileEntity.BanPick.FALSE,
											                                  mqttMsgEntity.getFromId()).findFirst(FtpFileEntity.class);
											if (null == ffe) {
												ftpFileEntity.setCreateTime(TimeUtils.millis2String(FileUtils.getFileLastModified(ftpFileEntity.getPath()),
												                                                    sdf));
												ftpFileEntity.setSize(FileUtils.getSize(ftpFileEntity.getPath()));
												ftpFileEntity.setState(FtpFileEntity.State.NORMAL);
												ftpFileEntity.setPick(FtpFileEntity.BanPick.TRUE);
												ftpFileEntity.setPhoneNum(mqttMsgEntity.getFromId());
												ftpFileEntity.save();
											} else {
												ffe.setPick(FtpFileEntity.BanPick.TRUE);
												ffe.saveOrUpdate();
											}
										} else {
											FtpFileEntity ffe = LitePal.where("path = ? AND pick = ? AND phoneNum = ?",
											                                  ftpFileEntity.getPath(),
											                                  FtpFileEntity.BanPick.TRUE,
											                                  mqttMsgEntity.getFromId()).findFirst(FtpFileEntity.class);
											if (null == ffe) {
												ftpFileEntity.setCreateTime(TimeUtils.millis2String(FileUtils.getFileLastModified(ftpFileEntity.getPath()),
												                                                    sdf));
												ftpFileEntity.setSize(FileUtils.getSize(ftpFileEntity.getPath()));
												ftpFileEntity.setState(FtpFileEntity.State.NORMAL);
												ftpFileEntity.setPick(FtpFileEntity.BanPick.FALSE);
												ftpFileEntity.setPhoneNum(mqttMsgEntity.getFromId());
												ftpFileEntity.save();
											} else {
												ffe.setPick(FtpFileEntity.BanPick.FALSE);
												ffe.saveOrUpdate();
											}
										}
									});
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
						ThreadUtils.executeByCached(new VoidTask() {
							@Override
							public Void doInBackground() {
								Map<String, Object> ftpFavoritesListMap = new HashMap<>();
								ftpFavoritesListMap.put("favoritesPathList",
								                        LitePal.where("phoneNum = ? AND pick = ? AND state = ?",
								                                      mqttMsgEntity.getFromId(),
								                                      FtpFileEntity.BanPick.TRUE,
								                                      FtpFileEntity.State.NORMAL).find(FtpFileEntity.class));
								MQTTMsgEntity ftpFavoritesListMsg = ftpFavoritesList(mqttMsgEntity.getFromId());
								ftpFavoritesListMsg.setParam(new JSONObject(ftpFavoritesListMap).toJSONString());
								sendMQTTMsg(ftpFavoritesListMsg);
								return null;
							}
						});
						break;

					//ftp文件查询
					case MQTTMsgEntity.MSG_FTP_QUERY_LIST:
						String queryPath = JSON.parseObject(mqttMsgEntity.getParam()).getString("path");
						String name = JSON.parseObject(mqttMsgEntity.getParam()).getString("name");
						int page = JSON.parseObject(mqttMsgEntity.getParam()).getInteger("page");
						int size = JSON.parseObject(mqttMsgEntity.getParam()).getInteger("size");
						ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<FtpFileQueryPaginationEntity>() {
							@Override
							public FtpFileQueryPaginationEntity doInBackground() {
								List<FtpFileQueryEntity> ftpFileList = new ArrayList<>();
								ContentResolver contentResolver = getContentResolver();
								Cursor cursor = contentResolver.query(MediaStore.Files.getContentUri("external"),
								                                      new String[]{MediaStore.Files.FileColumns.DATA},
								                                      MediaStore.Files.FileColumns.DATA + " LIKE ?",
								                                      new String[]{queryPath + "%" + name + "%"},
								                                      null);
								if (null != cursor) {
									while (cursor.moveToNext()) {
										String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA));
										if (!path.startsWith(PathConfig.PUBLIC) && !path.startsWith(PathConfig.PRIVATE + mqttMsgEntity.getFromId() + File.separator)) {
											continue;
										}
										FtpFileQueryEntity ftpFileQueryEntity = new FtpFileQueryEntity();
										ftpFileQueryEntity.setName(FileUtils.getFileName(path));
										ftpFileQueryEntity.setPath(path);
										ftpFileQueryEntity.setType(FileUtils.isDir(path) ? "dir" : "file");
										ftpFileQueryEntity.setCreateTime(TimeUtils.millis2String(FileUtils.getFileLastModified(path),
										                                                         new SimpleDateFormat("yyyy-MM-dd HH:mm",
										                                                                              Locale.getDefault())));
										ftpFileQueryEntity.setSrc(path.startsWith(PathConfig.PUBLIC) ? "public" : "private");
										ftpFileList.add(ftpFileQueryEntity);
									}
									cursor.close();
								}
								FtpFileQueryPaginationEntity ftpFileQueryPaginationEntity = new FtpFileQueryPaginationEntity();
								ftpFileQueryPaginationEntity.setPage(page);
								ftpFileQueryPaginationEntity.setSize(size);
								ftpFileQueryPaginationEntity.setTotal(ftpFileList.size());
								if (!ftpFileList.isEmpty()) {
									if (ftpFileList.size() <= size) {//若查询列表条数小于单页最大条数，则返回全部列表
										ftpFileQueryPaginationEntity.setPage(1);
										ftpFileQueryPaginationEntity.getQueryList().addAll(ftpFileList);
									} else {//开始分页
										List<List<FtpFileQueryEntity>> partitionList = Lists.partition(ftpFileList, size);
										if (page <= partitionList.size()) {
											ftpFileQueryPaginationEntity.getQueryList().addAll(partitionList.get(page - 1));
										} else {
											ftpFileQueryPaginationEntity.setPage(1);
											ftpFileQueryPaginationEntity.getQueryList().addAll(partitionList.get(0));
										}
									}
								}
								return ftpFileQueryPaginationEntity;
							}

							@Override
							public void onSuccess(FtpFileQueryPaginationEntity result) {
								MQTTMsgEntity ftpQueryListMsg = ftpQueryList(mqttMsgEntity.getFromId());
								ftpQueryListMsg.setParam(JSON.toJSONString(result));
								sendMQTTMsg(ftpQueryListMsg);
							}
						});
						break;

					//文件分类
					case MQTTMsgEntity.MSG_FTP_CATEGORY_LIST:
						boolean privateOnly = JSON.parseObject(mqttMsgEntity.getParam()).getBoolean("privateOnly");
						String category = JSON.parseObject(mqttMsgEntity.getParam()).getString("category");
						String phoneNum = mqttMsgEntity.getFromId();
						ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<List<FtpCategoryEntity>>() {
							@Override
							public List<FtpCategoryEntity> doInBackground() {
								return queryFtpCategory(privateOnly, category, phoneNum);
							}

							@Override
							public void onSuccess(List<FtpCategoryEntity> result) {
								Map<String, Object> ftpCategoryListMap = new HashMap<>();
								ftpCategoryListMap.put("category", category);
								ftpCategoryListMap.put("categoryList", result);
								MQTTMsgEntity ftpCategoryListMsg = ftpCategoryList(mqttMsgEntity.getFromId());
								ftpCategoryListMsg.setParam(new JSONObject(ftpCategoryListMap).toJSONString());
								sendMQTTMsg(ftpCategoryListMsg);
							}
						});
						break;

					//ftp文件状态刷新
					case MQTTMsgEntity.MSG_FTP_STATUS_REFRESH:
						List<FtpCmdEntity> refreshPathList = JSON.parseObject(mqttMsgEntity.getParam()).getJSONArray("refreshPathList").toJavaList(FtpCmdEntity.class);
						ThreadUtils.executeByCached(new VoidTask() {
							@Override
							public Void doInBackground() {
								if (!refreshPathList.isEmpty()) {
									List<String> pathList = new ArrayList<>();
									for (FtpCmdEntity ftpCmdEntity : refreshPathList) {
										pathList.add(ftpCmdEntity.getPath());
									}
									MediaScannerConnection.scanFile(Utils.getApp(), pathList.toArray(new String[0]), null, (s, uri) -> {
										Map<String, Object> ftpRefreshPathParamMap = new HashMap<>();
										ftpRefreshPathParamMap.put("result", true);
										MQTTMsgEntity ftpRefreshPathMsg = ftpStatusRefresh(mqttMsgEntity.getFromId());
										ftpRefreshPathMsg.setParam(new JSONObject(ftpRefreshPathParamMap).toJSONString());
										sendMQTTMsg(ftpRefreshPathMsg);
									});
								}
								return null;
							}
						});
						break;

					//ftp禁止访问列表
					case MQTTMsgEntity.MSG_FTP_BAN_LIST:
						boolean isBan = JSON.parseObject(mqttMsgEntity.getParam()).getBoolean("isBan");
						List<FtpFileEntity> banList = JSON.parseObject(mqttMsgEntity.getParam())
						                                  .getJSONArray("banList")
						                                  .toJavaList(FtpFileEntity.class);
						ThreadUtils.executeByCached(new VoidTask() {
							@Override
							public Void doInBackground() {
								if (null != banList && !banList.isEmpty()) {
									SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
									banList.forEach(ftpFileEntity -> {
										if (isBan) {
											FtpFileEntity ffe = LitePal.where("path = ? AND ban = ? AND phoneNum = ?",
											                                  ftpFileEntity.getPath(),
											                                  FtpFileEntity.BanPick.FALSE,
											                                  ftpFileEntity.getPhoneNum()).findFirst(FtpFileEntity.class);
											if (null == ffe) {
												ftpFileEntity.setCreateTime(TimeUtils.millis2String(FileUtils.getFileLastModified(ftpFileEntity.getPath()),
												                                                    sdf));
												ftpFileEntity.setSize(FileUtils.getSize(ftpFileEntity.getPath()));
												ftpFileEntity.setState(FtpFileEntity.State.NORMAL);
												ftpFileEntity.setBan(FtpFileEntity.BanPick.TRUE);
												ftpFileEntity.save();
											} else {
												ffe.setBan(FtpFileEntity.BanPick.TRUE);
												ffe.saveOrUpdate();
											}
										} else {
											FtpFileEntity ffe = LitePal.where("path = ? AND ban = ? AND phoneNum = ?",
											                                  ftpFileEntity.getPath(),
											                                  FtpFileEntity.BanPick.TRUE,
											                                  ftpFileEntity.getPhoneNum()).findFirst(FtpFileEntity.class);
											if (null == ffe) {
												ftpFileEntity.setCreateTime(TimeUtils.millis2String(FileUtils.getFileLastModified(ftpFileEntity.getPath()),
												                                                    sdf));
												ftpFileEntity.setSize(FileUtils.getSize(ftpFileEntity.getPath()));
												ftpFileEntity.setState(FtpFileEntity.State.NORMAL);
												ftpFileEntity.setBan(FtpFileEntity.BanPick.FALSE);
												ftpFileEntity.save();
											} else {
												ffe.setBan(FtpFileEntity.BanPick.FALSE);
												ffe.saveOrUpdate();
											}
										}
									});
								}

								Map<String, Object> ftpBanListParamMap = new HashMap<>();
								ftpBanListParamMap.put("result", true);
								MQTTMsgEntity ftpBanListMsg = ftpBanList(mqttMsgEntity.getFromId());
								ftpBanListMsg.setParam(new JSONObject(ftpBanListParamMap).toJSONString());
								sendMQTTMsg(ftpBanListMsg);
								return null;
							}
						});
						break;

					//ftp获取禁止访问列表
					case MQTTMsgEntity.MSG_FTP_GET_BAN_LIST:
						String banPhoneNum = JSON.parseObject(mqttMsgEntity.getParam()).getString("phoneNum");
						ThreadUtils.executeByCached(new VoidTask() {
							@Override
							public Void doInBackground() {
								Map<String, Object> ftpGetBanListMap = new HashMap<>();
								ftpGetBanListMap.put("banList",
								                     LitePal.where("phoneNum = ? AND ban = ? AND state = ?",
								                                   banPhoneNum,
								                                   FtpFileEntity.BanPick.TRUE,
								                                   FtpFileEntity.State.NORMAL).find(FtpFileEntity.class));
								MQTTMsgEntity ftpGetBanListMsg = ftpGetBanList(mqttMsgEntity.getFromId());
								ftpGetBanListMsg.setParam(new JSONObject(ftpGetBanListMap).toJSONString());
								sendMQTTMsg(ftpGetBanListMsg);
								return null;
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
	 * 设备信息
	 *
	 * @param toId 目标ClientId
	 * @return MQTT消息
	 */
	private MQTTMsgEntity deviceInfo(String toId) {
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("name", "国乾NAS 2020");
		paramMap.put("mac", DeviceUtils.getMacAddress());
		paramMap.put("ip", NetworkUtils.getIPAddress(true));
		paramMap.put("cpu", "Cortex-A53 8核 1.5GHz");
		ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
		activityManager.getMemoryInfo(memoryInfo);
		paramMap.put("ram", memoryInfo.totalMem);
		paramMap.put("status", "正常");
		paramMap.put("runningTime", SystemClock.elapsedRealtime());
		paramMap.put("driveSn", "000000000033");
		paramMap.put("driveStatus", "正常");
		paramMap.put("driveModel", "1816");
//		paramMap.put("driveCapacity", FileUtils.getFsTotalSize("/storage/3C3E71843E71384A/"));
		paramMap.put("driveCapacity", 0);
		JSONObject paramJson = new JSONObject(paramMap);
		return new MQTTMsgEntity(MQTTMsgEntity.TYPE_NOTIFY, MQTTMsgEntity.MSG_DEVICE_INFO, toId, paramJson.toJSONString());
	}

	/**
	 * 公共/私人存储空间
	 *
	 * @param toId 目标ClientId
	 * @return MQTT消息
	 */
	private MQTTMsgEntity storageInfo(String toId) {
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
		} else {
			paramMap.put("publicUsed", 0);
		}
		JSONObject paramJson = new JSONObject(paramMap);
		return new MQTTMsgEntity(MQTTMsgEntity.TYPE_NOTIFY, MQTTMsgEntity.MSG_STORAGE_INFO, toId, paramJson.toJSONString());
	}

	/**
	 * 备份
	 *
	 * @param toId 外部设备列表
	 * @return MQTT消息
	 */
	private MQTTMsgEntity externalDriveList(String toId) {
		return new MQTTMsgEntity(MQTTMsgEntity.TYPE_NOTIFY, MQTTMsgEntity.MSG_EXTERNAL_DRIVE_LIST, toId);
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
	 * ftp复制/移动
	 *
	 * @param toId 目标ClientId
	 * @return MQTT消息
	 */
	private MQTTMsgEntity ftpLocation(String toId) {
		return new MQTTMsgEntity(MQTTMsgEntity.TYPE_CMD, MQTTMsgEntity.MSG_FTP_LOCATION, toId);
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
	 * ftp文件查询
	 *
	 * @param toId 目标ClientId
	 * @return MQTT消息
	 */
	private MQTTMsgEntity ftpQueryList(String toId) {
		return new MQTTMsgEntity(MQTTMsgEntity.TYPE_CMD, MQTTMsgEntity.MSG_FTP_QUERY_LIST, toId);
	}

	/**
	 * ftp文件分类
	 *
	 * @param toId 目标ClientId
	 * @return MQTT消息
	 */
	private MQTTMsgEntity ftpCategoryList(String toId) {
		return new MQTTMsgEntity(MQTTMsgEntity.TYPE_CMD, MQTTMsgEntity.MSG_FTP_CATEGORY_LIST, toId);
	}

	/**
	 * ftp文件状态刷新
	 *
	 * @param toId 目标ClientId
	 * @return MQTT消息
	 */
	private MQTTMsgEntity ftpStatusRefresh(String toId) {
		return new MQTTMsgEntity(MQTTMsgEntity.TYPE_CMD, MQTTMsgEntity.MSG_FTP_STATUS_REFRESH, toId);
	}

	/**
	 * ftp禁止访问列表
	 *
	 * @param toId 目标ClientId
	 * @return MQTT消息
	 */
	private MQTTMsgEntity ftpBanList(String toId) {
		return new MQTTMsgEntity(MQTTMsgEntity.TYPE_CMD, MQTTMsgEntity.MSG_FTP_BAN_LIST, toId);
	}

	/**
	 * ftp获取禁止访问列表
	 *
	 * @param toId 目标ClientId
	 * @return MQTT消息
	 */
	private MQTTMsgEntity ftpGetBanList(String toId) {
		return new MQTTMsgEntity(MQTTMsgEntity.TYPE_CMD, MQTTMsgEntity.MSG_FTP_GET_BAN_LIST, toId);
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

	private List<FtpCategoryEntity> queryFtpCategory(boolean privateOnly, String category, String phoneNum) {
		Uri uri;
		String pathProjection, sizeProjection, selection;
		String[] selectionArgs;
		switch (category) {
			case "image":
				uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				pathProjection = MediaStore.Images.Media.DATA;
				sizeProjection = MediaStore.Images.Media.SIZE;
				break;

			case "video":
				uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				pathProjection = MediaStore.Video.Media.DATA;
				sizeProjection = MediaStore.Video.Media.SIZE;
				break;

			case "audio":
				uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				pathProjection = MediaStore.Audio.Media.DATA;
				sizeProjection = MediaStore.Audio.Media.SIZE;
				break;

			default:
				uri = MediaStore.Files.getContentUri("external");
				pathProjection = MediaStore.Files.FileColumns.DATA;
				sizeProjection = MediaStore.Files.FileColumns.SIZE;
				break;
		}
		if (privateOnly) {
			selection = MediaStore.Files.FileColumns.DATA + " LIKE ?";
			selectionArgs = new String[]{PathConfig.PRIVATE + phoneNum + File.separator + "%"};
		} else {
			selection = MediaStore.Files.FileColumns.DATA + " LIKE ? OR " + MediaStore.Files.FileColumns.DATA + " LIKE ?";
			selectionArgs = new String[]{PathConfig.PUBLIC + "%", PathConfig.PRIVATE + phoneNum + File.separator + "%"};
		}
		List<FtpCategoryEntity> list = new ArrayList<>();
		Cursor cursor = Utils.getApp().getContentResolver().query(uri, new String[]{pathProjection, sizeProjection}, selection, selectionArgs, null);
		while (cursor.moveToNext()) {
			String path = cursor.getString(cursor.getColumnIndexOrThrow(pathProjection));
			if (FileUtils.isDir(path)) {
				continue;
			}

			String src = path.startsWith(PathConfig.PUBLIC) ? "public" : "private";

			String name = FileUtils.getFileName(path);
			if ("document".equals(category) && !isDocument(FileUtils.getFileExtension(name))) {
				continue;
			}

			if ("other".equals(category)) {
				if (isImage(FileUtils.getFileExtension(name)) || isVideo(FileUtils.getFileExtension(name)) || isAudio(FileUtils.getFileExtension(name)) || isDocument(
						FileUtils.getFileExtension(name))) {
					continue;
				}
			}

			long time = FileUtils.getFileLastModified(path);
			String createTime = TimeUtils.millis2String(time, new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()));
			long size = cursor.getLong(cursor.getColumnIndexOrThrow(sizeProjection));
			list.add(new FtpCategoryEntity(name, path, createTime, ConvertUtils.byte2FitMemorySize(size, 2), src));
		}
		cursor.close();
		return list;
	}

	private boolean isImage(String name) {
		List<String> nameList = new ArrayList<>();
		nameList.add("jpg");
		nameList.add("png");
		nameList.add("gif");
		nameList.add("bmp");
		nameList.add("webp");
		return nameList.contains(name.toLowerCase());
	}

	private boolean isVideo(String name) {
		List<String> nameList = new ArrayList<>();
		nameList.add("3gp");
		nameList.add("mp4");
		nameList.add("ts");
		nameList.add("mkv");
		nameList.add("avi");
		nameList.add("rm");
		nameList.add("rmvb");
		nameList.add("mov");
		return nameList.contains(name.toLowerCase());
	}

	private boolean isAudio(String name) {
		List<String> nameList = new ArrayList<>();
		nameList.add("3gp");
		nameList.add("mp3");
		nameList.add("m4a");
		nameList.add("aac");
		nameList.add("flac");
		nameList.add("wav");
		nameList.add("ogg");
		return nameList.contains(name.toLowerCase());
	}

	private boolean isDocument(String name) {
		List<String> nameList = new ArrayList<>();
		nameList.add("docx");
		nameList.add("doc");
		nameList.add("xls");
		nameList.add("xlsx");
		nameList.add("ppt");
		nameList.add("pptx");
		nameList.add("pdf");
		nameList.add("txt");
		return nameList.contains(name.toLowerCase());
	}

	private String getFtpFileType(String path) {
		if (path.startsWith(PathConfig.PRIVATE)) {
			return FtpFileEntity.Type.PRIVATE;
		} else if (path.startsWith(PathConfig.PUBLIC)) {
			return FtpFileEntity.Type.PUBLIC;
		} else {
			return FtpFileEntity.Type.UNKNOWN;
		}
	}
}
