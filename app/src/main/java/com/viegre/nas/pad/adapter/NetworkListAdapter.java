package com.viegre.nas.pad.adapter;

import android.content.Context;
import android.net.wifi.WifiManager;

import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.Utils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.entity.WiFiEntity;

import org.jetbrains.annotations.NotNull;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by レインマン on 2020/11/27 10:43 with Android Studio.
 */
public class NetworkListAdapter extends BaseQuickAdapter<WiFiEntity, BaseViewHolder> {

	private final WifiManager mWifiManager;

	public NetworkListAdapter() {
		super(R.layout.item_network_list);
		mWifiManager = (WifiManager) Utils.getApp().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		addChildClickViewIds(R.id.acivItemNetworkArrow);
	}

	@Override
	protected void convert(@NotNull BaseViewHolder baseViewHolder, WiFiEntity wiFiEntity) {
		//隐藏已连接WiFi
		RecyclerView.LayoutParams param = (RecyclerView.LayoutParams) baseViewHolder.getView(R.id.clItemNetworkRoot).getLayoutParams();
		if (wiFiEntity.getScanResult().BSSID.equals(mWifiManager.getConnectionInfo().getBSSID())) {
			baseViewHolder.setGone(R.id.clItemNetworkRoot, true);
			param.height = 0;
			param.width = 0;
		} else {
			param.width = ConstraintLayout.LayoutParams.MATCH_PARENT;
			param.height = 67;
			baseViewHolder.setGone(R.id.clItemNetworkRoot, false);
		}
		baseViewHolder.setImageResource(R.id.acivItemNetworkStatus, R.mipmap.network_item_link)
		              .setText(R.id.actvItemNetworkName,
		                       StringUtils.isEmpty(wiFiEntity.getScanResult().SSID) ? wiFiEntity.getScanResult().BSSID : wiFiEntity.getScanResult().SSID)
		              .setGone(R.id.actvItemNetworkTip, StringUtils.isEmpty(wiFiEntity.getPassword()));
		if (wiFiEntity.getScanResult().level >= -50) {
			baseViewHolder.setImageResource(R.id.acivItemNetworkSignal, R.mipmap.network_wifi_signal_4);
		} else if (wiFiEntity.getScanResult().level < -50 && wiFiEntity.getScanResult().level >= -70) {
			baseViewHolder.setImageResource(R.id.acivItemNetworkSignal, R.mipmap.network_wifi_signal_3);
		} else if (wiFiEntity.getScanResult().level < -70 && wiFiEntity.getScanResult().level >= -80) {
			baseViewHolder.setImageResource(R.id.acivItemNetworkSignal, R.mipmap.network_wifi_signal_2);
		} else {
			baseViewHolder.setImageResource(R.id.acivItemNetworkSignal, R.mipmap.network_wifi_signal_1);
		}
	}
}
