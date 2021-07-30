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
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.constant.TimeConstants;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.DeviceUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.Utils;
import com.google.common.collect.Lists;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.activity.BlueToothBindStatusActivity;
import com.viegre.nas.pad.config.BusConfig;
import com.viegre.nas.pad.config.PathConfig;
import com.viegre.nas.pad.config.SPConfig;
import com.viegre.nas.pad.config.UrlConfig;
import com.viegre.nas.pad.entity.ExternalDriveEntity;
import com.viegre.nas.pad.entity.FtpCategoryEntity;
import com.viegre.nas.pad.entity.FtpCmdEntity;
import com.viegre.nas.pad.entity.FtpFileBackupEntity;
import com.viegre.nas.pad.entity.FtpFileEntity;
import com.viegre.nas.pad.entity.FtpFileQueryEntity;
import com.viegre.nas.pad.entity.FtpFileQueryPaginationEntity;
import com.viegre.nas.pad.entity.MQTTMsgEntity;
import com.viegre.nas.pad.interceptor.TokenInterceptor;
import com.viegre.nas.pad.task.VoidTask;
import com.viegre.nas.pad.util.CommonUtils;
import com.viegre.nas.pad.util.MediaScanner;
import com.yanzhenjie.andserver.AndServer;
import com.yanzhenjie.andserver.Server;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

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
import java.util.stream.Collectors;

import androidx.core.app.NotificationCompat;
import custom.fileobserver.FileListener;
import custom.fileobserver.FileWatcher;

/**
 * Created by レインマン on 2021/04/12 09:40 with Android Studio.
 */
public class MQTTService extends Service {

    private final String TAG = MQTTService.class.getSimpleName();

    private MqttConnectOptions mMqttConnectOptions;
    private MqttAndroidClient mMqttAndroidClient;
    private FileWatcher mFileWatcher;
    private TipsDevicesFriend tipsdevicesfriend;
    private welcomebind welcomeBindStr;
    private userUpBind upBindStr;
    private Server mServer;

    @Override
    public void onCreate() {
        super.onCreate();
        initAndServer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        registerNetworkStatusChangedListener();
//		initFileWatcher();
        startAndServer();
        initMqttAndroidClient();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        stopAndServer();
        if (null != mFileWatcher) {
            mFileWatcher.stopWatching();
        }
        if (null != mMqttAndroidClient) {
            try {
                if (mMqttAndroidClient.isConnected()) {
                    mMqttAndroidClient.disconnect();
                }
            } catch (MqttException e) {
                e.printStackTrace();
            }
            mMqttAndroidClient.close();
        }
        if (null != mOnNetworkStatusChangedListener) {
            NetworkUtils.unregisterNetworkStatusChangedListener(mOnNetworkStatusChangedListener);
        }
        super.onDestroy();
    }

    private void initAndServer() {
        mServer =
                AndServer.webServer(this).port(8080).timeout(15, TimeUnit.SECONDS).listener(new Server.ServerListener() {
                    @Override
                    public void onStarted() {
                        LogUtils.iTag(TAG, "AndServer已启动", NetworkUtils.getIPAddress(true));
                    }

                    @Override
                    public void onStopped() {
                        LogUtils.iTag(TAG, "AndServer已停止");
                    }

                    @Override
                    public void onException(Exception e) {
                        e.printStackTrace();
                        LogUtils.eTag(TAG, "AndServer异常");
                    }
                }).build();
    }

    private void startAndServer() {
        mServer.startup();
    }

    private void stopAndServer() {
        mServer.shutdown();
    }

