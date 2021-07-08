package com.viegre.nas.pad.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Environment;
import android.os.Handler;
import android.os.storage.StorageManager;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ServiceUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.djangoogle.framework.activity.BaseActivity;
import com.google.gson.Gson;
import com.kongzue.dialog.interfaces.OnDialogButtonClickListener;
import com.kongzue.dialog.interfaces.OnDismissListener;
import com.kongzue.dialog.util.BaseDialog;
import com.kongzue.dialog.v3.MessageDialog;
import com.kongzue.dialog.v3.TipDialog;
import com.kongzue.dialog.v3.WaitDialog;
import com.viegre.nas.pad.BuildConfig;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.activity.audio.AudioActivity;
import com.viegre.nas.pad.activity.im.ContactsActivity;
import com.viegre.nas.pad.activity.image.ImageActivity;
import com.viegre.nas.pad.activity.video.VideoActivity;
import com.viegre.nas.pad.config.BusConfig;
import com.viegre.nas.pad.config.PathConfig;
import com.viegre.nas.pad.config.SPConfig;
import com.viegre.nas.pad.config.UrlConfig;
import com.viegre.nas.pad.databinding.ActivityMainBinding;
import com.viegre.nas.pad.entity.DevicesTokenEntity;
import com.viegre.nas.pad.entity.LoginEntity;
import com.viegre.nas.pad.entity.LoginResult;
import com.viegre.nas.pad.entity.LoglinCodeEntity;
import com.viegre.nas.pad.entity.UserTokenTime;
import com.viegre.nas.pad.entity.WeatherEntity;
import com.viegre.nas.pad.manager.AMapLocationManager;
import com.viegre.nas.pad.service.AppService;
import com.viegre.nas.pad.service.MscService;
import com.viegre.nas.pad.service.ScreenSaverService;
import com.viegre.nas.pad.task.VoidTask;
import com.viegre.nas.pad.util.CommonUtils;
import com.youth.banner.Banner;
import com.youth.banner.adapter.BannerImageAdapter;
import com.youth.banner.holder.BannerImageHolder;
import com.youth.banner.indicator.CircleIndicator;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cn.wildfire.chat.kit.ChatManagerHolder;
import cn.wildfirechat.message.CallStartMessageContent;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.OnMessageUpdateListener;
import hdp.http.APIConstant;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import rxhttp.RxHttp;
import rxhttp.RxHttpPlugins;

/**
 * Created by レインマン on 2020/12/15 09:29 with Android Studio.
 */
public class MainActivity extends BaseActivity<ActivityMainBinding> implements OnMessageUpdateListener {

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    private final Map<String, Integer> mWeatherMap = new HashMap<>();

    @Override
    protected void initialize() {
        if ("official".equals(BuildConfig.FLAVOR)) {
            ServiceUtils.startService(MscService.class);
        }
        ServiceUtils.startService(ScreenSaverService.class);
        ChatManager.Instance().addOnMessageUpdateListener(this);
//		getUsbPermission();
        initClick();
        initIcon();
        initBanner();
        initWeather();
//        loginIM();
//		getUsbPaths();
        if (NetworkUtils.isConnected()) {
            mViewBinding.llcMainUnconnected.setVisibility(View.GONE);
        } else {
            mViewBinding.llcMainUnconnected.setVisibility(View.VISIBLE);
        }
    }

