package com.viegre.nas.pad.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.FragmentUtils;
import com.blankj.utilcode.util.ImageUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ServiceUtils;
import com.blankj.utilcode.util.ShellUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.Utils;
import com.bumptech.glide.Glide;
import com.djangoogle.framework.activity.BaseFragmentActivity;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.utils.GSYVideoType;
import com.viegre.nas.pad.BuildConfig;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.config.BusConfig;
import com.viegre.nas.pad.config.PathConfig;
import com.viegre.nas.pad.config.SPConfig;
import com.viegre.nas.pad.config.UrlConfig;
import com.viegre.nas.pad.databinding.ActivitySplashBinding;
import com.viegre.nas.pad.entity.DeviceResourceEntity;
import com.viegre.nas.pad.entity.DeviceResourceRootEntity;
import com.viegre.nas.pad.entity.GuideResourceEntity;
import com.viegre.nas.pad.fragment.settings.network.NetworkDetailFragment;
import com.viegre.nas.pad.fragment.settings.network.NetworkFragment;
import com.viegre.nas.pad.service.MQTTService;
import com.viegre.nas.pad.service.ScreenSaverService;
import com.viegre.nas.pad.task.VoidTask;
import com.viegre.nas.pad.util.CommonUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.litepal.LitePal;
import org.primftpd.prefs.LoadPrefsUtil;
import org.primftpd.prefs.PrefsBean;
import org.primftpd.util.KeyFingerprintProvider;
import org.primftpd.util.ServicesStartStopUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import rxhttp.RxHttp;

/**
 * 启动页
 * Created by レインマン on 2020/11/19 10:26 with Android Studio.
 */
public class SplashActivity extends BaseFragmentActivity<ActivitySplashBinding> {

	private NetworkFragment mNetworkFragment;
	private NetworkDetailFragment mNetworkDetailFragment;
	private CountDownTimer mGuideSkipCountDownTimer;
	private PrefsBean prefsBean;
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final KeyFingerprintProvider keyFingerprintProvider = new KeyFingerprintProvider();

	@Override
	protected void initialize() {
		ServiceUtils.startService(ScreenSaverService.class);
		ServiceUtils.startService(MQTTService.class);
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
	}

