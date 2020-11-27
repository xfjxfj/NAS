package com.viegre.nas.speaker.adapter;

import android.net.wifi.ScanResult;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.sunfusheng.marqueeview.MarqueeView;
import com.viegre.nas.speaker.R;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Djangoogle on 2020/11/27 10:43 with Android Studio.
 */
public class WLANListAdapter extends BaseQuickAdapter<ScanResult, BaseViewHolder> {

	public WLANListAdapter(int layoutResId) {
		super(layoutResId);
	}

	@Override
	protected void convert(@NotNull BaseViewHolder baseViewHolder, ScanResult scanResult) {
		baseViewHolder.setImageResource(R.id.acivItemWLANStatus, R.mipmap.wlan_item_link).setText(R.id.mvItemWLANName, scanResult.SSID);
		MarqueeView marqueeView = baseViewHolder.getView(R.id.mvItemWLANName);
		marqueeView.startWithText(scanResult.SSID);
	}
}
