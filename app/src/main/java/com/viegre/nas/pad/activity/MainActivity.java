package com.viegre.nas.pad.activity;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.BusUtils;
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
import com.viegre.nas.pad.BuildConfig;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.activity.audio.AudioActivity;
import com.viegre.nas.pad.activity.im.ContactsActivity;
import com.viegre.nas.pad.activity.image.ImageActivity;
import com.viegre.nas.pad.activity.video.VideoActivity;
import com.viegre.nas.pad.config.BusConfig;
import com.viegre.nas.pad.config.SPConfig;
import com.viegre.nas.pad.config.UrlConfig;
import com.viegre.nas.pad.databinding.ActivityMainBinding;
import com.viegre.nas.pad.entity.DevicesTokenEntity;
import com.viegre.nas.pad.entity.LoginResult;
import com.viegre.nas.pad.entity.LoglinCodeEntity;
import com.viegre.nas.pad.entity.WeatherEntity;
import com.viegre.nas.pad.manager.AMapLocationManager;
import com.viegre.nas.pad.manager.AccessibilityServiceManager;
import com.viegre.nas.pad.service.AppService;
import com.viegre.nas.pad.service.MscService;
import com.viegre.nas.pad.service.WakeupService;
import com.viegre.nas.pad.task.VoidTask;
import com.viegre.nas.pad.util.CommonUtils;
import com.youth.banner.Banner;
import com.youth.banner.adapter.BannerImageAdapter;
import com.youth.banner.holder.BannerImageHolder;
import com.youth.banner.indicator.CircleIndicator;

import org.primftpd.PrimitiveFtpdActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cn.wildfire.chat.kit.ChatManagerHolder;
import hdp.http.APIConstant;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import rxhttp.RxHttp;

/**
 * Created by レインマン on 2020/12/15 09:29 with Android Studio.
 */
public class MainActivity extends BaseActivity<ActivityMainBinding> {

	private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

	private final Map<String, Integer> mWeatherMap = new HashMap<>();
	private UsbManager mUsbManager;

	@Override
	protected void initialize() {
		ServiceUtils.startService(MscService.class);
		openUsbDevice();
		initClick();
		initIcon();
		initBanner();
		initWeather();
		loginIM();
	}

