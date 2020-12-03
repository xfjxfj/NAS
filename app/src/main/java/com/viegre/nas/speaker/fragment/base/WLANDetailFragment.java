package com.viegre.nas.speaker.fragment.base;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;

import com.alibaba.fastjson.JSON;
import com.blankj.utilcode.util.BusUtils;
import com.blankj.utilcode.util.ColorUtils;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.Utils;
import com.thanosfisherman.wifiutils.WifiUtils;
import com.thanosfisherman.wifiutils.wifiRemove.RemoveErrorCode;
import com.thanosfisherman.wifiutils.wifiRemove.RemoveSuccessListener;
import com.viegre.nas.speaker.R;
import com.viegre.nas.speaker.adapter.WLANDetailListAdapter;
import com.viegre.nas.speaker.config.BusConfig;
import com.viegre.nas.speaker.config.SPConfig;
import com.viegre.nas.speaker.databinding.FragmentWlanDetailBinding;
import com.viegre.nas.speaker.entity.WiFiEntity;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

/**
 * 网络详情
 * Created by Djangoogle on 2020/12/03 15:49 with Android Studio.
 */
public class WLANDetailFragment extends BaseFragment<FragmentWlanDetailBinding> {

	@Override
	protected void initView() {
		mViewBinding.llcWLANDetailBack.setOnClickListener(view -> BusUtils.post(BusConfig.BUS_NETWORK_DETAIL, BusConfig.BUS_HIDE_NETWORK_DETAIL));
		String SSID = SPUtils.getInstance().getString(SPConfig.SP_CURRENT_WIFI_SSID, "");
		mViewBinding.actvWLANDetailSSID.setText(SSID);
		mViewBinding.actvWLANDetailIgnore.setOnClickListener(view -> WifiUtils.withContext(mActivity).remove(SSID, new RemoveSuccessListener() {
			@Override
			public void success() {
				removeWiFiFromSPBySSID(SSID);
				BusUtils.post(BusConfig.BUS_NETWORK_DETAIL, BusConfig.BUS_HIDE_NETWORK_DETAIL);
			}

			@Override
			public void failed(@NonNull RemoveErrorCode errorCode) {
				ToastUtils.showShort(R.string.ignore_network_failed);
			}
		}));
	}

	@Override
	protected void initData() {
		initList();
	}

	public static WLANDetailFragment newInstance() {
		return new WLANDetailFragment();
	}

	private void initList() {
		ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<List<String[]>>() {
			@Override
			public List<String[]> doInBackground() {
				List<String[]> networkInfoList = new ArrayList<>();
				networkInfoList.add(new String[]{StringUtils.getString(R.string.network_status), "已连接"});
				networkInfoList.add(new String[]{StringUtils.getString(R.string.ip_address), NetworkUtils.getIpAddressByWifi()});
				networkInfoList.add(new String[]{StringUtils.getString(R.string.subnet_mask), NetworkUtils.getNetMaskByWifi()});
				networkInfoList.add(new String[]{StringUtils.getString(R.string.gateway), NetworkUtils.getGatewayByWifi()});
				WifiManager wifiManager = (WifiManager) Utils.getApp().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
				DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
				networkInfoList.add(new String[]{StringUtils.getString(R.string.dns), dhcpInfo2String(dhcpInfo.dns1)});
				return networkInfoList;
			}

			@Override
			public void onSuccess(List<String[]> result) {
				mViewBinding.rvWLANDetail.setLayoutManager(new LinearLayoutManager(mActivity));
				mViewBinding.rvWLANDetail.addItemDecoration(new HorizontalDividerItemDecoration.Builder(mActivity).color(ColorUtils.getColor(R.color.wlan_dividing_line))
				                                                                                                  .size(ConvertUtils.dp2px(0.5F))
				                                                                                                  .margin(ConvertUtils.dp2px(12.5F),
				                                                                                                          ConvertUtils.dp2px(12.5F))
				                                                                                                  .build());
				mViewBinding.rvWLANDetail.setAdapter(new WLANDetailListAdapter(result));
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
		List<WiFiEntity> wifiList = JSON.parseArray(SPUtils.getInstance().getString(SPConfig.SP_SAVED_WIFI, ""), WiFiEntity.class);
		if (null != wifiList && !wifiList.isEmpty()) {
			Iterator<WiFiEntity> iterator = wifiList.iterator();
			while (iterator.hasNext()) {
				if (iterator.next().getScanResult().SSID.equals(SSID)) {
					iterator.remove();
					break;
				}
			}
			SPUtils.getInstance().put(SPConfig.SP_SAVED_WIFI, JSON.toJSONString(wifiList));
		}
	}
}
