package com.viegre.nas.speaker.adapter;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.Utils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.viegre.nas.speaker.R;
import com.viegre.nas.speaker.entity.WiFiEntity;

import org.jetbrains.annotations.NotNull;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by Djangoogle on 2020/11/27 10:43 with Android Studio.
 */
public class WLANListAdapter extends BaseQuickAdapter<WiFiEntity, BaseViewHolder> {

	private final WifiManager mWifiManager;

	public WLANListAdapter() {
		super(R.layout.item_wlan_list);
		mWifiManager = (WifiManager) Utils.getApp().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		addChildClickViewIds(R.id.acivItemWLANArrow);
	}

	@Override
	protected void convert(@NotNull BaseViewHolder baseViewHolder, WiFiEntity wiFiEntity) {
		//隐藏已连接WiFi
		RecyclerView.LayoutParams param = (RecyclerView.LayoutParams) baseViewHolder.getView(R.id.clItemWLANRoot).getLayoutParams();
		if (wiFiEntity.getScanResult().BSSID.equals(mWifiManager.getConnectionInfo().getBSSID())) {
			baseViewHolder.setGone(R.id.clItemWLANRoot, true);
			param.height = 0;
			param.width = 0;
		} else {
			param.width = ConstraintLayout.LayoutParams.MATCH_PARENT;
			param.height = ConvertUtils.dp2px(33.5F);
			baseViewHolder.setGone(R.id.clItemWLANRoot, false);
		}
		baseViewHolder.setImageResource(R.id.acivItemWLANStatus, R.mipmap.wlan_item_link)
		              .setText(R.id.actvItemWLANName,
		                       TextUtils.isEmpty(wiFiEntity.getScanResult().SSID) ? wiFiEntity.getScanResult().BSSID : wiFiEntity.getScanResult().SSID)
		              .setGone(R.id.actvItemWLANTip, TextUtils.isEmpty(wiFiEntity.getPassword()));
		if (wiFiEntity.getScanResult().level >= -50) {
			baseViewHolder.setImageResource(R.id.acivItemWLANSignal, R.mipmap.wifi_signal_4);
		} else if (wiFiEntity.getScanResult().level < -50 && wiFiEntity.getScanResult().level >= -70) {
			baseViewHolder.setImageResource(R.id.acivItemWLANSignal, R.mipmap.wifi_signal_3);
		} else if (wiFiEntity.getScanResult().level < -70 && wiFiEntity.getScanResult().level >= -80) {
			baseViewHolder.setImageResource(R.id.acivItemWLANSignal, R.mipmap.wifi_signal_2);
		} else {
			baseViewHolder.setImageResource(R.id.acivItemWLANSignal, R.mipmap.wifi_signal_1);
		}
	}
}
