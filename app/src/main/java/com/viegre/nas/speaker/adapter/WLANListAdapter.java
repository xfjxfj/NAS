package com.viegre.nas.speaker.adapter;

import android.net.wifi.ScanResult;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.viegre.nas.speaker.R;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Djangoogle on 2020/11/27 10:43 with Android Studio.
 */
public class WLANListAdapter extends BaseQuickAdapter<ScanResult, BaseViewHolder> {

	public WLANListAdapter(int layoutResId) {
		super(layoutResId);
		addChildClickViewIds(R.id.acivItemWLANArrow);
	}

	@Override
	protected void convert(@NotNull BaseViewHolder baseViewHolder, ScanResult scanResult) {
		baseViewHolder.setImageResource(R.id.acivItemWLANStatus, R.mipmap.wlan_item_link).setText(R.id.actvItemWLANName, scanResult.SSID);
		if (scanResult.level >= -50) {
			baseViewHolder.setImageResource(R.id.acivItemWLANSignal, R.mipmap.wifi_signal_4);
		} else if (scanResult.level < -50 && scanResult.level >= -70) {
			baseViewHolder.setImageResource(R.id.acivItemWLANSignal, R.mipmap.wifi_signal_3);
		} else if (scanResult.level < -70 && scanResult.level >= -80) {
			baseViewHolder.setImageResource(R.id.acivItemWLANSignal, R.mipmap.wifi_signal_2);
		} else {
			baseViewHolder.setImageResource(R.id.acivItemWLANSignal, R.mipmap.wifi_signal_1);
		}
	}
}