    private void getUsbPaths() {
        try {
            @SuppressLint("WrongConstant")
            StorageManager srgMgr = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
            Class<StorageManager> srgMgrClass = StorageManager.class;
            String[] paths = (String[]) srgMgrClass.getMethod("getVolumePaths").invoke(srgMgr);
            if (null != paths && paths.length > 0) {
                for (String path : paths) {
                    Object volumeState = srgMgrClass.getMethod("getVolumeState", String.class).invoke(srgMgr, path);
                    if (!path.contains("emulated") && Environment.MEDIA_MOUNTED.equals(volumeState)) {
                        LogUtils.iTag("getUsbPaths", path);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        getLoginIM();
    }

    //判断是否出现token错误的情况，如果没有则不需要重新登录
    private void getLoginIM() {
        if (!ContactsActivity.Token_valid) {
            loginIM();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initUserInfo();
        AMapLocationManager.INSTANCE.getLocation();
    }

    //登录音视频通话服务器
    private void loginIM() {
        //音视频登录 04f69b6004f9cc41
        WaitDialog.show(this, "请稍候...");
        String ANDROID_ID = SPUtils.getInstance().getString(SPConfig.ANDROID_ID);
        String authCode = "66666";
        //需要注意token跟clientId是强依赖的，一定要调用getClientId获取到clientId，然后用这个clientId获取token，这样connect才能成功，如果随便使用一个clientId获取到的token将无法链接成功。
        ChatManagerHolder.gChatManager.disconnect(true, true);
        AppService.Instance().smsLogin(ANDROID_ID, authCode, new AppService.LoginCallback() {
            @Override
            public void onUiSuccess(LoginResult loginResult) {
                if (isFinishing()) {
                    TipDialog.show(MainActivity.this, "登录失败", TipDialog.TYPE.ERROR).doDismiss();
                    return;
                }
                ThreadUtils.executeByCachedWithDelay(new VoidTask() {
                    @Override
                    public Void doInBackground() {
                        ChatManagerHolder.gChatManager.connect(loginResult.getUserId(), loginResult.getToken());
                        SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
                        sp.edit()
                          .putString("id", loginResult.getUserId())
                          .putString("token", loginResult.getUserId())
                          .putString("mToken", loginResult.getToken())
                          .apply();
                        Log.d("MainInfo:", ANDROID_ID + "," + loginResult.getUserId());
                        getDevicesToken(ANDROID_ID, loginResult.getUserId());
                        return null;
                    }
                }, 3L, TimeUnit.SECONDS);
            }

            @Override
            public void onUiFailure(int code, String msg) {
                if (isFinishing()) {
                    return;
                }
                TipDialog.show(MainActivity.this, "登录失败", TipDialog.TYPE.ERROR).doDismiss();
//                Toast.makeText(MainActivity.this, "登录失败：" + code + " " + msg, Toast.LENGTH_SHORT).show();
//                loginButton.setEnabled(true);
            }
        });
    }

    private void getDevicesToken(String android_id, String userid) {
        String url = UrlConfig.Device.GET_DEVICESTOKEN;
        RxHttp.postForm(url).add("sn", android_id).asString().observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                Log.d("", "");
            }

            @Override
            public void onNext(@NonNull String s) {
                //添加公共请求头
//                        {"code":0,"msg":"OK","data":{"token":"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpdGVtIjp7Iml0ZW1JZCI6ImY2ZmUyNTkyMmZhMjAyOGEiLCJpdGVtVHlwZSI6IjIifSwiaXNzIjoiYXV0aDAiLCJleHAiOjE2MjA0MjA4ODh9.DJk9tVcaIK62PQbR_c8zwkyHxDB0zP3Mvc_In7pcrac"}}
//                        {"code":0,"msg":"OK","data":null}
//                        {"code":1000,"msg":"设备不存在","data":null}
                Gson gson = new Gson();
                DevicesTokenEntity loglinCodeEntity = gson.fromJson(s, DevicesTokenEntity.class);
                if (loglinCodeEntity.getMsg().equals("OK")) {
                    String token = loglinCodeEntity.getData().getToken();
                    SPUtils.getInstance().put("token", token);
                    postCallId(token, android_id, userid);
                } else {
                    mViewBinding.mainError.setText("提示：ID " + android_id + ", " + loglinCodeEntity.getMsg() + "!");
                    Log.e("提示：ID ", android_id + ", " + loglinCodeEntity.getMsg() + "!");
                    TipDialog.show(MainActivity.this, loglinCodeEntity.getMsg(), TipDialog.TYPE.ERROR).doDismiss();
                }
            }

            @Override
            public void onError(@NonNull Throwable e) {
//                        CommonUtils.showErrorToast(e.getMessage());
                TipDialog.show(MainActivity.this, "登录失败", TipDialog.TYPE.ERROR).doDismiss();
            }

            @Override
            public void onComplete() {
                Log.d("", "");
//                        SPUtils.getInstance().remove(SPConfig.LOGIN_CODE_SESSION_ID);
////                        mViewBinding.actvLoginAccountBtn.setClickable(true);
            }
        });

//        postCallId(android_id,android_id);
    }

    private void postCallId(String token, String Callid, String sn) {
        RxHttp.postForm(UrlConfig.Call.GET_REPORTINFO)
              .addHeader("token", token)
              .add("itemId", Callid)
              .add("callId", sn)
              .add("itemType", 2)
              .asString()
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(new Observer<String>() {
                  @Override
                  public void onSubscribe(@NonNull Disposable d) {
                      Log.d("", "");
                  }

                  @Override
                  public void onNext(@NonNull String s) {
                      //添加公共请求头
//                        {"code":0,"msg":"OK","data":null}
                      Gson gson = new Gson();
                      LoglinCodeEntity loglinCodeEntity = gson.fromJson(s, LoglinCodeEntity.class);
                      String msg = loglinCodeEntity.getMsg();
                      Log.d("postCallId：", msg);
                      TipDialog.show(MainActivity.this, "成功", TipDialog.TYPE.SUCCESS).doDismiss();
//                        CommonUtils.showErrorToast(msg);
//                        {"code":500,"msg":"服务器内部异常","data":null}
                  }

                  @Override
                  public void onError(@NonNull Throwable e) {
//                        CommonUtils.showErrorToast(e.getMessage());
                      TipDialog.show(MainActivity.this, "登录失败", TipDialog.TYPE.ERROR).doDismiss();
                  }

                  @Override
                  public void onComplete() {
                      Log.d("", "");
//                        SPUtils.getInstance().remove(SPConfig.LOGIN_CODE_SESSION_ID);
////                        mViewBinding.actvLoginAccountBtn.setClickable(true);
                  }
              });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * 初始化用户区域
     */
    private void initUserInfo() {
        if (SPUtils.getInstance().contains(SPConfig.PHONE)) {
            Glide.with(mActivity)
                 .load(CommonUtils.stringToBitmap(SPUtils.getInstance().getString(SPConfig.USERICON)))
                 .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                 .into(mViewBinding.acivMainUserIcon);
            mViewBinding.actvMainUserInfo.setText(CommonUtils.getMarkedPhoneNumber(SPUtils.getInstance().getString(SPConfig.PHONE)));
            mViewBinding.llcMainUser.setOnClickListener(null);
//			计算token是否过期
//            {"phone":"15357906428","token_start_time":1625640110622,"token_hour_time":24}
            String jsonTokenInfo = SPUtils.getInstance().getString(SPConfig.TOKEN_TIME);
            if (!"".equals(jsonTokenInfo)) {
                UserTokenTime userTokenTime = new Gson().fromJson(jsonTokenInfo, UserTokenTime.class);
                Long token_hour_time = Long.valueOf(userTokenTime.getToken_hour_time() * 60);
                long minute = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - userTokenTime.getToken_start_time());
                if (minute <= token_hour_time) {
                    getLoginIM();
//                refreshToken(userTokenTime.getToken_hour_time());
                } else {
                    showTips();//过期token，提示
                }
            }
        } else {
            Glide.with(mActivity)
                 .load(R.mipmap.main_unlogin)
                 .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                 .into(mViewBinding.acivMainUserIcon);
//			mViewBinding.actvMainUserInfo.setText(R.string.main_click_to_login);
            mViewBinding.actvMainUserInfo.setText(R.string.main_click_to_login1);
            mViewBinding.llcMainUser.setOnClickListener(view -> ActivityUtils.startActivity(LoginActivity.class));
        }
        mViewBinding.llcMainUser.setVisibility(View.VISIBLE);
    }

    private void refreshToken(int token_hour_time) {
        TipDialog show = WaitDialog.show(this, "请稍候...");
        RxHttp.postForm(UrlConfig.User.REFRESH_TOKEN)
              .addHeader("token", SPUtils.getInstance().getString(SPConfig.TOKEN))
              .add("hour", token_hour_time)
              .asResponse(LoginEntity.class)
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(new Observer<LoginEntity>() {
                  @Override
                  public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
                  }

                  @Override
                  public void onNext(@io.reactivex.rxjava3.annotations.NonNull LoginEntity loginEntity) {
//                        token_hour_time
                      SPUtils.getInstance().put(SPConfig.TOKEN, loginEntity.getToken());
                      RxHttpPlugins.init(RxHttpPlugins.getOkHttpClient())
                                   .setOnParamAssembly(param -> param.addHeader("token", loginEntity.getToken()));
                      loginIM();
                  }

                  @Override
                  public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
//                        token verify fail
                      if (e.getMessage().equals("token verify fail")) {
                          show.doDismiss();
                          showTips();//提示信息
                      } else {
                          CommonUtils.showErrorToast(e.getMessage());
                          SPUtils.getInstance().remove(SPConfig.PHONE);
                      }
                  }

                  @Override
                  public void onComplete() {

                  }
              });
    }

