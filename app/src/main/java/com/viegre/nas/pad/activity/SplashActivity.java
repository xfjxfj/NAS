package com.viegre.nas.pad.activity;

import android.Manifest;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.view.View;

import com.blankj.utilcode.constant.PermissionConstants;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.BusUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.FragmentUtils;
import com.blankj.utilcode.util.ImageUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.PhoneUtils;
import com.blankj.utilcode.util.ServiceUtils;
import com.blankj.utilcode.util.ShellUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.bumptech.glide.Glide;
import com.djangoogle.framework.activity.BaseFragmentActivity;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.utils.GSYVideoType;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.config.BusConfig;
import com.viegre.nas.pad.config.PathConfig;
import com.viegre.nas.pad.config.UrlConfig;
import com.viegre.nas.pad.databinding.ActivitySplashBinding;
import com.viegre.nas.pad.entity.DeviceResourceEntity;
import com.viegre.nas.pad.entity.DeviceResourceRootEntity;
import com.viegre.nas.pad.entity.GuideResourceEntity;
import com.viegre.nas.pad.entity.LoginInfoEntity;
import com.viegre.nas.pad.fragment.settings.network.NetworkDetailFragment;
import com.viegre.nas.pad.fragment.settings.network.NetworkFragment;
import com.viegre.nas.pad.service.ScreenSaverService;
import com.viegre.nas.pad.task.VoidTask;
import com.viegre.nas.pad.util.CommonUtils;
import com.yanzhenjie.kalle.Kalle;
import com.yanzhenjie.kalle.download.Callback;
import com.yanzhenjie.kalle.simple.SimpleCallback;
import com.yanzhenjie.kalle.simple.SimpleResponse;

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
	 * 授予权限、忽略电池优化、创建私有文件夹
	 */
	private void grantPermission() {
		List<String> commandList = new ArrayList<>();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			String space = " ";
			//授予运行时权限
			String grantPermission = "pm grant " + getPackageName() + space;
			if (!PermissionUtils.isGranted(PermissionConstants.CAMERA)) {
				commandList.add(grantPermission + Manifest.permission.CAMERA);
			}
			if (!PermissionUtils.isGranted(PermissionConstants.LOCATION)) {
				commandList.add(grantPermission + Manifest.permission.ACCESS_FINE_LOCATION);
				commandList.add(grantPermission + Manifest.permission.ACCESS_COARSE_LOCATION);
			}
			if (!PermissionUtils.isGranted(PermissionConstants.MICROPHONE)) {
				commandList.add(grantPermission + Manifest.permission.RECORD_AUDIO);
			}
			if (!PermissionUtils.isGranted(PermissionConstants.PHONE)) {
				commandList.add(grantPermission + Manifest.permission.READ_PHONE_STATE);
			}
			if (!PermissionUtils.isGranted(PermissionConstants.STORAGE)) {
				commandList.add(grantPermission + Manifest.permission.READ_EXTERNAL_STORAGE);
				commandList.add(grantPermission + Manifest.permission.WRITE_EXTERNAL_STORAGE);
			}
			//授予悬浮窗权限
			if (!PermissionUtils.isGrantedDrawOverlays()) {
				commandList.add(grantPermission + Manifest.permission.SYSTEM_ALERT_WINDOW);
			}
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
//				FileUtils.createOrExistsDir(PathConfig.IMAGE.PRI);
//				FileUtils.createOrExistsDir(PathConfig.IMAGE.PUB);
//				FileUtils.createOrExistsDir(PathConfig.AUDIO.PRI);
//				FileUtils.createOrExistsDir(PathConfig.AUDIO.PUB);
//				FileUtils.createOrExistsDir(PathConfig.VIDEO.PRI);
//				FileUtils.createOrExistsDir(PathConfig.VIDEO.PUB);
				FileUtils.createOrExistsDir(PathConfig.GUIDE_RESOURCE);
				return null;
			}

			@Override
			public void onSuccess(Void v) {
				super.onSuccess(v);
				ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<Void>() {
					@Override
					public Void doInBackground() {
						List<String> commands = new ArrayList<>();
						commands.add("cd /data/data/com.viegre.nas.pad/files/frp/");
						commands.add("./frpc -c ./frpc.ini > frpc.log  2>&1  &");
						ShellUtils.execCmd(commands, false);
						return null;
					}

					@Override
					public void onSuccess(Void result) {
						SharedPreferences prefs = LoadPrefsUtil.getPrefs(mActivity);
						PrefsBean prefsBean = LoadPrefsUtil.loadPrefs(logger, prefs);
						keyFingerprintProvider.calcPubkeyFingerprints(mActivity);
						ServicesStartStopUtil.startServers(mActivity, prefsBean, keyFingerprintProvider, null);
						getDeviceBoundstatus();
					}
				});
			}
		});
	}

	/**
	 * 判断设备是否绑定用户
	 */
	private void getDeviceBoundstatus() {
		ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<LoginInfoEntity>() {
			@Override
			public LoginInfoEntity doInBackground() {
				return LitePal.findFirst(LoginInfoEntity.class);
			}

			@Override
			public void onSuccess(LoginInfoEntity result) {
				if (null == result) {//未绑定
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
		});
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
								LitePal.saveAll(resourceList);
								DeviceResourceEntity deviceResourceEntity = LitePal.where("type = 'guideVideo'")
								                                                   .findFirst(DeviceResourceEntity.class);
								List<File> guideFileList = FileUtils.listFilesInDir(PathConfig.GUIDE_RESOURCE);
								if (null != deviceResourceEntity) {
									String url = deviceResourceEntity.getContent();
									String fileName = FileUtils.getFileName(url);
									//判断文件是否已下载
									if (!guideFileList.isEmpty() && guideFileList.get(0).getName().equals(fileName)) {
										return null;
									}
									FileUtils.deleteAllInDir(PathConfig.GUIDE_RESOURCE);
									downloadGuideData(new GuideResourceEntity(fileName, url, ImageUtils.isImage(fileName)));
								}
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
//		ActivityUtils.startActivity(MainActivity.class);
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
				mViewBinding.actvSplashGuideSkip.setText(StringUtils.getString(R.string.splash_guide_skip) + l / 1000);
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
		Kalle.Download.get(guideResourceEntity.getUrl())
		              .directory(PathConfig.GUIDE_RESOURCE)
		              .fileName(guideResourceEntity.getFileName())
		              .perform(new Callback() {
			              @Override
			              public void onStart() {}

			              @Override
			              public void onFinish(String path) {
				              ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<Void>() {
					              @Override
					              public Void doInBackground() {
						              LitePal.deleteAll(GuideResourceEntity.class);
						              guideResourceEntity.save();
						              return null;
					              }

					              @Override
					              public void onSuccess(Void result) {}
				              });
			              }

			              @Override
			              public void onException(Exception e) {
				              e.printStackTrace();
				              ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<Void>() {
					              @Override
					              public Void doInBackground() {
						              LitePal.deleteAll(GuideResourceEntity.class);
						              FileUtils.deleteAllInDir(PathConfig.GUIDE_RESOURCE);
						              return null;
					              }

					              @Override
					              public void onSuccess(Void result) {}
				              });
			              }

			              @Override
			              public void onCancel() {
				              ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<Void>() {
					              @Override
					              public Void doInBackground() {
						              LitePal.deleteAll(GuideResourceEntity.class);
						              FileUtils.deleteAllInDir(PathConfig.GUIDE_RESOURCE);
						              return null;
					              }

					              @Override
					              public void onSuccess(Void result) {}
				              });
			              }

			              @Override
			              public void onEnd() {}
		              });
	}
}
