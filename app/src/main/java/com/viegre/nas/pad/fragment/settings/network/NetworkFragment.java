package com.viegre.nas.pad.fragment.settings.network;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;

import com.alibaba.fastjson.JSON;
import com.blankj.utilcode.util.BusUtils;
import com.blankj.utilcode.util.ColorUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.Utils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.djangoogle.framework.fragment.BaseFragment;
import com.github.iielse.switchbutton.SwitchView;
import com.thanosfisherman.wifiutils.WifiConnectorBuilder;
import com.thanosfisherman.wifiutils.WifiUtils;
import com.thanosfisherman.wifiutils.wifiConnect.ConnectionErrorCode;
import com.thanosfisherman.wifiutils.wifiConnect.ConnectionSuccessListener;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.adapter.NetworkListAdapter;
import com.viegre.nas.pad.config.BusConfig;
import com.viegre.nas.pad.config.SPConfig;
import com.viegre.nas.pad.databinding.FragmentNetworkBinding;
import com.viegre.nas.pad.entity.WiFiEntity;
import com.viegre.nas.pad.manager.PopupManager;
import com.viegre.nas.pad.popup.NetworkConnectionFailedPopup;
import com.viegre.nas.pad.popup.NetworkPasswordPopup;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

/**
 * 网络设置
 * Created by レインマン on 2020/11/26 11:35 with Android Studio.
 */
public class NetworkFragment extends BaseFragment<FragmentNetworkBinding> implements NetworkUtils.OnNetworkStatusChangedListener, OnItemClickListener, OnItemChildClickListener {

	public static final String IS_FIRST_RUN = "isFirstRun";

	private NetworkListAdapter mNetworkListAdapter;
	private WiFiEntity mWiFiEntity;
	private WifiConnectorBuilder.WifiUtilsBuilder mWifiUtilsBuilder;
	private Animation mNetworkLoadingAnimation;
	private boolean mIsConnecting = false;
	private final List<WiFiEntity> mSavedWiFiList = new ArrayList<>();

