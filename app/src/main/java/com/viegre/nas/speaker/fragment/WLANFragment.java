package com.viegre.nas.speaker.fragment;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;

import com.alibaba.fastjson.JSON;
import com.blankj.utilcode.util.BusUtils;
import com.blankj.utilcode.util.ColorUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ThreadUtils;
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
import com.viegre.nas.speaker.config.SPConfig;
import com.viegre.nas.speaker.databinding.FragmentWlanBinding;
import com.viegre.nas.speaker.entity.WiFiEntity;
import com.viegre.nas.speaker.fragment.base.BaseFragment;
import com.viegre.nas.speaker.popup.WiFiConnectionFailedPopup;
import com.viegre.nas.speaker.popup.WiFiPasswordPopup;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

/**
 * 网络设置
 * Created by Djangoogle on 2020/11/26 11:35 with Android Studio.
 */
public class WLANFragment extends BaseFragment<FragmentWlanBinding> implements NetworkUtils.OnNetworkStatusChangedListener, OnItemClickListener, OnItemChildClickListener {

	public static final String IS_FIRST_RUN = "isFirstRun";

	private WLANListAdapter mWLANListAdapter;
	private WiFiEntity mWiFiEntity;
	private WifiConnectorBuilder.WifiUtilsBuilder mWifiUtilsBuilder;
	private Animation mWLANLoadingAnimation;
	private boolean mIsConnecting = false;
	private final List<WiFiEntity> mSavedWiFiList = new ArrayList<>();

	@Override
	protected void initView() {
		NetworkUtils.registerNetworkStatusChangedListener(this);
		initLoadingAnim();
		getWiFiStatus();
		initAdapter();
		setWLANSwitch();
		mViewBinding.ilWLANSelected.acivItemWLANArrow.setOnClickListener(view -> {
			if (mIsConnecting) {
				return;
			}
			BusUtils.post(BusConfig.BUS_NETWORK_DETAIL, BusConfig.BUS_SHOW_NETWORK_DETAIL);
		});
		mViewBinding.acivWLANRefresh.setOnClickListener(view -> {
			mViewBinding.acivWLANRefresh.setClickable(false);
			scanWiFi();
		});
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
		if (mIsConnecting) {
			return;
		}
		mWiFiEntity = mWLANListAdapter.getItem(position);
		if (TextUtils.isEmpty(mWiFiEntity.getPassword())) {
			new XPopup.Builder(getContext()).hasShadowBg(false)//是否有半透明的背景，默认为true
			                                .hasBlurBg(true)//是否有高斯模糊的背景，默认为false
			                                .dismissOnBackPressed(false)//按返回键是否关闭弹窗，默认为true
			                                .dismissOnTouchOutside(false)//点击外部是否关闭弹窗，默认为true
			                                .hasStatusBar(false)//是否显示状态栏，默认显示
			                                .hasNavigationBar(false)//是否显示导航栏，默认显示
			                                .asCustom(new WiFiPasswordPopup(mActivity, mWiFiEntity.getScanResult().SSID))//设置自定义弹窗
			                                .show();
		} else {
			getPassword(mWiFiEntity.getPassword());
		}
	}