	//登录音视频通话服务器
	private void loginIM() {
		//音视频登录
		String ANDROID_ID = SPUtils.getInstance().getString(SPConfig.ANDROID_ID);
		String authCode = "66666";
		AppService.Instance().smsLogin(ANDROID_ID, authCode, new AppService.LoginCallback() {
			@Override
			public void onUiSuccess(LoginResult loginResult) {
				if (isFinishing()) {
					return;
				}
				//需要注意token跟clientId是强依赖的，一定要调用getClientId获取到clientId，然后用这个clientId获取token，这样connect才能成功，如果随便使用一个clientId获取到的token将无法链接成功。
				ChatManagerHolder.gChatManager.disconnect(true, true);
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
				Toast.makeText(MainActivity.this, "登录失败：" + code + " " + msg, Toast.LENGTH_SHORT).show();
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
				Gson gson = new Gson();
				DevicesTokenEntity loglinCodeEntity = gson.fromJson(s, DevicesTokenEntity.class);
				String token = loglinCodeEntity.getData().getToken();
				SPUtils.getInstance().put("token", token);
				postCallId(token, android_id, userid);
				Log.d("", "");
			}

			@Override
			public void onError(@NonNull Throwable e) {
//                        CommonUtils.showErrorToast(e.getMessage());
				Log.d("", "");
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
//                        CommonUtils.showErrorToast(msg);
//                        {"code":500,"msg":"服务器内部异常","data":null}
			      }

			      @Override
			      public void onError(@NonNull Throwable e) {
//                        CommonUtils.showErrorToast(e.getMessage());
				      Log.d("", "");
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
	protected void onResume() {
		super.onResume();
		if (!AccessibilityServiceManager.INSTANCE.isOn(this, WakeupService.class.getName())) {
			AccessibilityServiceManager.INSTANCE.gotoSettings(this);
		}
		initUser();
		AMapLocationManager.INSTANCE.getLocation();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	/**
	 * 初始化用户区域
	 */
	private void initUser() {
		if (SPUtils.getInstance().contains(SPConfig.PHONE)) {
			Glide.with(mActivity)
			     .load(R.mipmap.main_unlogin)
			     .apply(RequestOptions.bitmapTransform(new CircleCrop()))
			     .into(mViewBinding.acivMainUserIcon);
			mViewBinding.actvMainUserInfo.setText(CommonUtils.getMarkedPhoneNumber(SPUtils.getInstance().getString(SPConfig.PHONE)));
			mViewBinding.llcMainUser.setOnClickListener(null);
		} else {
			Glide.with(mActivity)
			     .load(R.mipmap.main_unlogin)
			     .apply(RequestOptions.bitmapTransform(new CircleCrop()))
			     .into(mViewBinding.acivMainUserIcon);
			mViewBinding.actvMainUserInfo.setText(R.string.main_click_to_login);
			mViewBinding.llcMainUser.setOnClickListener(view -> ActivityUtils.startActivity(LoginActivity.class));
		}
		mViewBinding.llcMainUser.setVisibility(View.VISIBLE);
	}

	private void initClick() {
		if (BuildConfig.DEBUG) {
			mViewBinding.tcMainTime.setOnClickListener(view -> ActivityUtils.startActivity(PrimitiveFtpdActivity.class));
//			mViewBinding.tcMainTime.setOnClickListener(view -> {
//				if (!MscManager.INSTANCE.isListenHardWakeup() || AIUIManager.INSTANCE.isHardWakeup()) {
//					return;
//				}
//				AIUIManager.INSTANCE.startHardListening();
//			});
		}
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
//        mViewBinding.acivMainIcon8.setOnClickListener(view -> ActivityUtils.startActivity(SettingsActivity.class));
		mViewBinding.acivMainIcon8.setOnClickListener(view -> ActivityUtils.startActivity(MoreAppActivity.class));//跳转到更多应用activity中
	}

	private void initBanner() {
		List<String> bannerList = new ArrayList<>();
		bannerList.add("https://img.dpm.org.cn/Uploads/Picture/2021/04/30/s608b78aaea651.jpg");
		bannerList.add("https://img.dpm.org.cn/Uploads/Picture/2021/04/30/s608b77ae271cb.jpg");
		bannerList.add("https://img.dpm.org.cn/Uploads/Picture/2021/04/30/s608b768f32fd9.jpg");
		bannerList.add("https://img.dpm.org.cn/Uploads/Picture/2021/04/30/s608b75e282420.jpg");
		bannerList.add("https://img.dpm.org.cn/Uploads/Picture/2021/04/30/s608b744114525.jpg");
		bannerList.add("https://img.dpm.org.cn/Uploads/Picture/2021/04/09/s607004e9597f9.jpg");
		bannerList.add("https://img.dpm.org.cn/Uploads/Picture/2021/04/06/s606c1b321897c.jpg");
		Banner<String, BannerImageAdapter<String>> bMainBanner = findViewById(R.id.bMainBanner);
		bMainBanner.setAdapter(new BannerImageAdapter<String>(bannerList) {
			@Override
			public void onBindView(BannerImageHolder holder, String data, int position, int size) {
				Glide.with(holder.itemView).load(data).into(holder.imageView);
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

	@BusUtils.Bus(tag = BusConfig.WEATHER, threadMode = BusUtils.ThreadMode.MAIN)
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

	private void openUsbDevice() {
		//before open usb device
		//should try to get usb permission
		tryGetUsbPermission();
	}

	@SuppressLint("WrongConstant")
	private void tryGetUsbPermission() {
		mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		registerReceiver(mUsbPermissionActionReceiver, filter);

		PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);

		//here do emulation to ask all connected usb device for permission
		for (final UsbDevice usbDevice : mUsbManager.getDeviceList().values()) {
			//add some conditional check if necessary
			//if(isWeCaredUsbDevice(usbDevice)){
			if (mUsbManager.hasPermission(usbDevice)) {
				//if has already got permission, just goto connect it
				//that means: user has choose yes for your previously popup window asking for grant perssion for this usb device
				//and also choose option: not ask again
				afterGetUsbPermission(usbDevice);
			} else {
				//this line will let android popup window, ask user whether to allow this app to have permission to operate this usb device
				mUsbManager.requestPermission(usbDevice, mPermissionIntent);
			}
			//}
		}
	}

	private void afterGetUsbPermission(UsbDevice usbDevice) {
		//call method to set up device communication
		//Toast.makeText(this, String.valueOf("Got permission for usb device: " + usbDevice), Toast.LENGTH_LONG).show();
		//Toast.makeText(this, String.valueOf("Found USB device: VID=" + usbDevice.getVendorId() + " PID=" + usbDevice.getProductId()), Toast.LENGTH_LONG).show();

		doYourOpenUsbDevice(usbDevice);
	}

	private void doYourOpenUsbDevice(UsbDevice usbDevice) {
		//now follow line will NOT show: User has not given permission to device UsbDevice
		UsbDeviceConnection connection = mUsbManager.openDevice(usbDevice);
		//add your operation code here
	}

	private final BroadcastReceiver mUsbPermissionActionReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (ACTION_USB_PERMISSION.equals(action)) {
				synchronized (this) {
					UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
					if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						//user choose YES for your previously popup window asking for grant perssion for this usb device
						if (null != usbDevice) {
							afterGetUsbPermission(usbDevice);
						}
					} else {
						//user choose NO for your previously popup window asking for grant perssion for this usb device
						Toast.makeText(context, "Permission denied for device" + usbDevice, Toast.LENGTH_LONG).show();
					}
				}
			}
		}
	};
}
