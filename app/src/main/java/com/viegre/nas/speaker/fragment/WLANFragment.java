package com.viegre.nas.speaker.fragment;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;

import com.blankj.utilcode.util.BusUtils;
import com.blankj.utilcode.util.ColorUtils;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.Utils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.github.iielse.switchbutton.SwitchView;
import com.lxj.xpopup.XPopup;
import com.thanosfisherman.wifiutils.WifiConnectorBuilder;
import com.thanosfisherman.wifiutils.WifiUtils;
import com.thanosfisherman.wifiutils.wifiConnect.ConnectionErrorCode;
import com.thanosfisherman.wifiutils.wifiConnect.ConnectionSuccessListener;
import com.viegre.nas.speaker.R;
import com.viegre.nas.speaker.adapter.WLANListAdapter;
import com.viegre.nas.speaker.config.BusConfig;
import com.viegre.nas.speaker.databinding.FragmentWlanBinding;
import com.viegre.nas.speaker.fragment.base.BaseFragment;
import com.viegre.nas.speaker.popup.WLANPasswordPopup;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

/**
 * 网络设置
 * Created by Djangoogle on 2020/11/26 11:35 with Android Studio.
 */
public class WLANFragment extends BaseFragment<FragmentWlanBinding> implements NetworkUtils.OnNetworkStatusChangedListener, OnItemClickListener, OnItemChildClickListener {

	public static final String IS_FIRST_RUN = "isFirstRun";

	private WLANListAdapter mWLANListAdapter;
	private ScanResult mScanResult;
	private WifiConnectorBuilder.WifiUtilsBuilder mWifiUtilsBuilder;
	private Animation mWLANLoadingAnimation;

	@Override
	protected void initView() {
		NetworkUtils.registerNetworkStatusChangedListener(this);
		initLoadingAnim();
		getWiFiStatus();
		initAdapter();
		setWLANSwitch();
	}