	@Override
	public void onItemChildClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
		if (R.id.acivItemWLANArrow == view.getId()) {

		}
	}

	@Override
	public void onResume() {
		super.onResume();
		initSavedWiFiList();
		if (null != mWLANListAdapter && !mWLANListAdapter.getData().isEmpty()) {
			updateWiFiList(mWLANListAdapter.getData());
			mWLANListAdapter.notifyDataSetChanged();
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
			String SSID;
			if (null != info) {
				SSID = info.getSSID().replace("\"", "");
				SPUtils.getInstance().put(SPConfig.SP_CURRENT_WIFI_SSID, SSID);
			} else {
				SSID = info.getBSSID();
			}
			mViewBinding.ilWLANSelected.actvItemWLANName.setText(SSID);
		} else {
			if (!mIsConnecting) {
				hideSelectedWiFi();
			}
		}
	}

	/**
	 * 初始化适配器
	 */
	private void initAdapter() {
		mWLANListAdapter = new WLANListAdapter();
		mWLANListAdapter.setOnItemClickListener(this);
		mWLANListAdapter.setOnItemChildClickListener(this);
		mViewBinding.rvWLANOtherNetworkList.setLayoutManager(new LinearLayoutManager(mActivity));
		mViewBinding.rvWLANOtherNetworkList.addItemDecoration(new HorizontalDividerItemDecoration.Builder(mActivity).color(ColorUtils.getColor(R.color.wlan_dividing_line))
		                                                                                                            .size(1)
		                                                                                                            .margin(40, 25)
		                                                                                                            .build());
		mViewBinding.rvWLANOtherNetworkList.setAdapter(mWLANListAdapter);
	}

	/**
	 * 设置WLAN开关
	 */
	private void setWLANSwitch() {
		mViewBinding.svWLANSwitch.setOpened(NetworkUtils.getWifiEnabled());
		mViewBinding.svWLANSwitch.setOnStateChangedListener(new SwitchView.OnStateChangedListener() {
			@Override
			public void toggleToOn(SwitchView view) {
				mViewBinding.svWLANSwitch.setClickable(false);
				WifiUtils.withContext(Utils.getApp()).enableWifi(isSuccess -> {
					if (isSuccess) {
						view.toggleSwitch(true);
						onWiFiOpen();
					} else {
						view.toggleSwitch(false);
						onWiFiClose();
					}
					mViewBinding.svWLANSwitch.setClickable(true);
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

	/**
	 * WiFi开启时的操作
	 */
	private void onWiFiOpen() {
		mViewBinding.actvWLANOtherNetwork.setVisibility(View.VISIBLE);
		mViewBinding.acivWLANRefresh.setVisibility(View.VISIBLE);
		mViewBinding.rvWLANOtherNetworkList.setVisibility(View.VISIBLE);
		scanWiFi();
	}

	/**
	 * WiFi关闭时的操作
	 */
	private void onWiFiClose() {
		mWLANListAdapter.getData().clear();
		mWLANListAdapter.notifyDataSetChanged();
		hideSelectedWiFi();
		mViewBinding.actvWLANOtherNetwork.setVisibility(View.GONE);
		mViewBinding.acivWLANRefresh.setVisibility(View.GONE);
		mViewBinding.rvWLANOtherNetworkList.setVisibility(View.GONE);
	}

	/**
	 * 显示选择的WiFi
	 */
	private void showSelectedWiFi() {
		mViewBinding.vWLANLine.setVisibility(View.VISIBLE);
		mViewBinding.ilWLANSelected.clItemWLANRoot.setVisibility(View.VISIBLE);
	}

	/**
	 * 隐藏选中的WiFi
	 */
	private void hideSelectedWiFi() {
		mViewBinding.vWLANLine.setVisibility(View.GONE);
		mViewBinding.ilWLANSelected.clItemWLANRoot.setVisibility(View.GONE);
	}

	/**
	 * 开始扫描WiFi
	 */
	private void scanWiFi() {
		WifiUtils.withContext(Utils.getApp()).scanWifi(scanResults -> ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<List<WiFiEntity>>() {
			@Override
			public List<WiFiEntity> doInBackground() {
				List<WiFiEntity> wifiList = new ArrayList<>();
				for (ScanResult scanResult : scanResults) {
					wifiList.add(new WiFiEntity(scanResult, ""));
				}
				return updateWiFiList(wifiList);
			}

			@Override
			public void onSuccess(List<WiFiEntity> result) {
				if (!result.isEmpty()) {
					mWLANListAdapter.setList(result);
				}
				mViewBinding.acivWLANRefresh.setClickable(true);
			}
		})).start();
	}

	/**
	 * 更新WiFi列表
	 *
	 * @param wifiList
	 * @return
	 */
	private List<WiFiEntity> updateWiFiList(List<WiFiEntity> wifiList) {
		List<WiFiEntity> processWiFiList = new ArrayList<>(wifiList);
		for (WiFiEntity scanResult : processWiFiList) {
			for (WiFiEntity wifiEntity : mSavedWiFiList) {
				if (scanResult.getScanResult().SSID.equals(wifiEntity.getScanResult().SSID) && scanResult.getScanResult().BSSID.equals(wifiEntity.getScanResult().BSSID)) {
					scanResult.setPassword(wifiEntity.getPassword());
				}
			}
		}
		return processWiFiList;
	}

	/**
	 * 初始化loading动画
	 */
	private void initLoadingAnim() {
		mWLANLoadingAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.wlan_loading_anim);
		LinearInterpolator li = new LinearInterpolator();
		mWLANLoadingAnimation.setInterpolator(li);
	}

	/**
	 * 获取密码消息
	 *
	 * @param password
	 */
	@BusUtils.Bus(tag = BusConfig.BUS_WLAN_PASSWORD, threadMode = BusUtils.ThreadMode.MAIN)
	public void getPassword(String password) {
		mIsConnecting = true;
		showSelectedWiFi();
		mViewBinding.ilWLANSelected.acivItemWLANStatus.setImageResource(R.mipmap.wifi_loading);
		mViewBinding.ilWLANSelected.acivItemWLANStatus.startAnimation(mWLANLoadingAnimation);
		mViewBinding.ilWLANSelected.actvItemWLANName.setText(mWiFiEntity.getScanResult().SSID);
		mWifiUtilsBuilder = WifiUtils.withContext(Utils.getApp());
		mWifiUtilsBuilder.connectWith(mWiFiEntity.getScanResult().SSID, mWiFiEntity.getScanResult().BSSID, password)
		                 .setTimeout(15 * 1000L)
		                 .onConnectionResult(new ConnectionSuccessListener() {
			                 @Override
			                 public void success() {
				                 mIsConnecting = false;
				                 mWifiUtilsBuilder = null;
				                 saveConnectedWiFi(new WiFiEntity(mWiFiEntity.getScanResult(), password));
				                 mViewBinding.ilWLANSelected.acivItemWLANStatus.clearAnimation();
				                 mViewBinding.ilWLANSelected.acivItemWLANStatus.setImageResource(R.mipmap.wifi_connected);
				                 mWLANListAdapter.notifyDataSetChanged();
				                 SPUtils.getInstance().put(SPConfig.SP_CURRENT_WIFI_SSID, mWiFiEntity.getScanResult().SSID);
			                 }

			                 @Override
			                 public void failed(@NonNull ConnectionErrorCode errorCode) {
				                 mIsConnecting = false;
				                 mWifiUtilsBuilder = null;
				                 hideSelectedWiFi();
				                 mWLANListAdapter.notifyDataSetChanged();
				                 SPUtils.getInstance().remove(SPConfig.SP_CURRENT_WIFI_SSID);
				                 new XPopup.Builder(getContext()).hasShadowBg(false)//是否有半透明的背景，默认为true
				                                                 .hasBlurBg(true)//是否有高斯模糊的背景，默认为false
				                                                 .dismissOnBackPressed(false)//按返回键是否关闭弹窗，默认为true
				                                                 .dismissOnTouchOutside(false)//点击外部是否关闭弹窗，默认为true
				                                                 .hasStatusBar(false)//是否显示状态栏，默认显示
				                                                 .hasNavigationBar(false)//是否显示导航栏，默认显示
				                                                 .asCustom(new WiFiConnectionFailedPopup(mActivity))//设置自定义弹窗
				                                                 .show();
			                 }
		                 })
		                 .start();
		mWLANListAdapter.notifyDataSetChanged();
	}

	@Override
	public void onDisconnected() {
		getWiFiStatus();
	}

	@Override
	public void onConnected(NetworkUtils.NetworkType networkType) {
		getWiFiStatus();
	}

	/**
	 * 初始化已保存WiFi
	 */
	private void initSavedWiFiList() {
		mSavedWiFiList.clear();
		List<WiFiEntity> wifiList = JSON.parseArray(SPUtils.getInstance().getString(SPConfig.SP_SAVED_WIFI, ""), WiFiEntity.class);
		if (null != wifiList && !wifiList.isEmpty()) {
			mSavedWiFiList.addAll(wifiList);
		}
	}

	/**
	 * 保存已连接WiFi
	 *
	 * @param wifiEntity
	 */
	private void saveConnectedWiFi(WiFiEntity wifiEntity) {
		if (mSavedWiFiList.isEmpty()) {
			mSavedWiFiList.add(wifiEntity);
		} else {//更新密码
			int index = -1;
			boolean hasWiFi = false;
			for (int i = 0; i < mSavedWiFiList.size(); i++) {
				if (mSavedWiFiList.get(i).getScanResult().SSID.equals(wifiEntity.getScanResult().SSID) && mSavedWiFiList.get(i)
				                                                                                                        .getScanResult().BSSID.equals(wifiEntity.getScanResult().BSSID)) {
					if (!mSavedWiFiList.get(i).getPassword().equals(wifiEntity.getPassword())) {
						index = i;
					}
					hasWiFi = true;
				}
			}
			if (index > -1) {
				mSavedWiFiList.set(index, wifiEntity);
			} else {
				if (!hasWiFi) {
					mSavedWiFiList.add(wifiEntity);
				}
			}
		}
		SPUtils.getInstance().put(SPConfig.SP_SAVED_WIFI, JSON.toJSONString(mSavedWiFiList));
		if (!mWLANListAdapter.getData().isEmpty()) {
			updateWiFiList(mWLANListAdapter.getData());
		}
	}
}