    private void initFileWatcher() {
        mFileWatcher = new FileWatcher(PathConfig.NAS.substring(0, PathConfig.NAS.length() - 1), true,
                FileWatcher.ALL_EVENTS);
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
            LogUtils.iTag(TAG, "Mqtt连接成功, sn = " + SPUtils.getInstance().getString(SPConfig.ANDROID_ID) + ", " +
                    "serverUri = " + serverURI);
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

    @SuppressLint("NewApi")
    private void createNotificationChannel() {
        String CHANNEL_ID = "nas_channel_mqtt";
        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID,
                NotificationManager.IMPORTANCE_NONE);
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(notificationChannel);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID).setContentTitle("")
                .setContentText("")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setChannelId(CHANNEL_ID)
                .build();
        startForeground(0x02, notification);
    }

    /**
     * 生成MQTT消息
     *
     * @param msgType
     * @param action
     * @param toId
     * @return MQTT消息
     */
    private MQTTMsgEntity getMQTTMsg(String msgType, String action, String toId) {
        return new MQTTMsgEntity(msgType, action, toId);
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
//		{"action":"addFriendResult","fromId":"appService","itemType":0,"msgType":"notify",
//		"param":{"handleTime":"2021-07-05T15:17:06.509","requestedSn":"5830e3fbe8c57dd5","status":1},
//		"timeStamp":1625469426,"toId":"6fa8295f4764b429"}
        MQTTMsgEntity mqttMsgEntity = JSON.parseObject(message, MQTTMsgEntity.class);
        switch (mqttMsgEntity.getMsgType()) {
            case MQTTMsgEntity.TYPE_REQUEST:
                switch (mqttMsgEntity.getAction()) {
                    case MQTTMsgEntity.MSG_ADD_FRIEND_REQUEST:
                        String requesterID = JSON.parseObject(mqttMsgEntity.getParam()).getString("requester");
                        Log.d("MSG_ADD_FRIEND_REQUEST：", message);
                        if (null != tipsdevicesfriend) {
                            tipsdevicesfriend.onTipsdevicesFriend(requesterID);
                        }
                        break;

                    default:
                        break;
                }

                break;
            case MQTTMsgEntity.TYPE_NOTIFY:
                switch (mqttMsgEntity.getAction()) {
                    case MQTTMsgEntity.MSG_ADDFRIENDRESULT:
                        String status = JSON.parseObject(mqttMsgEntity.getParam()).getString("status");
                        Log.d("MSG_ADDFRIENDRESULT：", message);
                        if (null != tipsdevicesfriend) {
                            tipsdevicesfriend.onTipsdevicesFriendStatus(status);
                        }
                        break;
                    //登录设备
                    case MQTTMsgEntity.MSG_SCAN_LOGIN://扫码登录过来
                        String token = JSON.parseObject(mqttMsgEntity.getParam()).getString(SPConfig.TOKEN);
                        String phone = JSON.parseObject(mqttMsgEntity.getParam()).getString(SPConfig.PHONE);
                        String avatar = JSON.parseObject(mqttMsgEntity.getParam()).getString(SPConfig.AVATAR);
                        String hour = JSON.parseObject(mqttMsgEntity.getParam()).getString(SPConfig.HOUR);
                        TokenInterceptor.saveTokenInfo(phone, token);
                        JSONObject js = new JSONObject();
                        js.put("phone", SPUtils.getInstance().getString(SPConfig.PHONE));
                        js.put(SPConfig.TOKEN_START_TIME, System.currentTimeMillis());
                        js.put(SPConfig.USERICON, avatar);
                        js.put(SPConfig.TOKEN_HOUR_TIME, hour);
                        SPUtils.getInstance().put(SPConfig.TOKEN_TIME, js.toString());

//                        getImgUrl(avatar,token);
//                        ActivityUtils.finishActivity(LoginActivity.class);
                        break;

                    //设备信息
                    case MQTTMsgEntity.MSG_DEVICE_INFO:
                        ThreadUtils.executeByCached(new VoidTask() {
                            @Override
                            public Void doInBackground() {
                                Map<String, Object> deviceInfoMap = new HashMap<>();
                                deviceInfoMap.put("name", "国乾NAS 2020");
                                deviceInfoMap.put("mac", DeviceUtils.getMacAddress());
                                deviceInfoMap.put("ip", NetworkUtils.getIPAddress(true));
                                deviceInfoMap.put("cpu", "Cortex-A53 8核 1.5GHz");
                                ActivityManager activityManager =
                                        (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                                ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
                                activityManager.getMemoryInfo(memoryInfo);
                                deviceInfoMap.put("ram", memoryInfo.totalMem);
                                deviceInfoMap.put("status", "正常");
                                deviceInfoMap.put("runningTime", SystemClock.elapsedRealtime());
                                deviceInfoMap.put("driveSn", "000000000033");
                                deviceInfoMap.put("driveStatus", "正常");
                                deviceInfoMap.put("driveModel", "1816");
                                deviceInfoMap.put("driveCapacity", FileUtils.getFsTotalSize(PathConfig.NAS));
                                MQTTMsgEntity deviceInfoMsg = getMQTTMsg(MQTTMsgEntity.TYPE_NOTIFY,
                                        MQTTMsgEntity.MSG_DEVICE_INFO,
                                        mqttMsgEntity.getFromId());
                                deviceInfoMsg.setParam(new JSONObject(deviceInfoMap).toJSONString());
                                sendMQTTMsg(deviceInfoMsg);
                                return null;
                            }
                        });
                        break;

                    //公共/私人存储空间
                    case MQTTMsgEntity.MSG_STORAGE_INFO:
                        ThreadUtils.executeByCached(new VoidTask() {
                            @Override
                            public Void doInBackground() {
                                Map<String, Object> storageInfoMap = new HashMap<>();
                                storageInfoMap.put("total", FileUtils.getFsTotalSize(PathConfig.NAS));
                                storageInfoMap.put("publicUsed",
                                        getDirLength(FileUtils.getFileByPath(PathConfig.PUBLIC)));
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
                                        storageInfoMap.put("privateUsed", privateDirJson.toJSONString());
                                    }
                                } else {
                                    storageInfoMap.put("publicUsed", 0);
                                }
                                MQTTMsgEntity storageInfoMsg = getMQTTMsg(MQTTMsgEntity.TYPE_NOTIFY,
                                        MQTTMsgEntity.MSG_STORAGE_INFO,
                                        mqttMsgEntity.getFromId());
                                storageInfoMsg.setParam(new JSONObject(storageInfoMap).toJSONString());
                                sendMQTTMsg(storageInfoMsg);
                                return null;
                            }
                        });
                        break;

                    //外部设备列表
                    case MQTTMsgEntity.MSG_EXTERNAL_DRIVE_LIST:
                        ThreadUtils.executeByCached(new VoidTask() {
                            @Override
                            public Void doInBackground() {
                                List<ExternalDriveEntity> list = new ArrayList<>();
                                list.add(new ExternalDriveEntity("USB Storage", PathConfig.NAS));
                                Map<String, Object> externalDriveListMap = new HashMap<>();
                                externalDriveListMap.put("externalDriveList", list);
                                MQTTMsgEntity externalDriveListMsg = getMQTTMsg(MQTTMsgEntity.TYPE_NOTIFY,
                                        MQTTMsgEntity.MSG_EXTERNAL_DRIVE_LIST,
                                        mqttMsgEntity.getFromId());
                                externalDriveListMsg.setParam(new JSONObject(externalDriveListMap).toJSONString());
                                sendMQTTMsg(externalDriveListMsg);
                                return null;
                            }
                        });
                        break;
                    //设备解绑
                    case MQTTMsgEntity.MSG_UNBUNDING:
                        String userName1 = JSON.parseObject(mqttMsgEntity.getParam()).getString("userName");
                        if (upBindStr != null) {
                            upBindStr.onUpBind(userName1 + "已经解绑成功");
                        }
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
                            ActivityUtils.finishActivity(BlueToothBindStatusActivity.class);
                        } else if (1 == state || 3 == state) {
                            if (welcomeBindStr != null) {
                                if (SPUtils.getInstance().getBoolean("bleBound", false)) {
                                    EventBus.getDefault().postSticky(BusConfig.DEVICE_BOUND_SUCCESS);
                                } else {
                                    CommonUtils.showToast("绑定成功，请稍等");
                                    welcomeBindStr.onWelcomeBind("绑定成功");
                                }
                            }
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
                        ThreadUtils.executeByCachedWithDelay(new VoidTask() {
                            @Override
                            public Void doInBackground() {
                                Map<String, Object> diskDefragmentMap = new HashMap<>();
                                diskDefragmentMap.put("result", true);
                                MQTTMsgEntity diskDefragmentMsg = getMQTTMsg(MQTTMsgEntity.TYPE_CMD,
                                        MQTTMsgEntity.MSG_DISK_DEFRAGMENT,
                                        mqttMsgEntity.getFromId());
                                diskDefragmentMsg.setParam(new JSONObject(diskDefragmentMap).toJSONString());
                                sendMQTTMsg(diskDefragmentMsg);
                                return null;
                            }
                        }, getRandomNum(10, 20), TimeUnit.SECONDS);
                        break;

                    //还原列表
                    case MQTTMsgEntity.MSG_RESTORE_LIST:
                        ThreadUtils.executeByCached(new VoidTask() {
                            @Override
                            public Void doInBackground() {
                                Map<String, Object> restoreListMap = new HashMap<>();
                                restoreListMap.put("restoreList",
                                        LitePal.where("phoneNum = ?", mqttMsgEntity.getFromId()).find(FtpFileBackupEntity.class));
                                MQTTMsgEntity restoreListMsg = getMQTTMsg(MQTTMsgEntity.TYPE_CMD,
                                        MQTTMsgEntity.MSG_RESTORE_LIST,
                                        mqttMsgEntity.getFromId());
                                restoreListMsg.setParam(new JSONObject(restoreListMap).toJSONString());
                                sendMQTTMsg(restoreListMsg);
                                return null;
                            }
                        });
                        break;

                    //还原
                    case MQTTMsgEntity.MSG_RESTORE:
                        ThreadUtils.executeByCached(new VoidTask() {
                            @Override
                            public Void doInBackground() throws ZipException {
                                String restoreFilePath = JSON.parseObject(mqttMsgEntity.getParam()).getString("path");
                                new ZipFile(restoreFilePath).extractAll(PathConfig.NAS);
                                Map<String, Object> restoreParamMap = new HashMap<>();
                                restoreParamMap.put("result", true);
                                MQTTMsgEntity restoreMsg = getMQTTMsg(MQTTMsgEntity.TYPE_CMD,
                                        MQTTMsgEntity.MSG_RESTORE, mqttMsgEntity.getFromId());
                                restoreMsg.setParam(new JSONObject(restoreParamMap).toJSONString());
                                sendMQTTMsg(restoreMsg);
                                return null;
                            }

                            @Override
                            public void onFail(Throwable t) {
                                super.onFail(t);
                                Map<String, Object> restoreParamMap = new HashMap<>();
                                restoreParamMap.put("result", false);
                                MQTTMsgEntity restoreMsg = getMQTTMsg(MQTTMsgEntity.TYPE_CMD,
                                        MQTTMsgEntity.MSG_RESTORE, mqttMsgEntity.getFromId());
                                restoreMsg.setParam(new JSONObject(restoreParamMap).toJSONString());
                                sendMQTTMsg(restoreMsg);
                            }
                        });
                        break;

                    //备份
                    case MQTTMsgEntity.MSG_BACKUP:
                        ThreadUtils.executeByCached(new VoidTask() {
                            @Override
                            public Void doInBackground() throws Throwable {
                                String backupDirPath = JSON.parseObject(mqttMsgEntity.getParam()).getString("path");
                                String backupFileName = "备份" + TimeUtils.getNowString(new SimpleDateFormat(
                                        "yyyyMMddHHmmss",
                                        Locale.getDefault())) + ".zip";
                                String backupPath = backupDirPath + backupFileName;
                                MQTTMsgEntity backupMsg = getMQTTMsg(MQTTMsgEntity.TYPE_CMD, MQTTMsgEntity.MSG_BACKUP
                                        , mqttMsgEntity.getFromId());
                                ZipFile zipFile = new ZipFile(backupPath);
                                zipFile.addFolder(FileUtils.getFileByPath(PathConfig.PRIVATE + mqttMsgEntity.getFromId() + File.separator));
                                zipFile.addFolder(FileUtils.getFileByPath(PathConfig.PUBLIC));
                                new FtpFileBackupEntity(backupPath, mqttMsgEntity.getFromId()).save();
                                Map<String, Object> backupParamMap = new HashMap<>();
                                backupParamMap.put("path", backupPath);
                                backupParamMap.put("result", true);
                                backupMsg.setParam(new JSONObject(backupParamMap).toJSONString());
                                sendMQTTMsg(backupMsg);
                                return null;
                            }

                            @Override
                            public void onFail(Throwable t) {
                                super.onFail(t);
                                Map<String, Object> backupParamMap = new HashMap<>();
                                backupParamMap.put("result", false);
                                MQTTMsgEntity backupMsg = getMQTTMsg(MQTTMsgEntity.TYPE_CMD, MQTTMsgEntity.MSG_BACKUP
                                        , mqttMsgEntity.getFromId());
                                backupMsg.setParam(new JSONObject(backupParamMap).toJSONString());
                                sendMQTTMsg(backupMsg);
                            }
                        });
                        break;

                    //使用寿命检测
                    case MQTTMsgEntity.MSG_LIFE_TEST:
                        ThreadUtils.executeByCachedWithDelay(new VoidTask() {
                            @Override
                            public Void doInBackground() {
                                Map<String, Object> lifeTestMap = new HashMap<>();
                                lifeTestMap.put("days",
                                        TimeUtils.getTimeSpan(System.currentTimeMillis(),
                                                TimeUtils.string2Millis("20210401",
                                                        new SimpleDateFormat("yyyyMMdd", Locale.getDefault())),
                                                TimeConstants.DAY));
                                MQTTMsgEntity lifeTestMsg = getMQTTMsg(MQTTMsgEntity.TYPE_CMD,
                                        MQTTMsgEntity.MSG_LIFE_TEST,
                                        mqttMsgEntity.getFromId());
                                lifeTestMsg.setParam(new JSONObject(lifeTestMap).toJSONString());
                                sendMQTTMsg(lifeTestMsg);
                                return null;
                            }
                        }, getRandomNum(10, 20), TimeUnit.SECONDS);
                        break;

                    //关机
                    case MQTTMsgEntity.MSG_SHUT_DOWN:
                        ThreadUtils.executeByCached(new VoidTask() {
                            @Override
                            public Void doInBackground() {
                                Map<String, Object> shutDownMap = new HashMap<>();
                                shutDownMap.put("result", true);
                                MQTTMsgEntity shutDownMsg = getMQTTMsg(MQTTMsgEntity.TYPE_CMD,
                                        MQTTMsgEntity.MSG_SHUT_DOWN,
                                        mqttMsgEntity.getFromId());
                                shutDownMsg.setParam(new JSONObject(shutDownMap).toJSONString());
                                sendMQTTMsg(shutDownMsg);
                                return null;
                            }
                        });
                        break;

                    //重启
                    case MQTTMsgEntity.MSG_REBOOT:
                        ThreadUtils.executeByCached(new VoidTask() {
                            @Override
                            public Void doInBackground() {
                                Map<String, Object> rebootMap = new HashMap<>();
                                rebootMap.put("result", true);
                                MQTTMsgEntity rebootMsg = getMQTTMsg(MQTTMsgEntity.TYPE_CMD, MQTTMsgEntity.MSG_REBOOT
                                        , mqttMsgEntity.getFromId());
                                rebootMsg.setParam(new JSONObject(rebootMap).toJSONString());
                                sendMQTTMsg(rebootMsg);
                                return null;
                            }
                        });
                        break;

                    //ftp复制/移动/重命名
                    case MQTTMsgEntity.MSG_FTP_LOCATION:
                        ThreadUtils.executeByCached(new VoidTask() {
                            @Override
                            public Void doInBackground() {
                                String type = JSON.parseObject(mqttMsgEntity.getParam()).getString("type");
                                String destPath = JSON.parseObject(mqttMsgEntity.getParam()).getString("destPath");
                                List<FtpFileEntity> srcPathList = JSON.parseObject(mqttMsgEntity.getParam())
                                        .getJSONArray("srcPathList")
                                        .toJavaList(FtpFileEntity.class);
                                List<FtpFileEntity> doneList = new ArrayList<>();
                                boolean result;
                                FtpFileEntity ftpFile = srcPathList.stream().filter(ftpFileEntity -> {
                                    if (FileUtils.isFileExists(ftpFileEntity.getPath())) {
                                        String destFilePath = destPath + FileUtils.getFileName(ftpFileEntity.getPath());
                                        switch (type) {
                                            case FtpFileEntity.CP:
                                                if (FileUtils.isFileExists(destFilePath)) {
                                                    if (ftpFileEntity.isCoverageConfirm()) {
                                                        coverageFile(ftpFileEntity.getPath(), destPath, type, 1);
                                                    } else {
                                                        ftpFileEntity.setCoverageConfirm(true);
                                                        return true;
                                                    }
                                                } else {
                                                    FileUtils.copy(ftpFileEntity.getPath(), destFilePath);
                                                    new MediaScanner().scanFile(destFilePath);
                                                }
                                                break;

                                            case FtpFileEntity.MV:
                                                if (FileUtils.isFileExists(destFilePath)) {
                                                    if (ftpFileEntity.isCoverageConfirm()) {
                                                        coverageFile(ftpFileEntity.getPath(), destPath, type, 1);
                                                    } else {
                                                        ftpFileEntity.setCoverageConfirm(true);
                                                        return true;
                                                    }
                                                } else {
                                                    FileUtils.move(ftpFileEntity.getPath(), destFilePath);
                                                    new MediaScanner().scanFile(ftpFileEntity.getPath());
                                                    new MediaScanner().scanFile(destFilePath);
                                                    FtpFileEntity mvFtpFileEntity = LitePal.where("path = ? and state" +
                                                                    " = ?",
                                                            ftpFileEntity.getPath(),
                                                            FtpFileEntity.State.NORMAL)
                                                            .findFirst(FtpFileEntity.class);
                                                    if (null != mvFtpFileEntity) {
                                                        mvFtpFileEntity.setPath(destFilePath);
                                                        mvFtpFileEntity.save();
                                                    }
                                                }
                                                break;

                                            case FtpFileEntity.RE:
                                                if (FileUtils.isFileExists(destPath)) {
                                                    if (ftpFileEntity.isCoverageConfirm()) {
                                                        coverageFile(ftpFileEntity.getPath(), destPath, type, 1);
                                                    } else {
                                                        ftpFileEntity.setCoverageConfirm(true);
                                                        return true;
                                                    }
                                                } else {
                                                    FileUtils.rename(ftpFileEntity.getPath(),
                                                            FileUtils.getFileName(destPath));
                                                    new MediaScanner().scanFile(ftpFileEntity.getPath());
                                                    new MediaScanner().scanFile(destPath);
                                                    FtpFileEntity reFtpFileEntity = LitePal.where("path = ? and state" +
                                                                    " = ?",
                                                            ftpFileEntity.getPath(),
                                                            FtpFileEntity.State.NORMAL)
                                                            .findFirst(FtpFileEntity.class);
                                                    if (null != reFtpFileEntity) {
                                                        reFtpFileEntity.setPath(destPath);
                                                        reFtpFileEntity.save();
                                                    }
                                                }
                                                break;

                                            default:
                                                break;
                                        }
                                    }
                                    doneList.add(ftpFileEntity);
                                    return false;
                                }).findFirst().orElse(null);

                                Map<String, Object> ftpCopyParamMap = new HashMap<>();
                                if (null == ftpFile) {
                                    result = true;
                                } else {
                                    result = false;
                                    ftpCopyParamMap.put("confirmList",
                                            srcPathList.stream()
                                                    .filter(ftpFileEntity -> !doneList.contains(ftpFileEntity))
                                                    .collect(Collectors.toList()));
                                    ftpCopyParamMap.put("destPath", destPath);
                                    ftpCopyParamMap.put("type", type);
                                }
                                ftpCopyParamMap.put("result", result);
                                MQTTMsgEntity ftpCopyMsg = getMQTTMsg(MQTTMsgEntity.TYPE_CMD,
                                        MQTTMsgEntity.MSG_FTP_LOCATION,
                                        mqttMsgEntity.getFromId());
                                ftpCopyMsg.setParam(new JSONObject(ftpCopyParamMap).toJSONString());
                                sendMQTTMsg(ftpCopyMsg);
                                return null;
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
                                delPathList.forEach(ftpCmdEntity -> {
                                    if (FileUtils.isFileExists(ftpCmdEntity.getPath())) {
                                        FtpFileEntity ftpFile =
                                                LitePal.where("path = ?", ftpCmdEntity.getPath()).findFirst(FtpFileEntity.class);
                                        String recycledPath =
                                                PathConfig.RECYCLE_BIN + FileUtils.getFileName(ftpCmdEntity.getPath());
                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm",
                                                Locale.getDefault());
                                        String deleteTime = TimeUtils.getNowString(sdf);
                                        String type = getFtpFileType(ftpCmdEntity.getPath());
                                        if (null == ftpFile) {
                                            FtpFileEntity ftpFileEntity = new FtpFileEntity(ftpCmdEntity.getPath(),
                                                    recycledPath,
                                                    TimeUtils.millis2String(FileUtils.getFileLastModified(
                                                            ftpCmdEntity.getPath()), sdf),
                                                    deleteTime,
                                                    mqttMsgEntity.getFromId(),
                                                    type,
                                                    FtpFileEntity.State.RECYCLED);
                                            ftpFileEntity.save();
                                            FileUtils.move(ftpFileEntity.getPath(), ftpFileEntity.getRecycledPath());
                                            MediaScannerConnection.scanFile(Utils.getApp(),
                                                    new String[]{ftpFileEntity.getPath(),
                                                            ftpFileEntity.getRecycledPath()},
                                                    null,
                                                    null);
                                        } else {
                                            ftpFile.setRecycledPath(recycledPath);
                                            ftpFile.setDeleteTime(deleteTime);
                                            ftpFile.setDeletePhoneNum(mqttMsgEntity.getFromId());
                                            ftpFile.setType(type);
                                            ftpFile.setState(FtpFileEntity.State.RECYCLED);
                                            ftpFile.save();
                                            FileUtils.move(ftpFile.getPath(), ftpFile.getRecycledPath());
                                            MediaScannerConnection.scanFile(Utils.getApp(),
                                                    new String[]{ftpFile.getPath(), ftpFile.getRecycledPath()},
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
                                MQTTMsgEntity ftpDeleteMsg = getMQTTMsg(MQTTMsgEntity.TYPE_CMD,
                                        MQTTMsgEntity.MSG_FTP_DELETE,
                                        mqttMsgEntity.getFromId());
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
                                return LitePal.where("deletePhoneNum = ? and state = ?", mqttMsgEntity.getFromId(),
                                        FtpFileEntity.State.RECYCLED)
                                        .order("createTime desc")
                                        .find(FtpFileEntity.class);
                            }

                            @Override
                            public void onSuccess(List<FtpFileEntity> result) {
                                Map<String, Object> ftpDeleteListMap = new HashMap<>();
                                ftpDeleteListMap.put("delPathList", result);
                                MQTTMsgEntity ftpDeleteListMsg = getMQTTMsg(MQTTMsgEntity.TYPE_CMD,
                                        MQTTMsgEntity.MSG_FTP_DELETE_LIST,
                                        mqttMsgEntity.getFromId());
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
                                    FtpFileEntity ftpFileEntity = LitePal.where("path = ? and deletePhoneNum = ? and " +
                                                    "state = ?",
                                            ftpCmdEntity.getPath(),
                                            mqttMsgEntity.getFromId(),
                                            FtpFileEntity.State.RECYCLED).findFirst(FtpFileEntity.class);
                                    if (null != ftpFileEntity) {
                                        ftpFileEntity.setState(FtpFileEntity.State.NORMAL);
                                        ftpFileEntity.save();
                                        FileUtils.move(ftpFileEntity.getRecycledPath(), ftpFileEntity.getPath());
                                    }
                                });
                                return true;
                            }

                            @Override
                            public void onSuccess(Boolean result) {
                                Map<String, Object> ftpRestoreParamMap = new HashMap<>();
                                ftpRestoreParamMap.put("result", result);
                                MQTTMsgEntity ftpDeleteMsg = getMQTTMsg(MQTTMsgEntity.TYPE_CMD,
                                        MQTTMsgEntity.MSG_FTP_RESTORE_LIST,
                                        mqttMsgEntity.getFromId());
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
                        ThreadUtils.executeByCached(new VoidTask() {
                            @Override
                            public Void doInBackground() {
                                if (erase) {
                                    List<FtpFileEntity> eraseList = LitePal.where("deletePhoneNum = ? and state = ?",
                                            mqttMsgEntity.getFromId(),
                                            FtpFileEntity.State.RECYCLED)
                                            .order("createTime desc")
                                            .find(FtpFileEntity.class);
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
                                                    "path = ? and deletePhoneNum = ? and state = ?",
                                                    ftpCmdEntity.getPath(),
                                                    mqttMsgEntity.getFromId(),
                                                    FtpFileEntity.State.RECYCLED);
                                        });
                                    }
                                }

                                Map<String, Object> ftpEraseParamMap = new HashMap<>();
                                ftpEraseParamMap.put("result", true);
                                MQTTMsgEntity ftpEraseMsg = getMQTTMsg(MQTTMsgEntity.TYPE_CMD,
                                        MQTTMsgEntity.MSG_FTP_ERASE,
                                        mqttMsgEntity.getFromId());
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
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm",
                                            Locale.getDefault());
                                    ftpFileEntityList.forEach(ftpFileEntity -> {
                                        FtpFileEntity ftpFile = LitePal.where("path = ? and state = ?",
                                                ftpFileEntity.getPath(),
                                                FtpFileEntity.State.NORMAL).findFirst(FtpFileEntity.class);
                                        if (isAdd) {
                                            if (null == ftpFile) {
                                                ftpFileEntity.setCreateTime(TimeUtils.millis2String(FileUtils.getFileLastModified(ftpFileEntity.getPath()),
                                                                                                    sdf));
                                                ftpFileEntity.setSize(String.valueOf(FileUtils.getLength(ftpFileEntity.getPath())));
                                                ftpFileEntity.setState(FtpFileEntity.State.NORMAL);
                                                ftpFileEntity.getPickSet().add(mqttMsgEntity.getFromId());
                                                ftpFileEntity.save();
                                            } else {
                                                if (!ftpFile.getPickSet().contains(mqttMsgEntity.getFromId())) {
                                                    ftpFile.getPickSet().add(mqttMsgEntity.getFromId());
                                                    ftpFile.save();
                                                }
                                            }
                                        } else {
                                            if (null != ftpFile && ftpFile.getPickSet().contains(mqttMsgEntity.getFromId())) {
                                                ftpFile.getPickSet().remove(mqttMsgEntity.getFromId());
                                                ftpFile.save();
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
                                MQTTMsgEntity ftpFavoritesMsg = getMQTTMsg(MQTTMsgEntity.TYPE_CMD,
                                        MQTTMsgEntity.MSG_FTP_FAVORITES,
                                        mqttMsgEntity.getFromId());
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
                                        LitePal.where("state = ?", FtpFileEntity.State.NORMAL)
                                                .order("createTime desc")
                                                .find(FtpFileEntity.class)
                                                .stream()
                                                .filter(ftpFileEntity -> ftpFileEntity.getPickSet()
                                                        .contains(mqttMsgEntity.getFromId()))
                                                .collect(Collectors.toList()));
                                MQTTMsgEntity ftpFavoritesListMsg = getMQTTMsg(MQTTMsgEntity.TYPE_CMD,
                                        MQTTMsgEntity.MSG_FTP_FAVORITES_LIST,
                                        mqttMsgEntity.getFromId());
                                ftpFavoritesListMsg.setParam(new JSONObject(ftpFavoritesListMap).toJSONString());
                                sendMQTTMsg(ftpFavoritesListMsg);
                                return null;
                            }
                        });
                        break;

                    //ftp文件查询
                    case MQTTMsgEntity.MSG_FTP_QUERY_LIST:
                        ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<FtpFileQueryPaginationEntity>() {
                            @Override
                            public FtpFileQueryPaginationEntity doInBackground() {
                                String queryPath = JSON.parseObject(mqttMsgEntity.getParam()).getString("path");
                                String name = JSON.parseObject(mqttMsgEntity.getParam()).getString("name");
                                int page = JSON.parseObject(mqttMsgEntity.getParam()).getInteger("page");
                                int size = JSON.parseObject(mqttMsgEntity.getParam()).getInteger("size");
                                List<FtpFileQueryEntity> ftpFileList = new ArrayList<>();
                                ContentResolver contentResolver = getContentResolver();
                                String selection;
                                String[] selectionArgs;
                                if (PathConfig.NAS.equals(queryPath)) {
                                    selection =
                                            "(" + MediaStore.Files.FileColumns.DATA + " like ? escape '/' or " + MediaStore.Files.FileColumns.DATA + " like ? escape '/') and " + MediaStore.Files.FileColumns.DISPLAY_NAME + " like ? escape '/'";
                                    selectionArgs = new String[]{CommonUtils.sqliteEscape(PathConfig.PUBLIC) + "%",
                                            CommonUtils.sqliteEscape(
                                                    PathConfig.PRIVATE + mqttMsgEntity.getFromId() + File.separator) + "%",
                                            "%" + CommonUtils.sqliteEscape(
                                                    name) + "%"};
                                } else {
                                    selection =
                                            MediaStore.Files.FileColumns.DATA + " like ? escape '/' and " + MediaStore.Files.FileColumns.DISPLAY_NAME + " like ? escape '/'";
                                    selectionArgs = new String[]{CommonUtils.sqliteEscape(queryPath) + "%",
                                            "%" + CommonUtils.sqliteEscape(name) + "%"};
                                }
                                Cursor cursor = contentResolver.query(MediaStore.Files.getContentUri("external"),
                                        new String[]{MediaStore.Files.FileColumns.DATA,
                                                MediaStore.Files.FileColumns.DISPLAY_NAME},
                                        selection,
                                        selectionArgs,
                                        null);
                                if (null != cursor) {
                                    while (cursor.moveToNext()) {
                                        String path =
                                                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA));
                                        String displayName =
                                                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME));
                                        FtpFileQueryEntity ftpFileQueryEntity = new FtpFileQueryEntity();
                                        ftpFileQueryEntity.setName(displayName);
                                        ftpFileQueryEntity.setPath(path);
                                        ftpFileQueryEntity.setType(FileUtils.isDir(path) ? "dir" : "file");
                                        ftpFileQueryEntity.setCreateTime(TimeUtils.millis2String(FileUtils.getFileLastModified(path),
                                                new SimpleDateFormat("yyyy-MM-dd HH:mm",
                                                        Locale.getDefault())));
                                        ftpFileQueryEntity.setSrc(path.startsWith(PathConfig.PUBLIC) ? "public" :
                                                "private");
                                        ftpFileList.add(ftpFileQueryEntity);
                                    }
                                    cursor.close();
                                }
                                FtpFileQueryPaginationEntity ftpFileQueryPaginationEntity =
                                        new FtpFileQueryPaginationEntity();
                                ftpFileQueryPaginationEntity.setPage(page);
                                ftpFileQueryPaginationEntity.setSize(size);
                                ftpFileQueryPaginationEntity.setTotal(ftpFileList.size());
                                if (!ftpFileList.isEmpty()) {
                                    if (ftpFileList.size() <= size) {//若查询列表条数小于单页最大条数，则返回全部列表
                                        ftpFileQueryPaginationEntity.setPage(1);
                                        ftpFileQueryPaginationEntity.getQueryList().addAll(ftpFileList);
                                    } else {//开始分页
                                        List<List<FtpFileQueryEntity>> partitionList = Lists.partition(ftpFileList,
                                                size);
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
                                MQTTMsgEntity ftpQueryListMsg = getMQTTMsg(MQTTMsgEntity.TYPE_CMD,
                                        MQTTMsgEntity.MSG_FTP_QUERY_LIST,
                                        mqttMsgEntity.getFromId());
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
                                MQTTMsgEntity ftpCategoryListMsg = getMQTTMsg(MQTTMsgEntity.TYPE_CMD,
                                        MQTTMsgEntity.MSG_FTP_CATEGORY_LIST,
                                        mqttMsgEntity.getFromId());
                                ftpCategoryListMsg.setParam(new JSONObject(ftpCategoryListMap).toJSONString());
                                sendMQTTMsg(ftpCategoryListMsg);
                            }
                        });
                        break;

                    //ftp文件状态刷新
                    case MQTTMsgEntity.MSG_FTP_STATUS_REFRESH:
                        List<FtpCmdEntity> refreshPathList = JSON.parseObject(mqttMsgEntity.getParam())
                                .getJSONArray("refreshPathList")
                                .toJavaList(FtpCmdEntity.class);
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
                                        MQTTMsgEntity ftpRefreshPathMsg = getMQTTMsg(MQTTMsgEntity.TYPE_CMD,
                                                MQTTMsgEntity.MSG_FTP_STATUS_REFRESH,
                                                mqttMsgEntity.getFromId());
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
                        String banPhoneNum = JSON.parseObject(mqttMsgEntity.getParam()).getString("banPhoneNum");
                        List<FtpFileEntity> banList = JSON.parseObject(mqttMsgEntity.getParam())
                                .getJSONArray("banList")
                                .toJavaList(FtpFileEntity.class);
                        ThreadUtils.executeByCached(new VoidTask() {
                            @Override
                            public Void doInBackground() {
                                if (null != banList && !banList.isEmpty()) {
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                                    banList.forEach(ftpFileEntity -> {
                                        FtpFileEntity fileEntity = LitePal.where("path = ? and state = ?",
                                                ftpFileEntity.getPath(),
                                                FtpFileEntity.State.NORMAL).findFirst(FtpFileEntity.class);
                                        if (isBan) {
                                            if (null == fileEntity) {
                                                ftpFileEntity.setCreateTime(TimeUtils.millis2String(FileUtils.getFileLastModified(ftpFileEntity.getPath()),
                                                                                                    sdf));
                                                ftpFileEntity.setSize(String.valueOf(FileUtils.getLength(ftpFileEntity.getPath())));
                                                ftpFileEntity.setState(FtpFileEntity.State.NORMAL);
                                                ftpFileEntity.getBanSet().add(banPhoneNum);
                                                ftpFileEntity.save();
                                            } else {
                                                if (!fileEntity.getBanSet().contains(banPhoneNum)) {
                                                    fileEntity.getBanSet().add(banPhoneNum);
                                                    fileEntity.setExt(ftpFileEntity.getExt());
                                                    fileEntity.save();
                                                }
                                            }
                                        } else {
                                            if (null != fileEntity && fileEntity.getBanSet().contains(banPhoneNum)) {
                                                fileEntity.getPickSet().remove(banPhoneNum);
                                                fileEntity.setExt("");
                                                fileEntity.save();
                                            }
                                        }
                                    });
                                }

                                Map<String, Object> ftpBanListParamMap = new HashMap<>();
                                ftpBanListParamMap.put("result", true);
                                MQTTMsgEntity ftpBanListMsg = getMQTTMsg(MQTTMsgEntity.TYPE_CMD,
                                        MQTTMsgEntity.MSG_FTP_BAN_LIST,
                                        mqttMsgEntity.getFromId());
                                ftpBanListMsg.setParam(new JSONObject(ftpBanListParamMap).toJSONString());
                                sendMQTTMsg(ftpBanListMsg);
                                return null;
                            }
                        });
                        break;

                    //ftp获取禁止访问列表
                    case MQTTMsgEntity.MSG_FTP_GET_BAN_LIST:
                        String getBanPhoneNum = JSON.parseObject(mqttMsgEntity.getParam()).getString("phoneNum");
                        ThreadUtils.executeByCached(new VoidTask() {
                            @Override
                            public Void doInBackground() {
                                Map<String, Object> ftpGetBanListMap = new HashMap<>();
                                ftpGetBanListMap.put("banList",
                                        LitePal.where("state = ?", FtpFileEntity.State.NORMAL)
                                                .order("createTime desc")
                                                .find(FtpFileEntity.class)
                                                .stream()
                                                .filter(ftpFileEntity -> ftpFileEntity.getBanSet().contains(getBanPhoneNum))
                                                .collect(Collectors.toList()));
                                MQTTMsgEntity ftpGetBanListMsg = getMQTTMsg(MQTTMsgEntity.TYPE_CMD,
                                        MQTTMsgEntity.MSG_FTP_GET_BAN_LIST,
                                        mqttMsgEntity.getFromId());
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
            selection = MediaStore.Files.FileColumns.DATA + " like ?";
            selectionArgs = new String[]{PathConfig.PRIVATE + phoneNum + File.separator + "%"};
        } else {
            selection = MediaStore.Files.FileColumns.DATA + " like ? or " + MediaStore.Files.FileColumns.DATA + " like ?";
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
            boolean isPick;
            FtpFileEntity ftpFileEntity = LitePal.where("path = ? and state = ?", path, FtpFileEntity.State.NORMAL).findFirst(FtpFileEntity.class);
            if (null == ftpFileEntity) {
                isPick = false;
            } else {
                isPick = ftpFileEntity.getPickSet().contains(phoneNum);
            }
            list.add(new FtpCategoryEntity(name, path, createTime, size, src, isPick));
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

    private void coverageFile(String path, String destPath, String type, int index) {
        String filePath = FtpFileEntity.RE.equals(type) ? destPath : path;
        String filename = FileUtils.getFileName(filePath), name = FileUtils.getFileNameNoExtension(filePath), extension = FileUtils.getFileExtension(
                filePath), newPath;
        if (name.isEmpty() && extension.isEmpty()) {
            return;
        }
        String destDirPath = FtpFileEntity.RE.equals(type) ? FileUtils.getDirName(destPath) : destPath;
        if (name.isEmpty()) {
            newPath = destDirPath + filename + " (" + index + ")";
        } else if (extension.isEmpty()) {
            newPath = destDirPath + name + " (" + index + ")";
        } else {
            newPath = destDirPath + name + " (" + index + ")." + extension;
        }
        if (FileUtils.isFileExists(newPath)) {
            index++;
            coverageFile(path, destPath, type, index);
            return;
        }
        switch (type) {
            case FtpFileEntity.CP:
                FileUtils.copy(path, newPath);
                new MediaScanner().scanFile(newPath);
                break;

            case FtpFileEntity.MV:
                FileUtils.move(path, newPath);
                new MediaScanner().scanFile(path);
                new MediaScanner().scanFile(newPath);
                FtpFileEntity mvFtpFileEntity = LitePal.where("path = ? and state = ?", path, FtpFileEntity.State.NORMAL)
                        .findFirst(FtpFileEntity.class);
                if (null != mvFtpFileEntity) {
                    mvFtpFileEntity.setPath(newPath);
                    mvFtpFileEntity.save();
                }
                break;

            case FtpFileEntity.RE:
                FileUtils.rename(path, FileUtils.getFileName(newPath));
                new MediaScanner().scanFile(path);
                new MediaScanner().scanFile(newPath);
                FtpFileEntity reFtpFileEntity = LitePal.where("path = ? and state = ?", path, FtpFileEntity.State.NORMAL)
                        .findFirst(FtpFileEntity.class);
                if (null != reFtpFileEntity) {
                    reFtpFileEntity.setPath(newPath);
                    reFtpFileEntity.save();
                }
                break;

            default:
                break;
        }
    }

    public interface TipsDevicesFriend {
        void onTipsdevicesFriend(String requestID);

        void onTipsdevicesFriendStatus(String statusid);
    }

    public interface welcomebind {
        Void onWelcomeBind(String bindStr);
    }

    public interface userUpBind {
        Void onUpBind(String bindStr);
    }

    public void setUserUpBind(userUpBind userupbind) {
        this.upBindStr = userupbind;
    }

    public void setTipsDevicesFriend(TipsDevicesFriend tipsdevicesfriend) {
        this.tipsdevicesfriend = tipsdevicesfriend;
    }

    public void setWelcomeserver(welcomebind welcomeBindStr) {
        this.welcomeBindStr = welcomeBindStr;
    }

    public welcomebind getWelcomeBindStr() {
        return welcomeBindStr;
    }

    public IBinder onBind(Intent intent) {
        return new DownLoadBinder();
    }

    public class DownLoadBinder extends Binder {
        public MQTTService getService() {
            return MQTTService.this;
        }
    }

    private void registerNetworkStatusChangedListener() {
        if (!NetworkUtils.isRegisteredNetworkStatusChangedListener(mOnNetworkStatusChangedListener)) {
            NetworkUtils.registerNetworkStatusChangedListener(mOnNetworkStatusChangedListener);
        }
    }

    private final NetworkUtils.OnNetworkStatusChangedListener mOnNetworkStatusChangedListener = new NetworkUtils.OnNetworkStatusChangedListener() {
        @Override
        public void onDisconnected() {
            EventBus.getDefault().post(BusConfig.NETWORK_DISCONNECTED);
        }

        @Override
        public void onConnected(NetworkUtils.NetworkType networkType) {
            EventBus.getDefault().post(BusConfig.NETWORK_CONNECTED);
        }
    };
}
