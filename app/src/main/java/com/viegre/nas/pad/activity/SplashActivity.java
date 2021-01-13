package com.viegre.nas.pad.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;

import com.blankj.utilcode.constant.PermissionConstants;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.BusUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.FragmentUtils;
import com.blankj.utilcode.util.ImageUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.PathUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.PhoneUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.bumptech.glide.Glide;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.activity.base.BaseFragmentActivity;
import com.viegre.nas.pad.config.BusConfig;
import com.viegre.nas.pad.config.SPConfig;
import com.viegre.nas.pad.config.UrlConfig;
import com.viegre.nas.pad.databinding.ActivitySplashBinding;
import com.viegre.nas.pad.entity.DeviceResourceEntity;
import com.viegre.nas.pad.entity.DeviceResourceRootEntity;
import com.viegre.nas.pad.entity.GuideResourceEntity;
import com.viegre.nas.pad.fragment.settings.NetworkDetailFragment;
import com.viegre.nas.pad.fragment.settings.NetworkFragment;
import com.viegre.nas.pad.util.CommonUtils;
import com.yanzhenjie.kalle.Kalle;
import com.yanzhenjie.kalle.download.Callback;
import com.yanzhenjie.kalle.simple.SimpleCallback;
import com.yanzhenjie.kalle.simple.SimpleResponse;

import org.litepal.LitePal;

import java.io.File;
import java.util.List;

/**
 * 启动页
 * Created by レインマン on 2020/11/19 10:26 with Android Studio.
 */
public class SplashActivity extends BaseFragmentActivity<ActivitySplashBinding> {

	private static final long DEFAULT_GUIDE_TIME = 5 * 1000L;

	private NetworkFragment mNetworkFragment;
	private NetworkDetailFragment mNetworkDetailFragment;

	@Override
	protected void initView() {
		requestPermission();
	}

	@Override
	protected void initData() {}

	@Override
	protected void onResume() {
		super.onResume();
		requestDrawOverlays();
		GSYVideoManager.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		GSYVideoManager.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		GSYVideoManager.releaseAllVideos();
		FragmentUtils.removeAll(getSupportFragmentManager());
	}