	@Override
	protected void initData() {}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (null != mWifiUtilsBuilder) {
			mWifiUtilsBuilder.cancelAutoConnect();
		}
		if (NetworkUtils.isRegisteredNetworkStatusChangedListener(this)) {
			NetworkUtils.unregisterNetworkStatusChangedListener(this);
		}
	}

	@Override
	public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
		mScanResult = mWLANListAdapter.getItem(position);
		new XPopup.Builder(getContext()).hasShadowBg(false)
		                                .hasBlurBg(true)
		                                .isDestroyOnDismiss(true)
		                                .dismissOnBackPressed(false)
		                                .dismissOnTouchOutside(false)
		                                .autoOpenSoftInput(false)
		                                .moveUpToKeyboard(false)
		                                .hasStatusBar(false)
		                                .asCustom(new WLANPasswordPopup(mActivity, mScanResult.SSID))
		                                .show();
	}

	@Override
	public void onItemChildClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
		if (R.id.acivItemWLANArrow == view.getId()) {

		}
	}

	public static WLANFragment newInstance(boolean isFirstRun) {
		WLANFragment wlanFragment = new WLANFragment();
		Bundle bundle = new Bundle();
		bundle.putBoolean(IS_FIRST_RUN, isFirstRun);
		wlanFragment.setArguments(bundle);
		return wlanFragment;
	}

	/**
	 * 获取WiFi状态
	 */
	private void getWiFiStatus() {
		if (NetworkUtils.isWifiConnected()) {
			showSelectedWiFi();
			mViewBinding.ilWLANSelected.acivItemWLANStatus.setImageResource(R.mipmap.wifi_connected);
			WifiManager wifiMgr = (WifiManager) Utils.getApp().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
			WifiInfo info = wifiMgr.getConnectionInfo();
			String SSID = null != info ? info.getSSID().replace("\"", "") : "NULL";
			mViewBinding.ilWLANSelected.actvItemWLANName.setText(SSID);
		} else {
			hideSelectedWiFi();
		}
	}

	/**
	 * 初始化适配器
	 */
	private void initAdapter() {
		mWLANListAdapter = new WLANListAdapter(R.layout.item_wlan_list);
		mWLANListAdapter.setOnItemClickListener(this);
		mWLANListAdapter.setOnItemChildClickListener(this);
		mViewBinding.rvWLANList.setLayoutManager(new LinearLayoutManager(mActivity));
		mViewBinding.rvWLANList.addItemDecoration(new HorizontalDividerItemDecoration.Builder(mActivity).color(ColorUtils.getColor(R.color.wlan_dividing_line))
		                                                                                                .size(ConvertUtils.dp2px(0.5F))
		                                                                                                .margin(ConvertUtils.dp2px(20F), ConvertUtils.dp2px(12.5F))
		                                                                                                .build());
		mViewBinding.rvWLANList.setAdapter(mWLANListAdapter);
	}

	/**
	 * 设置WLAN开关
	 */
	private void setWLANSwitch() {
		mViewBinding.svWLANSwitch.setOpened(NetworkUtils.getWifiEnabled());
		mViewBinding.svWLANSwitch.setOnStateChangedListener(new SwitchView.OnStateChangedListener() {
			@Override
			public void toggleToOn(SwitchView view) {
				mViewBinding.svWLANSwitch.setEnabled(false);
				WifiUtils.withContext(Utils.getApp()).enableWifi(isSuccess -> {
					if (isSuccess) {
						view.toggleSwitch(true);
						onWiFiOpen();
					} else {
						view.toggleSwitch(false);
						onWiFiClose();
					}
					mViewBinding.svWLANSwitch.setEnabled(true);
				});
			}

			@Override
			public void toggleToOff(SwitchView view) {
				WifiUtils.withContext(Utils.getApp()).disableWifi();
				view.toggleSwitch(false);
				onWiFiClose();
			}
		});
		if (NetworkUtils.getWifiEnabled()) {
			onWiFiOpen();
		}
	}

	private void onWiFiOpen() {
		mViewBinding.actvWLANNearbyNetwork.setVisibility(View.VISIBLE);
		mViewBinding.rvWLANList.setVisibility(View.VISIBLE);
		scanWiFi();
	}

	private void onWiFiClose() {
		mWLANListAdapter.getData().clear();
		mWLANListAdapter.notifyDataSetChanged();
		mViewBinding.vWLANLine.setVisibility(View.GONE);
		mViewBinding.ilWLANSelected.getRoot().setVisibility(View.GONE);
		mViewBinding.actvWLANNearbyNetwork.setVisibility(View.GONE);
		mViewBinding.rvWLANList.setVisibility(View.GONE);
	}

	private void showSelectedWiFi() {
		mViewBinding.vWLANLine.setVisibility(View.VISIBLE);
		mViewBinding.ilWLANSelected.getRoot().setVisibility(View.VISIBLE);
	}

	private void hideSelectedWiFi() {
		mViewBinding.vWLANLine.setVisibility(View.GONE);
		mViewBinding.ilWLANSelected.getRoot().setVisibility(View.GONE);
	}

	/**
	 * 开始扫描WiFi
	 */
	private void scanWiFi() {
		WifiUtils.withContext(Utils.getApp()).scanWifi(scanResults -> {
			if (!scanResults.isEmpty()) {
				mWLANListAdapter.setList(scanResults);
			}
		}).start();
	}

	/**
	 * 初始化loading动画
	 */
	private void initLoadingAnim() {
		mWLANLoadingAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.wlan_loading_anim);
		LinearInterpolator li = new LinearInterpolator();
		mWLANLoadingAnimation.setInterpolator(li);
	}

	@BusUtils.Bus(tag = BusConfig.BUS_WLAN_PASSWORD, threadMode = BusUtils.ThreadMode.MAIN)
	public void getPassword(String password) {
		mViewBinding.rvWLANList.setEnabled(false);
		showSelectedWiFi();
		mViewBinding.ilWLANSelected.acivItemWLANStatus.setImageResource(R.mipmap.wifi_loading);
		mViewBinding.ilWLANSelected.acivItemWLANStatus.startAnimation(mWLANLoadingAnimation);
		mViewBinding.ilWLANSelected.actvItemWLANName.setText(mScanResult.SSID);
		mWifiUtilsBuilder = WifiUtils.withContext(Utils.getApp());
		mWifiUtilsBuilder.connectWith(mScanResult.SSID, mScanResult.BSSID, password).setTimeout(15 * 1000L).onConnectionResult(new ConnectionSuccessListener() {
			@Override
			public void success() {
				mWifiUtilsBuilder = null;
				mViewBinding.rvWLANList.setEnabled(true);
				mViewBinding.ilWLANSelected.acivItemWLANStatus.clearAnimation();
				mViewBinding.ilWLANSelected.acivItemWLANStatus.setImageResource(R.mipmap.wifi_connected);
			}

			@Override
			public void failed(@NonNull ConnectionErrorCode errorCode) {
				mWifiUtilsBuilder = null;
				mViewBinding.rvWLANList.setEnabled(true);
				hideSelectedWiFi();
			}
		}).start();
	}

	@Override
	public void onDisconnected() {
		getWiFiStatus();
	}

	@Override
	public void onConnected(NetworkUtils.NetworkType networkType) {
		getWiFiStatus();
	}
}
