package com.viegre.nas.pad.activity;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.FragmentUtils;
import com.blankj.utilcode.util.ImageUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ShellUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.Utils;
import com.bumptech.glide.Glide;
import com.djangoogle.framework.activity.BaseFragmentActivity;
import com.google.gson.Gson;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.utils.GSYVideoType;
import com.viegre.nas.pad.BuildConfig;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.config.BusConfig;
import com.viegre.nas.pad.config.PathConfig;
import com.viegre.nas.pad.config.SPConfig;
import com.viegre.nas.pad.config.UrlConfig;
import com.viegre.nas.pad.databinding.ActivitySplashBinding;
import com.viegre.nas.pad.entity.DataBeanXX;
import com.viegre.nas.pad.entity.DeviceResourceEntity;
import com.viegre.nas.pad.entity.DeviceResourceRootEntity;
import com.viegre.nas.pad.entity.DevicesFollowEntity;
import com.viegre.nas.pad.entity.DevicesFriendList;
import com.viegre.nas.pad.entity.DevicesTokenEntity;
import com.viegre.nas.pad.entity.GuideResourceEntity;
import com.viegre.nas.pad.entity.MyfriendDataFriend;
import com.viegre.nas.pad.fragment.settings.network.NetworkDetailFragment;
import com.viegre.nas.pad.fragment.settings.network.NetworkFragment;
import com.viegre.nas.pad.service.MQTTService;
import com.viegre.nas.pad.task.VoidTask;
import com.viegre.nas.pad.util.CommonUtils;
import com.viegre.nas.pad.util.SntpClient;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.litepal.LitePal;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import rxhttp.RxHttp;

/**
 * ?????????
 * Created by ??????????????? on 2020/11/19 10:26 with Android Studio.
 */
public class SplashActivity extends BaseFragmentActivity<ActivitySplashBinding> {