	/**
	 * 请求运行时权限
	 */
	private void requestPermission() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			PermissionUtils.permissionGroup(PermissionConstants.CAMERA, PermissionConstants.LOCATION, PermissionConstants.MICROPHONE,
			                                PermissionConstants.PHONE,
			                                PermissionConstants.STORAGE).callback((isAllGranted, granted, deniedForever, denied) -> {
				if (!isAllGranted) {
					requestPermission();
				} else {
					getDeviceBoundstatus();
				}
			}).request();
		} else {
			getDeviceBoundstatus();
		}
	}

	/**
	 * 申请悬浮窗权限
	 */
	private void requestDrawOverlays() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !PermissionUtils.isGrantedDrawOverlays()) {
			PermissionUtils.requestDrawOverlays(new PermissionUtils.SimpleCallback() {
				@Override
				public void onGranted() {
					ignoreBatteryOptimization();
				}

				@Override
				public void onDenied() {
					requestDrawOverlays();
				}
			});
		}
	}

	/**
	 * 忽略电池优化
	 */
	private void ignoreBatteryOptimization() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			PowerManager powerManager = (PowerManager) getApplicationContext().getSystemService(POWER_SERVICE);
			if (!powerManager.isIgnoringBatteryOptimizations(getPackageName())) {
				startActivity(new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).setData(Uri.parse("package:" + getPackageName())));
			}
		}
	}

	/**
	 * 判断设备是否绑定用户
	 */
	private void getDeviceBoundstatus() {
		if (!SPUtils.getInstance().getBoolean(SPConfig.IS_DEVICE_BOUND, false)) {//未绑定
			//判断网络是否可用
			NetworkUtils.isAvailableAsync(aBoolean -> {
				if (!aBoolean) {//配置网络
					mNetworkFragment = NetworkFragment.newInstance(true);
					mNetworkDetailFragment = NetworkDetailFragment.newInstance();
					FragmentUtils.add(getSupportFragmentManager(), mNetworkFragment, R.id.flSplash);
					FragmentUtils.show(mNetworkFragment);
				} else {//引导用户注册
					registerGuide();
				}
			});
		} else {//已绑定
			//判断网络是否可用
			NetworkUtils.isAvailableAsync(aBoolean -> {
				if (!aBoolean) {//读取并显示缓存引导页
					showGuideData();
				} else {//获取并显示最新引导页
					getDeviceResource();
				}
			});
		}
	}

	@BusUtils.Bus(tag = BusConfig.NETWORK_DETAIL, threadMode = BusUtils.ThreadMode.MAIN)
	public void networkDetailOperation(String operation) {
		switch (operation) {
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

	/**
	 * 获取资源配置
	 */
	private void getDeviceResource() {
		Kalle.post(UrlConfig.Device.GET_RESOURCE).param("sn", PhoneUtils.getSerial()).perform(new SimpleCallback<DeviceResourceRootEntity>() {
			@Override
			public void onResponse(SimpleResponse<DeviceResourceRootEntity, String> response) {
				if (response.isSucceed()) {
					List<DeviceResourceEntity> resourceList = response.succeed().getResourceList();
					if (!resourceList.isEmpty()) {
						ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<Void>() {
							@Override
							public Void doInBackground() {
								for (DeviceResourceEntity deviceResourceEntity : resourceList) {
									if ("guideVideo".equals(deviceResourceEntity.getType())) {
										String url = deviceResourceEntity.getContent();
										String filename = FileUtils.getFileName(url);
										downloadGuideData(new GuideResourceEntity(filename, url, ImageUtils.isImage(filename)));
										break;
									}
								}
								LitePal.saveAll(resourceList);
								return null;
							}

							@Override
							public void onSuccess(Void result) {
								showGuideData();
							}
						});
					}
				}
			}
		});
	}

	/**
	 * 引导用户注册
	 */
	private void registerGuide() {
		//显示引导页
		showGuideData();
	}

	/**
	 * 显示引导页
	 */
	private void showGuideData() {
		ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<GuideResourceEntity>() {
			@Override
			public GuideResourceEntity doInBackground() {
				List<GuideResourceEntity> guideResourceEntityList = LitePal.findAll(GuideResourceEntity.class);
				if (guideResourceEntityList.isEmpty()) {
					return null;
				} else {
					return guideResourceEntityList.get(0);
				}
			}

			@Override
			public void onSuccess(GuideResourceEntity result) {
				mViewBinding.actvSplashGuideSkip.setOnClickListener(view -> {
					ActivityUtils.startActivity(MainActivity.class);
					finish();
				});
				if (null == result) {
					mViewBinding.acivSplashGuideImage.setVisibility(View.VISIBLE);
					startCountdown(DEFAULT_GUIDE_TIME);
				} else {
					if (result.isImage()) {
						Glide.with(mActivity)
						     .load(PathUtils.getExternalStoragePath() + File.separator + result.getFilename())
						     .into(mViewBinding.acivSplashGuideImage);
						mViewBinding.acivSplashGuideImage.setVisibility(View.VISIBLE);
						startCountdown(DEFAULT_GUIDE_TIME);
					} else {
						mViewBinding.sgvpSplashGuideVideo.setVisibility(View.VISIBLE);
						playGuideVideo(PathUtils.getExternalStoragePath() + File.separator + result.getFilename());
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
		mViewBinding.sgvpSplashGuideVideo.setUp(path, true, "");
		mViewBinding.sgvpSplashGuideVideo.getBackButton().setVisibility(View.GONE);
		mViewBinding.sgvpSplashGuideVideo.setIsTouchWiget(false);
		mViewBinding.sgvpSplashGuideVideo.startPlayLogic();
		startCountdown(CommonUtils.getVideoDuration(path));
	}

	/**
	 * 开始倒计时
	 *
	 * @param duration
	 */
	private void startCountdown(long duration) {
		mViewBinding.actvSplashGuideSkip.setVisibility(View.VISIBLE);
		CountDownTimer countDownTimer = new CountDownTimer(duration, 1000L) {
			@Override
			public void onTick(long l) {
				mViewBinding.actvSplashGuideSkip.setText(StringUtils.getString(R.string.splash_guide_skip) + l / 1000);
			}

			@Override
			public void onFinish() {
				ActivityUtils.startActivity(MainActivity.class);
				finish();
			}
		};
		countDownTimer.start();
	}

	/**
	 * 下载引导资源
	 *
	 * @param guideResourceEntity
	 */
	private void downloadGuideData(GuideResourceEntity guideResourceEntity) {
		Kalle.Download.get(SPUtils.getInstance().getString(guideResourceEntity.getUrl(), ""))
		              .directory(PathUtils.getExternalStoragePath())
		              .fileName(guideResourceEntity.getFilename())
		              .perform(new Callback() {
			              @Override
			              public void onStart() {}

			              @Override
			              public void onFinish(String path) {
				              LitePal.deleteAll(GuideResourceEntity.class);
				              guideResourceEntity.save();
			              }

			              @Override
			              public void onException(Exception e) {
				              e.printStackTrace();
				              LitePal.deleteAll(GuideResourceEntity.class);
			              }

			              @Override
			              public void onCancel() {
				              LitePal.deleteAll(GuideResourceEntity.class);
			              }

			              @Override
			              public void onEnd() {}
		              });
	}
}
