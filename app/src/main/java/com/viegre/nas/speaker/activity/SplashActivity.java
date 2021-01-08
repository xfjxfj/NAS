package com.viegre.nas.speaker.activity;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.BusUtils;
import com.blankj.utilcode.util.FragmentUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.StringUtils;
import com.viegre.nas.speaker.R;
import com.viegre.nas.speaker.activity.base.BaseFragmentActivity;
import com.viegre.nas.speaker.config.BusConfig;
import com.viegre.nas.speaker.config.SPConfig;
import com.viegre.nas.speaker.databinding.ActivitySplashBinding;
import com.viegre.nas.speaker.fragment.settings.NetworkDetailFragment;
import com.viegre.nas.speaker.fragment.settings.NetworkFragment;

/**
 * 启动页
 * Created by レインマン on 2020/11/19 10:26 with Android Studio.
 */
public class SplashActivity extends BaseFragmentActivity<ActivitySplashBinding> {

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
		ignoreBatteryOptimization();
	}

	/**
	 * 判断是否为开机启动
	 */
	private void getBootStatus() {
		if (SPUtils.getInstance().getBoolean(SPConfig.SP_IS_BOOT, false)) {//开机启动
			//判断网络是否可用
			NetworkUtils.isAvailableAsync(aBoolean -> {
				if (!aBoolean) {//打开网络设置
					mNetworkFragment = NetworkFragment.newInstance(true);
					FragmentUtils.add(getSupportFragmentManager(), mNetworkFragment, R.id.flSplash);
					FragmentUtils.show(mNetworkFragment);
				} else {//判断是否为出厂首次开机
					getDeviceInitializedStatus();
				}
			});
		} else {//非开机启动
			//判断是否为出厂首次开机
			getDeviceInitializedStatus();
		}
	}

	/**
	 * 判断是否为出厂首次开机
	 */
	private void getDeviceInitializedStatus() {
		if (!SPUtils.getInstance().getBoolean(SPConfig.SP_IS_DEVICE_INITIALIZED, false)) {//未初始化

		} else {//已初始化

		}
	}

	/**
	 * 请求运行时权限
	 */
	private void requestPermission() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			PermissionUtils.permission(Manifest.permission.CAMERA,
			                           Manifest.permission.ACCESS_FINE_LOCATION,
			                           Manifest.permission.ACCESS_COARSE_LOCATION,
			                           Manifest.permission.RECORD_AUDIO,
			                           Manifest.permission.READ_PHONE_STATE,
			                           Manifest.permission.READ_EXTERNAL_STORAGE,
			                           Manifest.permission.WRITE_EXTERNAL_STORAGE).callback(new PermissionUtils.SimpleCallback() {
				@Override
				public void onGranted() {
					requestDrawOverlays();
				}

				@Override
				public void onDenied() {
					requestPermission();
				}
			}).request();
		} else {
			initViews();
		}
	}

	/**
	 * 申请悬浮窗权限
	 */
	private void requestDrawOverlays() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (PermissionUtils.isGrantedDrawOverlays()) {
				initViews();
			} else {
				PermissionUtils.requestDrawOverlays(new PermissionUtils.SimpleCallback() {
					@Override
					public void onGranted() {
						initViews();
					}

					@Override
					public void onDenied() {
						requestDrawOverlays();
					}
				});
			}
		}
	}

	/**
	 * 忽略电池优化
	 */
	private void ignoreBatteryOptimization() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			try {
				PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
				if (!powerManager.isIgnoringBatteryOptimizations(getPackageName())) {
					Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
					intent.setData(Uri.parse("package:" + getPackageName()));
					startActivity(intent);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void initViews() {
		//判断是否为开机启动
//		getBootStatus();
		mNetworkFragment = NetworkFragment.newInstance(true);
		mNetworkDetailFragment = NetworkDetailFragment.newInstance();
		//判断是否登录
		if (StringUtils.isEmpty(SPUtils.getInstance().getString(SPConfig.SP_TOKEN, ""))) {
			ActivityUtils.startActivity(LoginActivity.class);
		}
	}

	@BusUtils.Bus(tag = BusConfig.BUS_NETWORK_DETAIL, threadMode = BusUtils.ThreadMode.MAIN)
	public void networkDetailOperation(String operation) {
		switch (operation) {
			case BusConfig.BUS_SHOW_NETWORK_DETAIL:
				FragmentUtils.add(getSupportFragmentManager(), mNetworkDetailFragment, R.id.flSplash);
				FragmentUtils.show(mNetworkDetailFragment);
				break;

			case BusConfig.BUS_HIDE_NETWORK_DETAIL:
				FragmentUtils.remove(mNetworkDetailFragment);
				break;

			default:
				break;
		}
	}
}