	private NetworkFragment mNetworkFragment;
	private NetworkDetailFragment mNetworkDetailFragment;
	private CountDownTimer mGuideSkipCountDownTimer;
	private boolean isEthernetConnected;
	private boolean isWiFiConnected;
	// MQTT??????
	ServiceConnection conn = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			MQTTService myService = ((MQTTService.DownLoadBinder) service).getService();
			myService.setWelcomeserver(new MQTTService.welcomebind() {
				@Override
				public Void onWelcomeBind(String bindStr) {
					getDeviceResource();
					return null;
				}
			});
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {

		}
	};

	@Override
	protected void initialize() {
		bindService(new Intent(this, MQTTService.class), conn, Context.BIND_AUTO_CREATE);

		grantPermission();
	}

	@Override
	protected void onResume() {
		super.onResume();
		GSYVideoManager.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		GSYVideoManager.onPause();
	}

	@Override
	protected void onDestroy() {
		FragmentUtils.removeAll(getSupportFragmentManager());
		GSYVideoManager.releaseAllVideos();
		super.onDestroy();
		unbindService(conn);//????????????
	}

	/**
	 * ?????????????????????????????????????????????????????????????????????????????????
	 */
	private void grantPermission() {
		ThreadUtils.executeByCached(new VoidTask() {
			@SuppressLint("MissingPermission")
			@Override
			public Void doInBackground() {
				//???????????????
				FileUtils.createOrExistsDir(PathConfig.GUIDE_RESOURCE);
				FileUtils.createOrExistsDir(PathConfig.RECYCLE_BIN);
				FileUtils.createOrExistsDir(PathConfig.UPLOAD_CACHE);
				//??????MQTT??????
				startService(new Intent(mActivity, MQTTService.class));
//				ServiceUtils.startService(MQTTService.class);
				//??????????????????
				if ("official".equals(BuildConfig.FLAVOR)) {
					//?????????????????????
					Settings.Secure.putString(getContentResolver(),
					                          Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
					                          getPackageName() + "/com.viegre.nas.pad.service.WakeupService");
					Settings.Secure.putInt(getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED, 1);
					//??????24?????????
					if (24 != Settings.System.getInt(Utils.getApp().getContentResolver(), Settings.System.TIME_12_24, 12)) {
						Settings.System.putInt(Utils.getApp().getContentResolver(), Settings.System.TIME_12_24, 24);
					}
					if (SPUtils.getInstance().getBoolean(SPConfig.TIME_SYNC, true)) {
						SntpClient sntpClient = new SntpClient();
						Date date = null;
						if (sntpClient.requestTime("ntp3.aliyun.com", 10 * 1000)) {
							long now = sntpClient.getNtpTime() + SystemClock.elapsedRealtime() - sntpClient.getNtpTimeReference();
							date = new Date(now);
						}
						if (null != date) {
							String time = TimeUtils.date2String(date, new SimpleDateFormat("MMddHHmmyyyy.ss", Locale.getDefault()));
							ShellUtils.execCmd("date " + time, true);
						}
					}
//                    //??????????????????????????????
//                    if (1 == Settings.Global.getInt(Utils.getApp().getContentResolver(), Settings.Global.AUTO_TIME_ZONE, 1)) {
//                        Settings.Global.putInt(Utils.getApp().getContentResolver(), Settings.Global.AUTO_TIME_ZONE, 0);
//                    }
//                    //????????????????????????
//                    AlarmManager alarmManager = (AlarmManager) Utils.getApp().getSystemService(Context.ALARM_SERVICE);
//                    alarmManager.setTimeZone("Asia/Shanghai");
				}
				//??????command
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					List<String> commandList = new ArrayList<>();
					//??????????????????
					PowerManager powerManager = (PowerManager) getApplicationContext().getSystemService(POWER_SERVICE);
					if (!powerManager.isIgnoringBatteryOptimizations(getPackageName())) {
						String ignoreBatteryOptimization = "dumpsys deviceidle whitelist +" + getPackageName();
						commandList.add(ignoreBatteryOptimization);
					}
					//??????frp????????????
					commandList.add("cd /data/data/com.viegre.nas.pad/files/frp/");
					commandList.add("./frpc -c ./frpc.ini > frpc.log  2>&1  &");
					ShellUtils.CommandResult commandResult = ShellUtils.execCmd(commandList, true);
					LogUtils.iTag("ShellUtils", commandResult.toString());
				}
				return null;
			}

			@Override
			protected void onDone() {
				super.onDone();
				ThreadUtils.runOnUiThread(() -> {
//                    ActivityUtils.startActivity(MainActivity.class);
//                    finish();
//					getDeviceBoundstatus();
					isBindDevices();
				});
			}
		});
	}

	/**
	 * xfj ??????????????????????????????
	 */
	private void isBindDevices() {
		ConnectivityManager connectivityManager = (ConnectivityManager) Utils.getApp().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkEthernetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
		NetworkInfo networkWiFiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		isEthernetConnected = null != networkEthernetInfo && networkEthernetInfo.isConnected();
		isWiFiConnected = null != networkWiFiInfo && networkWiFiInfo.isConnected();
		MainActivity.mDevicesFriend.clear();//????????????
		getDevicesToken(SPUtils.getInstance().getString(SPConfig.ANDROID_ID));
//		if (!SPUtils.getInstance().getBoolean(SPConfig.IS_BOUND, false)) {//?????????
//			//????????????????????????
//			if (!isEthernetConnected && !isWiFiConnected) {//????????????
//				mNetworkFragment = NetworkFragment.newInstance(true);
//				mNetworkDetailFragment = NetworkDetailFragment.newInstance();
//				FragmentUtils.add(getSupportFragmentManager(), mNetworkFragment, R.id.flSplash);
//				FragmentUtils.show(mNetworkFragment);
//			} else {//??????????????????
//				ActivityUtils.startActivity(WelcomeActivity.class);
//			}
//		} else {//?????????
//			//????????????????????????
//			if (!isEthernetConnected && !isWiFiConnected) {
//				showGuideData();
//			} else {//??????????????????????????????
//				getDeviceResource();
//			}
//		}
	}

	/**
	 * ??????????????????????????????
	 */
	private void getDeviceBoundstatus() {
		ConnectivityManager connectivityManager = (ConnectivityManager) Utils.getApp().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkEthernetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
		NetworkInfo networkWiFiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		boolean isEthernetConnected = null != networkEthernetInfo && networkEthernetInfo.isConnected();
		boolean isWiFiConnected = null != networkWiFiInfo && networkWiFiInfo.isConnected();
		if (!SPUtils.getInstance().getBoolean(SPConfig.IS_BOUND, false)) {//?????????
			//????????????????????????
			if (!isEthernetConnected && !isWiFiConnected) {//????????????
				mNetworkFragment = NetworkFragment.newInstance(true);
				mNetworkDetailFragment = NetworkDetailFragment.newInstance();
				FragmentUtils.add(getSupportFragmentManager(), mNetworkFragment, R.id.flSplash);
				FragmentUtils.show(mNetworkFragment);
			} else {//??????????????????
				ActivityUtils.startActivity(WelcomeActivity.class);
//				ActivityUtils.startActivity(MainActivity.class);
			}
		} else {//?????????
			//????????????????????????
			if (!isEthernetConnected && !isWiFiConnected) {
				showGuideData();
			} else {//??????????????????????????????
				getDeviceResource();
			}
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void networkDetailOperation(String[] events) {
		if (!BusConfig.NETWORK_DETAIL.equals(events[0])) {
			return;
		}
		switch (events[1]) {
			case BusConfig.SHOW_NETWORK_DETAIL:
				FragmentUtils.add(getSupportFragmentManager(), mNetworkDetailFragment, R.id.flSplash);
				FragmentUtils.show(mNetworkDetailFragment);
				break;

			case BusConfig.HIDE_NETWORK_DETAIL:
				FragmentUtils.remove(mNetworkDetailFragment);
				break;

			default:
				break;
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
	public void deviceBound(String event) {
		if (!BusConfig.DEVICE_BOUND.equals(event)) {
			return;
		}
		SPUtils.getInstance().put(SPConfig.IS_BOUND, true);
		//??????????????????????????????
		getDeviceResource();
	}

	/**
	 * ??????????????????
	 */
	private void getDeviceResource() {
		RxHttp.postForm(UrlConfig.Device.GET_RESOURCE)
		      .setAssemblyEnabled(false)
		      .add("sn", SPUtils.getInstance().getString(SPConfig.ANDROID_ID))
		      .asResponse(DeviceResourceRootEntity.class)
		      .subscribe(new Observer<DeviceResourceRootEntity>() {
			      @Override
			      public void onSubscribe(@NonNull Disposable d) {

			      }

			      @Override
			      public void onNext(@NonNull DeviceResourceRootEntity deviceResourceRootEntity) {
				      List<DeviceResourceEntity> resourceList = deviceResourceRootEntity.getResourceList();
				      if (!resourceList.isEmpty()) {
					      LitePal.deleteAll(DeviceResourceEntity.class);
					      LitePal.saveAll(resourceList);
					      DeviceResourceEntity deviceResourceEntity = LitePal.where("type = ?", "guideVideo").findFirst(DeviceResourceEntity.class);
					      List<File> guideFileList = FileUtils.listFilesInDir(PathConfig.GUIDE_RESOURCE);
					      if (null != deviceResourceEntity) {
						      String url = deviceResourceEntity.getContent();
						      String fileName = FileUtils.getFileName(url);
						      //???????????????????????????
						      if (!guideFileList.isEmpty() && guideFileList.get(0).getName().equals(fileName)) {
							      showGuideData();
							      return;
						      }
						      downloadGuideData(new GuideResourceEntity(fileName, url, ImageUtils.isImage(fileName)));
					      }
				      }
				      showGuideData();
			      }

			      @Override
			      public void onError(@NonNull Throwable e) {
				      e.printStackTrace();
				      showGuideData();
			      }

			      @Override
			      public void onComplete() {
			      }
		      });
	}

	/**
	 * ???????????????
	 */
	private void showGuideData() {
		ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<GuideResourceEntity>() {
			@Override
			public GuideResourceEntity doInBackground() {
				return LitePal.findFirst(GuideResourceEntity.class);
			}

			@Override
			public void onSuccess(GuideResourceEntity result) {
				mViewBinding.actvSplashGuideSkip.setOnClickListener(view -> {
					mGuideSkipCountDownTimer.cancel();
					ActivityUtils.startActivity(MainActivity.class);
					finish();
				});
				if (null == result) {
					mViewBinding.acivSplashGuideImage.setVisibility(View.VISIBLE);
					startCountdown(CommonUtils.DEFAULT_SPLASH_GUIDE_DURATION);
				} else {
					String fileName = PathConfig.GUIDE_RESOURCE + result.getFileName();
					if (result.isImage()) {
						Glide.with(mActivity).load(fileName).into(mViewBinding.acivSplashGuideImage);
						mViewBinding.acivSplashGuideImage.setVisibility(View.VISIBLE);
						startCountdown(CommonUtils.DEFAULT_SPLASH_GUIDE_DURATION);
					} else {
						mViewBinding.svpSplashGuideVideo.setVisibility(View.VISIBLE);
						playGuideVideo(fileName);
					}
				}
			}
		});
	}

	/**
	 * ??????????????????
	 *
	 * @param path
	 */
	private void playGuideVideo(String path) {
		//?????????????????????????????????????????????surface_container????????????FrameLayout
		GSYVideoType.setShowType(GSYVideoType.SCREEN_MATCH_FULL);
		mViewBinding.svpSplashGuideVideo.setUp(path, true, "");
		mViewBinding.svpSplashGuideVideo.setIsTouchWiget(false);
		ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<Long>() {
			@Override
			public Long doInBackground() {
				return CommonUtils.getLocalVideoDuration(path);
			}

			@Override
			public void onSuccess(Long result) {
				mViewBinding.svpSplashGuideVideo.startPlayLogic();
				startCountdown(result);
			}
		});
	}

	/**
	 * ???????????????
	 *
	 * @param duration
	 */
	private void startCountdown(long duration) {
		mViewBinding.actvSplashGuideSkip.setVisibility(View.VISIBLE);
		mGuideSkipCountDownTimer = new CountDownTimer(duration, 1000L) {
			@Override
			public void onTick(long l) {
				mViewBinding.actvSplashGuideSkip.setText(StringUtils.getString(R.string.splash_guide_skip, l / 1000));
			}

			@Override
			public void onFinish() {
				ActivityUtils.startActivity(MainActivity.class);
				finish();
			}
		};
		mGuideSkipCountDownTimer.start();
	}

	/**
	 * ??????????????????
	 *
	 * @param guideResourceEntity
	 */
	private void downloadGuideData(GuideResourceEntity guideResourceEntity) {
		FileUtils.deleteAllInDir(PathConfig.GUIDE_RESOURCE);
		RxHttp.get(guideResourceEntity.getUrl())
		      .setAssemblyEnabled(false)
		      .asDownload(PathConfig.GUIDE_RESOURCE + guideResourceEntity.getFileName())
		      .subscribe(new Observer<String>() {
			      @Override
			      public void onSubscribe(@NonNull Disposable d) {
			      }

			      @Override
			      public void onNext(@NonNull String s) {
				      LitePal.deleteAll(GuideResourceEntity.class);
				      guideResourceEntity.save();
			      }

			      @Override
			      public void onError(@NonNull Throwable e) {
				      e.printStackTrace();
				      LitePal.deleteAll(GuideResourceEntity.class);
				      FileUtils.deleteAllInDir(PathConfig.GUIDE_RESOURCE);
			      }

			      @Override
			      public void onComplete() {
			      }
		      });
	}

	private void getDevicesToken(String android_id) {
		String url = UrlConfig.Device.GET_DEVICESTOKEN;
		RxHttp.postForm(url).add("sn", android_id).asString().observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<String>() {
			@Override
			public void onSubscribe(@NonNull Disposable d) {
				Log.d("", "");
			}

			@Override
			public void onNext(@NonNull String s) {
				Gson gson = new Gson();
				DevicesTokenEntity loglinCodeEntity = gson.fromJson(s, DevicesTokenEntity.class);
				if (loglinCodeEntity.getMsg().equals("OK")) {
					String token = loglinCodeEntity.getData().getToken();
					SPUtils.getInstance().put(SPConfig.DEVICES_TOKEN, token);
					Log.d(CommonUtils.getFileName() + CommonUtils.getLineNumber() + "-----------", token);
//                    getContactsDatas(token, android_id);
				} else {
					Log.e("?????????ID ", android_id + ", " + loglinCodeEntity.getMsg() + "!");
					CommonUtils.showErrorToast(loglinCodeEntity.getMsg());
//					TipDialog.show(SplashActivity.this, loglinCodeEntity.getMsg(), TipDialog.TYPE.ERROR).doDismiss();
				}
				getDevicesfriend();
			}

			@Override
			public void onError(@NonNull Throwable e) {
				CommonUtils.showErrorToast(e.getMessage());
//				TipDialog.show(WelcomeActivity.this, "????????????", TipDialog.TYPE.ERROR).doDismiss();
			}

			@Override
			public void onComplete() {
				Log.d("", "");
//                        SPUtils.getInstance().remove(SPConfig.LOGIN_CODE_SESSION_ID);
////                        mViewBinding.actvLoginAccountBtn.setClickable(true);
			}
		});
	}

	private void getDevicesfriend() {
		RxHttp.get(UrlConfig.Device.GET_GETFRIENDS)
		      .addHeader(SPConfig.TOKEN, SPUtils.getInstance().getString(SPConfig.DEVICES_TOKEN))
		      .add("pageNum", new Integer(0))
		      .add("pageSize", new Integer(100))
		      .add("sn", SPUtils.getInstance().getString(SPConfig.ANDROID_ID))
		      .asString()
		      .observeOn(AndroidSchedulers.mainThread())
		      .subscribe(new Observer<String>() {
			      @Override
			      public void onSubscribe(@NonNull Disposable d) {
				      Log.d("onSubscribe", d.toString());
			      }

			      @Override
			      public void onNext(@NonNull String s) {
				      Gson gson = new Gson();
				      DevicesFriendList DevicesFriendList = gson.fromJson(s, DevicesFriendList.class);
				      if (DevicesFriendList.getMsg().equals("OK")) {//??????????????????
					      List<com.viegre.nas.pad.entity.DevicesFriendList.FriendsBean> friends = DevicesFriendList.getData().getFriends();
					      for (int i = 0; i < friends.size(); i++) {
						      MainActivity.mDevicesFriend.add(new MyfriendDataFriend(friends.get(i).getCallId(),
						                                                             friends.get(i).getName(),
						                                                             "",
						                                                             friends.get(i).getSn(),
						                                                             SPConfig.NASPAD));
					      }
				      } else {
					      CommonUtils.showErrorToast(DevicesFriendList.getMsg());
				      }
				      getContactsDatas();
			      }

			      @Override
			      public void onError(@NonNull Throwable e) {
				      CommonUtils.showErrorToast(e.getMessage());
			      }

			      @Override
			      public void onComplete() {
				      Log.d("onSubscribe", "1231456");
			      }
		      });
	}

	private void getContactsDatas() {
		RxHttp.postForm(UrlConfig.Device.GET_GETALLFOLLOWS)
		      .addHeader(SPConfig.TOKEN, SPUtils.getInstance().getString(SPConfig.DEVICES_TOKEN))
		      .add("sn", SPUtils.getInstance().getString(SPConfig.ANDROID_ID))
		      .asString()
		      .observeOn(AndroidSchedulers.mainThread())
		      .subscribe(new Observer<String>() {
			      @Override
			      public void onSubscribe(@NonNull Disposable d) {
				      Log.d("onSubscribe", d.toString());
			      }

			      @Override
			      public void onNext(@NonNull String s) {
				      Gson gson = new Gson();
				      DevicesFollowEntity devicesFollowEntity = gson.fromJson(s, DevicesFollowEntity.class);
				      if (devicesFollowEntity.getMsg().equals("OK")) {//??????????????????
					      List<DataBeanXX> data = devicesFollowEntity.getData();
					      if (null != data) {
						      if (data.size() == 0) {//0??????????????? ?????????????????????????????????????????? ????????????main
							      ActivityUtils.startActivity(WelcomeActivity.class);
						      } else {
							      for (DataBeanXX datum : data) {
								      String nickName = String.valueOf(datum.getNickName());
								      String phone = datum.getPhone();
								      String picdata = String.valueOf(datum.getPicData());
								      String userid = datum.getCallId();
								      MainActivity.mDevicesFriend.add(new MyfriendDataFriend(userid, nickName, picdata, phone, SPConfig.PHONE));
							      }
							      //????????????????????????
							      if (!isEthernetConnected && !isWiFiConnected) {
								      showGuideData();
							      } else {//??????????????????????????????
								      getDeviceResource();
							      }
						      }
					      } else {
						      CommonUtils.showErrorToast(devicesFollowEntity.getMsg());
					      }
				      } else {
					      CommonUtils.showErrorToast(devicesFollowEntity.getMsg());
				      }
			      }

			      @Override
			      public void onError(@NonNull Throwable e) {
				      CommonUtils.showErrorToast(e.getMessage());
			      }

			      @Override
			      public void onComplete() {
				      Log.d("", "");
			      }
		      });
	}
}