	@Override
	protected void initialize() {
		mViewBinding.actvNetworkTitle.setText(R.string.network_settings);
		NetworkUtils.registerNetworkStatusChangedListener(this);
		initLoadingAnim();
		getWiFiStatus();
		initAdapter();
		setNetworkSwitch();
		mViewBinding.ilNetworkSelected.acivItemNetworkArrow.setOnClickListener(view -> {
			if (mIsConnecting) {
				return;
			}
			BusUtils.post(BusConfig.NETWORK_DETAIL, BusConfig.SHOW_NETWORK_DETAIL);
		});
		mViewBinding.acivNetworkRefresh.setOnClickListener(view -> {
			mViewBinding.acivNetworkRefresh.setClickable(false);
			scanWiFi();
		});
	}

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
		mWiFiEntity = mNetworkListAdapter.getItem(position);
		if (StringUtils.isEmpty(mWiFiEntity.getPassword())) {
			PopupManager.INSTANCE.showCustomXPopup(mContext,
			                                       new NetworkPasswordPopup(mContext, mWiFiEntity.getScanResult().SSID));
		} else {
			getPassword(mWiFiEntity.getPassword());
		}
	}

	@Override
	public void onItemChildClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
		if (R.id.acivItemNetworkArrow == view.getId()) {

		}
	}

	@Override
	public void onResume() {
		super.onResume();
		initSavedWiFiList();
		if (null != mNetworkListAdapter && !mNetworkListAdapter.getData().isEmpty()) {
			updateWiFiList(mNetworkListAdapter.getData());
			mNetworkListAdapter.notifyDataSetChanged();
		}
	}

	public static NetworkFragment newInstance(boolean isFirstRun) {
		NetworkFragment networkFragment = new NetworkFragment();
		Bundle bundle = new Bundle();
		bundle.putBoolean(IS_FIRST_RUN, isFirstRun);
		networkFragment.setArguments(bundle);
		return networkFragment;
	}

	/**
	 * 获取WiFi状态
	 */
	private void getWiFiStatus() {
		if (NetworkUtils.isWifiConnected()) {
			showSelectedWiFi();
			mViewBinding.ilNetworkSelected.acivItemNetworkStatus.setImageResource(R.mipmap.network_wifi_connected);
			WifiManager wifiMgr = (WifiManager) Utils.getApp().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
			WifiInfo info = wifiMgr.getConnectionInfo();
			String SSID;
			if (null != info) {
				SSID = info.getSSID().replace("\"", "");
				SPUtils.getInstance().put(SPConfig.CURRENT_WIFI_SSID, SSID);
			} else {
				SSID = info.getBSSID();
			}
			mViewBinding.ilNetworkSelected.actvItemNetworkName.setText(SSID);
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
		mNetworkListAdapter = new NetworkListAdapter();
		mNetworkListAdapter.setOnItemClickListener(this);
		mNetworkListAdapter.setOnItemChildClickListener(this);
		mViewBinding.rvNetworkOtherNetworkList.setLayoutManager(new LinearLayoutManager(mActivity));
		mViewBinding.rvNetworkOtherNetworkList.addItemDecoration(new HorizontalDividerItemDecoration.Builder(mActivity).color(ColorUtils.getColor(R.color.divider_line))
		                                                                                                               .size(1)
		                                                                                                               .margin(40, 25)
		                                                                                                               .build());
		mViewBinding.rvNetworkOtherNetworkList.setAdapter(mNetworkListAdapter);
	}

	/**
	 * 设置网络开关
	 */
	private void setNetworkSwitch() {
		mViewBinding.svNetworkSwitch.setOpened(NetworkUtils.getWifiEnabled());
		mViewBinding.svNetworkSwitch.setOnStateChangedListener(new SwitchView.OnStateChangedListener() {
			@Override
			public void toggleToOn(SwitchView view) {
				mViewBinding.svNetworkSwitch.setClickable(false);
				WifiUtils.withContext(Utils.getApp()).enableWifi(isSuccess -> {
					if (isSuccess) {
						view.toggleSwitch(true);
						onWiFiOpen();
					} else {
						view.toggleSwitch(false);
						onWiFiClose();
					}
					mViewBinding.svNetworkSwitch.setClickable(true);
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
		mViewBinding.actvNetworkOtherNetwork.setVisibility(View.VISIBLE);
		mViewBinding.acivNetworkRefresh.setVisibility(View.VISIBLE);
		mViewBinding.rvNetworkOtherNetworkList.setVisibility(View.VISIBLE);
		scanWiFi();
	}

	/**
	 * WiFi关闭时的操作
	 */
	private void onWiFiClose() {
		mNetworkListAdapter.getData().clear();
		mNetworkListAdapter.notifyDataSetChanged();
		hideSelectedWiFi();
		mViewBinding.actvNetworkOtherNetwork.setVisibility(View.GONE);
		mViewBinding.acivNetworkRefresh.setVisibility(View.GONE);
		mViewBinding.rvNetworkOtherNetworkList.setVisibility(View.GONE);
	}

	/**
	 * 显示选择的WiFi
	 */
	private void showSelectedWiFi() {
		mViewBinding.vNetworkLine.setVisibility(View.VISIBLE);
		mViewBinding.ilNetworkSelected.clItemNetworkRoot.setVisibility(View.VISIBLE);
	}

	/**
	 * 隐藏选中的WiFi
	 */
	private void hideSelectedWiFi() {
		mViewBinding.vNetworkLine.setVisibility(View.GONE);
		mViewBinding.ilNetworkSelected.clItemNetworkRoot.setVisibility(View.GONE);
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
					mNetworkListAdapter.setList(result);
				}
				mViewBinding.acivNetworkRefresh.setClickable(true);
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
		mNetworkLoadingAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.network_loading_anim);
		LinearInterpolator li = new LinearInterpolator();
		mNetworkLoadingAnimation.setInterpolator(li);
	}

	/**
	 * 获取密码消息
	 *
	 * @param password
	 */
	@BusUtils.Bus(tag = BusConfig.NETWORK_PASSWORD, threadMode = BusUtils.ThreadMode.MAIN)
	public void getPassword(String password) {
		mIsConnecting = true;
		showSelectedWiFi();
		mViewBinding.ilNetworkSelected.acivItemNetworkStatus.setImageResource(R.mipmap.network_wifi_loading);
		mViewBinding.ilNetworkSelected.acivItemNetworkStatus.startAnimation(mNetworkLoadingAnimation);
		mViewBinding.ilNetworkSelected.actvItemNetworkName.setText(mWiFiEntity.getScanResult().SSID);
		mWifiUtilsBuilder = WifiUtils.withContext(Utils.getApp());
		mWifiUtilsBuilder.connectWith(mWiFiEntity.getScanResult().SSID, mWiFiEntity.getScanResult().BSSID, password)
		                 .setTimeout(15 * 1000L)
		                 .onConnectionResult(new ConnectionSuccessListener() {
			                 @Override
			                 public void success() {
				                 mIsConnecting = false;
				                 mWifiUtilsBuilder = null;
				                 saveConnectedWiFi(new WiFiEntity(mWiFiEntity.getScanResult(), password));
				                 mViewBinding.ilNetworkSelected.acivItemNetworkStatus.clearAnimation();
				                 mViewBinding.ilNetworkSelected.acivItemNetworkStatus.setImageResource(R.mipmap.network_wifi_connected);
				                 mNetworkListAdapter.notifyDataSetChanged();
				                 SPUtils.getInstance().put(SPConfig.CURRENT_WIFI_SSID, mWiFiEntity.getScanResult().SSID);
			                 }

			                 @Override
			                 public void failed(@NonNull ConnectionErrorCode errorCode) {
				                 mIsConnecting = false;
				                 mWifiUtilsBuilder = null;
				                 hideSelectedWiFi();
				                 mNetworkListAdapter.notifyDataSetChanged();
				                 SPUtils.getInstance().remove(SPConfig.CURRENT_WIFI_SSID);
				                 PopupManager.INSTANCE.showCustomXPopup(mContext, new NetworkConnectionFailedPopup(mContext));
			                 }
		                 })
		                 .start();
		mNetworkListAdapter.notifyDataSetChanged();
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
		List<WiFiEntity> wifiList = JSON.parseArray(SPUtils.getInstance().getString(SPConfig.SAVED_WIFI, ""), WiFiEntity.class);
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
				                                                                                                        .getScanResult().BSSID.equals(
								wifiEntity.getScanResult().BSSID)) {
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
		SPUtils.getInstance().put(SPConfig.SAVED_WIFI, JSON.toJSONString(mSavedWiFiList));
		if (!mNetworkListAdapter.getData().isEmpty()) {
			updateWiFiList(mNetworkListAdapter.getData());
		}
	}
}