	/**
	 * 开启无障碍服务、授予权限、忽略电池优化、创建私有文件夹
	 */
	private void grantPermission() {
		if ("official".equals(BuildConfig.FLAVOR)) {
			//开启无障碍服务
			Settings.Secure.putString(getContentResolver(),
			                          Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
			                          getPackageName() + "/com.viegre.nas.pad.service.WakeupService");
			Settings.Secure.putInt(getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED, 1);
		}
		List<String> commandList = new ArrayList<>();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			//授予修改系统设置权限
			if (!PermissionUtils.isGrantedWriteSettings()) {
				commandList.add("appops set " + getPackageName() + " WRITE_SETTINGS allow");
			}
			//忽略电池优化
			PowerManager powerManager = (PowerManager) getApplicationContext().getSystemService(POWER_SERVICE);
			if (!powerManager.isIgnoringBatteryOptimizations(getPackageName())) {
				String ignoreBatteryOptimization = "dumpsys deviceidle whitelist +" + getPackageName();
				commandList.add(ignoreBatteryOptimization);
			}
		}
		ThreadUtils.executeByCached(new VoidTask() {
			@Override
			public Void doInBackground() {
				if (!commandList.isEmpty()) {
					ShellUtils.CommandResult commandResult = ShellUtils.execCmd(commandList, true);
					LogUtils.iTag("ShellUtils", commandResult.toString());
				}
				//创建文件夹
				FileUtils.createOrExistsDir(PathConfig.GUIDE_RESOURCE);
				FileUtils.createOrExistsDir(PathConfig.RECYCLE_BIN);
				List<String> commands = new ArrayList<>();
				commands.add("cd /data/data/com.viegre.nas.pad/files/frp/");
				commands.add("./frpc -c ./frpc.ini > frpc.log  2>&1  &");
				ShellUtils.execCmd(commands, false);
				return null;
			}

			@Override
			public void onSuccess(Void v) {
				super.onSuccess(v);
				SharedPreferences prefs = LoadPrefsUtil.getPrefs(mActivity);
				PrefsBean prefsBean = LoadPrefsUtil.loadPrefs(logger, prefs);
				keyFingerprintProvider.calcPubkeyFingerprints(mActivity);
				ServicesStartStopUtil.startServers(mActivity, prefsBean, keyFingerprintProvider, null);
//				ActivityUtils.startActivity(MainActivity.class);
//				ActivityUtils.startActivity(WelcomeActivity.class);
				getDeviceBoundstatus();
			}
		});
	}

	/**
	 * 判断设备是否绑定用户
	 */
	private void getDeviceBoundstatus() {
		ConnectivityManager connectivityManager = (ConnectivityManager) Utils.getApp().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkEthernetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
		NetworkInfo networkWiFiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		boolean isEthernetConnected = null != networkEthernetInfo && networkEthernetInfo.isConnected();
		boolean isWiFiConnected = null != networkWiFiInfo && networkWiFiInfo.isConnected();
		if (!SPUtils.getInstance().getBoolean(SPConfig.IS_BOUND, false)) {//未绑定
			//判断网络是否可用
			if (!isEthernetConnected && !isWiFiConnected) {//配置网络
				mNetworkFragment = NetworkFragment.newInstance(true);
				mNetworkDetailFragment = NetworkDetailFragment.newInstance();
				FragmentUtils.add(getSupportFragmentManager(), mNetworkFragment, R.id.flSplash);
				FragmentUtils.show(mNetworkFragment);
			} else {//引导用户注册
//				ActivityUtils.startActivity(WelcomeActivity.class);
				ActivityUtils.startActivity(MainActivity.class);
			}
		} else {//已绑定
			//判断网络是否可用
			if (!isEthernetConnected && !isWiFiConnected) {
				showGuideData();
			} else {//获取并显示最新引导页
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
		//获取并显示最新引导页
		getDeviceResource();
	}

	/**
	 * 获取资源配置
	 */
	private void getDeviceResource() {
		RxHttp.postForm(UrlConfig.Device.GET_RESOURCE)
		      .setAssemblyEnabled(false)
		      .add("sn", SPUtils.getInstance().getString(SPConfig.ANDROID_ID))
		      .asResponse(DeviceResourceRootEntity.class)
		      .subscribe(new Observer<DeviceResourceRootEntity>() {
			      @Override
			      public void onSubscribe(@NonNull Disposable d) {}

			      @Override
			      public void onNext(@NonNull DeviceResourceRootEntity deviceResourceRootEntity) {
				      List<DeviceResourceEntity> resourceList = deviceResourceRootEntity.getResourceList();
				      if (!resourceList.isEmpty()) {
					      LitePal.deleteAll(DeviceResourceEntity.class);
					      LitePal.saveAll(resourceList);
					      DeviceResourceEntity deviceResourceEntity = LitePal.where("type = ?", "guideVideo")
					                                                         .findFirst(DeviceResourceEntity.class);
					      List<File> guideFileList = FileUtils.listFilesInDir(PathConfig.GUIDE_RESOURCE);
					      if (null != deviceResourceEntity) {
						      String url = deviceResourceEntity.getContent();
						      String fileName = FileUtils.getFileName(url);
						      //判断文件是否已下载
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
			      public void onComplete() {}
		      });
	}

	/**
	 * 显示引导页
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
						mViewBinding.nvpSplashGuideVideo.setVisibility(View.VISIBLE);
						playGuideVideo(fileName);
					}
				}
			}
		});
	}

	/**
	 * 播放引导视频
	 *
	 * @param path
	 */
	private void playGuideVideo(String path) {
		//全屏拉伸显示，使用这个属性时，surface_container建议使用FrameLayout
		GSYVideoType.setShowType(GSYVideoType.SCREEN_MATCH_FULL);
		mViewBinding.nvpSplashGuideVideo.setUp(path, true, "");
		mViewBinding.nvpSplashGuideVideo.setIsTouchWiget(false);
		ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<Long>() {
			@Override
			public Long doInBackground() {
				return CommonUtils.getLocalVideoDuration(path);
			}

			@Override
			public void onSuccess(Long result) {
				mViewBinding.nvpSplashGuideVideo.startPlayLogic();
				startCountdown(result);
			}
		});
	}

	/**
	 * 开始倒计时
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
	 * 下载引导资源
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
			      public void onSubscribe(@NonNull Disposable d) {}

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
			      public void onComplete() {}
		      });
	}
}
