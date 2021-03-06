package com.viegre.nas.pad.fragment.settings.network;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;

import com.alibaba.fastjson.JSON;
import com.blankj.utilcode.util.ColorUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.Utils;
import com.djangoogle.framework.fragment.BaseFragment;
import com.thanosfisherman.wifiutils.WifiUtils;
import com.thanosfisherman.wifiutils.wifiRemove.RemoveErrorCode;
import com.thanosfisherman.wifiutils.wifiRemove.RemoveSuccessListener;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.adapter.NetworkDetailListAdapter;
import com.viegre.nas.pad.config.BusConfig;
import com.viegre.nas.pad.config.SPConfig;
import com.viegre.nas.pad.databinding.FragmentNetworkDetailBinding;
import com.viegre.nas.pad.entity.WiFiEntity;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

/**
 * 网络详情
 * Created by レインマン on 2020/12/03 15:49 with Android Studio.
 */
public class NetworkDetailFragment extends BaseFragment<FragmentNetworkDetailBinding> {

	@Override
	protected void initialize() {
		mViewBinding.llcNetworkDetailBack.setOnClickListener(view -> EventBus.getDefault()
		                                                                     .post(new String[]{BusConfig.NETWORK_DETAIL, BusConfig.HIDE_NETWORK_DETAIL}));
		String SSID = SPUtils.getInstance().getString(SPConfig.CURRENT_WIFI_SSID, "");
		mViewBinding.actvNetworkDetailSSID.setText(SSID);
		mViewBinding.actvNetworkDetailIgnore.setOnClickListener(view -> WifiUtils.withContext(mActivity).remove(SSID, new RemoveSuccessListener() {
			@Override
			public void success() {
				removeWiFiFromSPBySSID(SSID);
				EventBus.getDefault().post(new String[]{BusConfig.NETWORK_DETAIL, BusConfig.HIDE_NETWORK_DETAIL});
			}

			@Override
			public void failed(@NonNull RemoveErrorCode errorCode) {
				ToastUtils.showShort(R.string.network_ignore_network_failed);
			}
		}));
		initList();
	}

	public static NetworkDetailFragment newInstance() {
		return new NetworkDetailFragment();
	}

	private void initList() {
		ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<List<String[]>>() {
			@Override
			public List<String[]> doInBackground() {
				List<String[]> networkInfoList = new ArrayList<>();
				networkInfoList.add(new String[]{StringUtils.getString(R.string.network_status), "已连接"});
				networkInfoList.add(new String[]{StringUtils.getString(R.string.network_ip_address), NetworkUtils.getIpAddressByWifi()});
				networkInfoList.add(new String[]{StringUtils.getString(R.string.network_subnet_mask), NetworkUtils.getNetMaskByWifi()});
				networkInfoList.add(new String[]{StringUtils.getString(R.string.network_gateway), NetworkUtils.getGatewayByWifi()});
				WifiManager wifiManager = (WifiManager) Utils.getApp().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
				DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
				networkInfoList.add(new String[]{StringUtils.getString(R.string.network_dns), dhcpInfo2String(dhcpInfo.dns1)});
				return networkInfoList;
			}

			@Override
			public void onSuccess(List<String[]> result) {
				mViewBinding.rvNetworkDetail.setLayoutManager(new LinearLayoutManager(mActivity));
				mViewBinding.rvNetworkDetail.addItemDecoration(new HorizontalDividerItemDecoration.Builder(mActivity).color(ColorUtils.getColor(R.color.divider_line))
				                                                                                                     .size(1)
				                                                                                                     .margin(25)
				                                                                                                     .build());
				mViewBinding.rvNetworkDetail.setAdapter(new NetworkDetailListAdapter(result));
			}
		});
	}

	/**
	 * DHCP信息转字符串
	 *
	 * @param dhcpInfo
	 * @return
	 */
	private String dhcpInfo2String(int dhcpInfo) {
		return (dhcpInfo & 0xFF) + "." + (0xFF & dhcpInfo >> 8) + "." + (0xFF & dhcpInfo >> 16) + "." + (0xFF & dhcpInfo >> 24);
	}

	/**
	 * 从缓存中删除忽略的WiFi
	 *
	 * @param SSID
	 */
	private void removeWiFiFromSPBySSID(String SSID) {
		List<WiFiEntity> wifiList = JSON.parseArray(SPUtils.getInstance().getString(SPConfig.SAVED_WIFI, "[]"), WiFiEntity.class);
		if (null != wifiList && !wifiList.isEmpty()) {
			Iterator<WiFiEntity> iterator = wifiList.iterator();
			while (iterator.hasNext()) {
				if (iterator.next().getScanResult().SSID.equals(SSID)) {
					iterator.remove();
					ThreadUtils.runOnUiThread(() -> ToastUtils.showShort(SSID + "已删除, 还剩" + wifiList.size() + "条"));
					break;
				}
			}
			SPUtils.getInstance().put(SPConfig.SAVED_WIFI, JSON.toJSONString(wifiList));
		}
	}
}
