package com.viegre.nas.pad.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.viegre.nas.pad.R;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Created by レインマン on 2020/11/27 10:43 with Android Studio.
 */
public class NetworkDetailListAdapter extends BaseQuickAdapter<String[], BaseViewHolder> {

	public NetworkDetailListAdapter(@Nullable List<String[]> data) {
		super(R.layout.item_network_detail_list, data);
	}

	@Override
	protected void convert(@NotNull BaseViewHolder baseViewHolder, String[] info) {
		baseViewHolder.setText(R.id.actvItemNetworkDetailKey, info[0]).setText(R.id.actvItemNetworkDetailValue, info[1]);
	}
}
