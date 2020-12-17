package com.viegre.nas.speaker.activity;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.BusUtils;
import com.blankj.utilcode.util.FragmentUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.SPUtils;
import com.viegre.nas.speaker.R;
import com.viegre.nas.speaker.activity.base.BaseFragmentActivity;
import com.viegre.nas.speaker.config.BusConfig;
import com.viegre.nas.speaker.config.SPConfig;
import com.viegre.nas.speaker.databinding.ActivitySplashBinding;
import com.viegre.nas.speaker.fragment.NetworkDetailFragment;
import com.viegre.nas.speaker.fragment.NetworkFragment;

/**
 * 启动页
 * Created by Djangoogle on 2020/11/19 10:26 with Android Studio.
 */
public class SplashActivity extends BaseFragmentActivity<ActivitySplashBinding> {

	private NetworkFragment mNetworkFragment;
	private NetworkDetailFragment mNetworkDetailFragment;

	@Override
	protected void initView() {
		//判断是否为开机启动
//		getBootStatus();
		mNetworkFragment = NetworkFragment.newInstance(true);
		mNetworkDetailFragment = NetworkDetailFragment.newInstance();
//		FragmentUtils.add(getSupportFragmentManager(), mNetworkFragment, R.id.flSplash);
//		FragmentUtils.show(mNetworkFragment);
		ActivityUtils.startActivity(MainActivity.class);
	}

	@Override
	protected void initData() {

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