    private void showTips() {
        MessageDialog.show(this, "提示", "登录已经过期,请重新登录！", "是", "取消").setOnOkButtonClickListener(new OnDialogButtonClickListener() {
            @Override
            public boolean onClick(BaseDialog baseDialog, View v) {
                WaitDialog.show(MainActivity.this, "请稍候...");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                        ResetRecord();
                                TipDialog.show(MainActivity.this, "成功！", TipDialog.TYPE.SUCCESS).setOnDismissListener(new OnDismissListener() {
                                    @Override
                                    public void onDismiss() {
                                        //清空手机号
                                        SPUtils.getInstance().remove(SPConfig.PHONE);
                                        //清空token
                                        SPUtils.getInstance().remove(SPConfig.TOKEN);
                                        //跳转到登录界面
                                        ActivityUtils.startActivity(LoginActivity.class);
                                    }
                                });
                            }
                        });
                    }
                }, 300);
                return false;
            }
        }).setOnCancelButtonClickListener(new OnDialogButtonClickListener() {
            @Override
            public boolean onClick(BaseDialog baseDialog, View v) {
                //清空手机号
                SPUtils.getInstance().remove(SPConfig.PHONE);
                //清空token
                SPUtils.getInstance().remove(SPConfig.TOKEN);
                return false;
            }
        }).setButtonOrientation(LinearLayout.VERTICAL);
    }

    private void initClick() {
//		if (BuildConfig.DEBUG) {
//			mViewBinding.tcMainTime.setOnClickListener(view -> {
//			});
//		}
        mViewBinding.llcMainUSBInfo.setOnClickListener(view -> ActivityUtils.startActivity(ExternalStorageActivity.class));
        mViewBinding.acivMainIncomingCall.setOnClickListener(view -> ActivityUtils.startActivity(ContactsActivity.class));
    }

    private void initIcon() {
        //图片
        Glide.with(this)
             .load(R.mipmap.main_icon_image)
             .apply(RequestOptions.bitmapTransform(new RoundedCorners(24)))
             .into(mViewBinding.acivMainIconImage);
        mViewBinding.acivMainIconImage.setOnClickListener(view -> ActivityUtils.startActivity(ImageActivity.class));

        //音频
        Glide.with(this)
             .load(R.mipmap.main_icon_audio)
             .apply(RequestOptions.bitmapTransform(new RoundedCorners(24)))
             .into(mViewBinding.acivMainIconAudio);
        mViewBinding.acivMainIconAudio.setOnClickListener(view -> ActivityUtils.startActivity(AudioActivity.class));

        //视频
        Glide.with(this)
             .load(R.mipmap.main_icon_video)
             .apply(RequestOptions.bitmapTransform(new RoundedCorners(24)))
             .into(mViewBinding.acivMainIconVideo);
        mViewBinding.acivMainIconVideo.setOnClickListener(view -> ActivityUtils.startActivity(VideoActivity.class));

        Glide.with(this).load(R.mipmap.test_icon_3).apply(RequestOptions.bitmapTransform(new RoundedCorners(24))).into(mViewBinding.acivMainIcon3);
        Glide.with(this).load(R.mipmap.test_icon_4).apply(RequestOptions.bitmapTransform(new RoundedCorners(24))).into(mViewBinding.acivMainIcon4);
        Glide.with(this).load(R.mipmap.test_icon_5).apply(RequestOptions.bitmapTransform(new RoundedCorners(24))).into(mViewBinding.acivMainIcon5);
        Glide.with(this).load(R.mipmap.test_icon_6).apply(RequestOptions.bitmapTransform(new RoundedCorners(24))).into(mViewBinding.acivMainIcon6);
        Glide.with(this).load(R.mipmap.test_icon_7).apply(RequestOptions.bitmapTransform(new RoundedCorners(24))).into(mViewBinding.acivMainIcon7);
        Glide.with(this).load(R.mipmap.test_icon_8).apply(RequestOptions.bitmapTransform(new RoundedCorners(24))).into(mViewBinding.acivMainIcon8);
        mViewBinding.acivMainIcon5.setOnClickListener(view -> {
            Intent liveIntent = new Intent();
            liveIntent.putExtra(APIConstant.HIDE_LOADING_DEFAULT, true);
            liveIntent.putExtra(APIConstant.HIDE_EXIT_DIAG, true);
            liveIntent.setAction("com.hdpfans.live.start");
            liveIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            liveIntent.putExtra("ChannelNum", 1);
            ActivityUtils.startActivity(liveIntent);
        });
        mViewBinding.acivMainIcon8.setOnClickListener(view -> ActivityUtils.startActivity(MoreAppActivity.class));//跳转到更多应用activity中
    }

    private void initBanner() {
        List<String> bannerList = new ArrayList<>();
        bannerList.add(
                "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fattach.bbs.miui.com%2Fforum%2F201408%2F07%2F213601f2xz7usscm2z1mjh.jpg&refer=http%3A%2F%2Fattach.bbs.miui.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=jpeg?sec=1624012837&t=89dae71696d130dcd16e8d3e172e3581");
        bannerList.add(
                "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fattach.bbs.miui.com%2Fforum%2F201401%2F04%2F114458foyo99odqb8qjzg4.jpg&refer=http%3A%2F%2Fattach.bbs.miui.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=jpeg?sec=1624012837&t=1e5b4e8b874cb0a28b6881a2df2d1e0c");
        bannerList.add("https://img.dpm.org.cn/Uploads/Picture/2021/04/30/s608b768f32fd9.jpg");
        bannerList.add("https://img.dpm.org.cn/Uploads/Picture/2021/04/30/s608b75e282420.jpg");
        bannerList.add("https://img.dpm.org.cn/Uploads/Picture/2021/04/30/s608b744114525.jpg");
        bannerList.add(
                "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fattach.bbs.miui.com%2Fforum%2F201306%2F21%2F220728m5zcr5ecr7cqq7bw.jpg&refer=http%3A%2F%2Fattach.bbs.miui.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=jpeg?sec=1624012837&t=76888dc2a6bb90cc3b028ebec7f523b2");
        bannerList.add("https://ss2.bdstatic.com/70cFvnSh_Q1YnxGkpoWK1HF6hhy/it/u=3060410847,3338028579&fm=26&gp=0.jpg");
        Banner<String, BannerImageAdapter<String>> bMainBanner = findViewById(R.id.bMainBanner);
        bMainBanner.setAdapter(new BannerImageAdapter<String>(bannerList) {
            @Override
            public void onBindView(BannerImageHolder holder, String data, int position, int size) {
                Glide.with(holder.itemView).load(data).into(holder.imageView);
                holder.imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(MainActivity.this, WebActivity.class));
                    }
                });
            }
        }).addBannerLifecycleObserver(this).setBannerRound2(16F).setIndicator(new CircleIndicator(this));
    }

    /**
     * 初始化天气
     */
    private void initWeather() {
        mViewBinding.llcMainWeather.setOnClickListener(view -> AMapLocationManager.INSTANCE.getLocation());
        ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<HashMap<String, Integer>>() {
            @Override
            public HashMap<String, Integer> doInBackground() {
                String[] weatherNameArr = getResources().getStringArray(R.array.main_weather_name);
                TypedArray typedArray = getResources().obtainTypedArray(R.array.main_weather_icon);
                HashMap<String, Integer> weatherMap = new HashMap<>();
                for (int i = 0; i < weatherNameArr.length; i++) {
                    weatherMap.put(weatherNameArr[i], typedArray.getResourceId(i, 0));
                }
                typedArray.recycle();
                return weatherMap;
            }

            @Override
            public void onSuccess(HashMap<String, Integer> result) {
                mWeatherMap.putAll(result);
                AMapLocationManager.INSTANCE.getLocation();
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getWeather(WeatherEntity weatherEntity) {
        if (null != weatherEntity && !mWeatherMap.isEmpty()) {
            for (Map.Entry<String, Integer> entry : mWeatherMap.entrySet()) {
                String name = entry.getKey();
                if (name.contains(weatherEntity.getWeather())) {
                    mViewBinding.acivMainWeather.setImageResource(entry.getValue());
                    mViewBinding.actvMainTemperature.setText(StringUtils.getString(R.string.weather_unknown_temperature,
                                                                                   weatherEntity.getCurtemperature()));
                    return;
                }
            }
        }
    }

//    @SuppressLint("WrongConstant")
//    private void getUsbPermission() {
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
//        IntentFilter intentFilter = new IntentFilter(ACTION_USB_PERMISSION);
//        registerReceiver(mUsbPermissionReceiver, intentFilter);
//        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
//        for (UsbDevice usbDevice : usbManager.getDeviceList().values()) {
//            int deviceClass = usbDevice.getDeviceClass();
//            if (0 == deviceClass) {
//                UsbInterface usbInterface = usbDevice.getInterface(0);
//                int interfaceClass = usbInterface.getInterfaceClass();
//                if (8 == interfaceClass) {
//                    if (!usbManager.hasPermission(usbDevice)) {
//                        LogUtils.iTag("getUsbPermission", usbDevice.getProductName() + ": 未获取权限，开始申请");
//                        usbManager.requestPermission(usbDevice, pendingIntent);
//                    } else {
//                        LogUtils.iTag("getUsbPermission", usbDevice.getProductName() + ": 权限已获取");
//                        for (UsbMassStorageDevice device : UsbMassStorageDevice.getMassStorageDevices(Utils.getApp())) {
//                            try {
//                                device.init();
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                            if (device.getPartitions().isEmpty()) {
//                                ToastUtils.showLong("分区为空");
//                                LogUtils.iTag("getUsbPermission", "分区为空");
//                                return;
//                            }
//                            FileSystem currentFs = device.getPartitions().get(0).getFileSystem();
//                            ToastUtils.showLong(currentFs.getVolumeLabel() + " - " + currentFs.getRootDirectory()
//                                    .getAbsolutePath());
//                            LogUtils.iTag("getUsbPermission",
//                                    currentFs.getVolumeLabel(),
//                                    currentFs.getRootDirectory().getAbsolutePath());
//                        }
//                    }
//                    break;
//                }
//            }
//        }
//    }
//
//    private final BroadcastReceiver mUsbPermissionReceiver = new BroadcastReceiver() {
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            if (ACTION_USB_PERMISSION.equals(action)) {
//                synchronized (this) {
//                    UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
//                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
//                        if (null != usbDevice) {
//                            for (UsbMassStorageDevice device : UsbMassStorageDevice.getMassStorageDevices(Utils.getApp())) {
//                                try {
//                                    device.init();
//                                } catch (IOException e) {
//                                    e.printStackTrace();
//                                }
//                                if (device.getPartitions().isEmpty()) {
//                                    ToastUtils.showLong("分区为空");
//                                    LogUtils.iTag("getUsbPermission", "分区为空");
//                                    return;
//                                }
//                                FileSystem currentFs = device.getPartitions().get(0).getFileSystem();
//                                ToastUtils.showLong(currentFs.getVolumeLabel() + " - " + currentFs.getRootDirectory()
//                                        .getAbsolutePath());
//                                LogUtils.iTag("getUsbPermission",
//                                        currentFs.getVolumeLabel(),
//                                        currentFs.getRootDirectory().getAbsolutePath());
//                            }
//                        } else {
//                            ToastUtils.showLong("设备为空");
//                            LogUtils.eTag("getUsbPermission", "设备为空");
//                        }
//                    } else {
//                        ToastUtils.showLong("permission denied for device " + usbDevice);
//                        LogUtils.eTag("getUsbPermission", "permission denied for device " + usbDevice);
//                    }
//                }
//            }
//        }
//    };

    @Override
    public void onMessageUpdate(Message message) {
        Log.d("onMessageUpdate:message", message.toString());
        try {
            CallStartMessageContent me = (CallStartMessageContent) message.content;
            //大于0 为挂断状态
            if (me.getEndTime() > 0) {
                JSONObject jsStr = new JSONObject();
                jsStr.put("targetId", message.conversation.target);
                jsStr.put("Direction", message.direction.toString());
                jsStr.put("AudioOnly", me.isAudioOnly());
                jsStr.put("ConnectTime", me.getConnectTime());
                jsStr.put("EndTime", me.getEndTime());
                jsStr.put("CallId", me.getCallId());
                jsStr.put("MessageUid", message.messageUid);
                jsStr.put("CallTime", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(Calendar.getInstance().getTime()));
                if (me.getConnectTime() > 0 && me.getEndTime() > 0) {
                    Long q = (me.getEndTime() - me.getConnectTime());
//                    int i = q.intValue();
                    Long time = q / 1000;
                    String str = "";
                    if (time > 3600) {
                        str = String.format("%d:%02d:%02d", time / 3600, time / 60, time % 60);
                    } else {
                        str = String.format("%d:%02d:%02d", time / 3600, time / 60, time % 60);
                    }
                    jsStr.put("TurnOnTime", str);
                    jsStr.put("TurnOn", true);
                } else {
                    jsStr.put("TurnOnTime", "0");
                    jsStr.put("TurnOn", false);
                }
                initContactsFile();//判断本地是否有相关文件存储数据 没有则创建
                FileWriter fileWriter = new FileWriter(getFilesDir().toString() + PathConfig.CONTACTS_RECOMDING, true);
                BufferedWriter vBufferedWriter = new BufferedWriter(fileWriter);
                vBufferedWriter.append(jsStr.toString());
                vBufferedWriter.newLine();
                vBufferedWriter.close();
                fileWriter.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initContactsFile() {
        File file = new File(getFilesDir().toString() + "/" + "Recomding.txt");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void networkConnected(String event) {
        if (BusConfig.NETWORK_CONNECTED.equals(event)) {
            mViewBinding.llcMainUnconnected.setVisibility(View.GONE);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void networkDisconnected(String event) {
        if (BusConfig.NETWORK_DISCONNECTED.equals(event)) {
            mViewBinding.llcMainUnconnected.setVisibility(View.VISIBLE);
		}
	}
}
