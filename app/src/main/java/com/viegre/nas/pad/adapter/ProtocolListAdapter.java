package com.viegre.nas.pad.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.entity.ProtocolEntity;

import org.jetbrains.annotations.NotNull;

/**
 * Created by レインマン on 2021/03/08 14:50 with Android Studio.
 */
public class ProtocolListAdapter extends BaseQuickAdapter<ProtocolEntity, BaseViewHolder> {

	public ProtocolListAdapter() {
		super(R.layout.item_protocol);
	}

	@Override
	protected void convert(@NotNull BaseViewHolder baseViewHolder, ProtocolEntity protocolEntity) {
		baseViewHolder.setText(R.id.actvProtocolName, protocolEntity.getName());
	}
}
