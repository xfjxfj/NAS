package com.viegre.nas.pad.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.entity.ExternalStorageEntity;

import org.jetbrains.annotations.NotNull;

/**
 * Created by レインマン on 2021/04/02 10:18 with Android Studio.
 */
public class ExternalStorageAdapter extends BaseQuickAdapter<ExternalStorageEntity, BaseViewHolder> {

	public ExternalStorageAdapter() {
		super(R.layout.item_external_storage_list);
	}

	@Override
	protected void convert(@NotNull BaseViewHolder baseViewHolder, ExternalStorageEntity externalStorageEntity) {
		baseViewHolder.setText(R.id.actvItemExternalStorageList, externalStorageEntity.getName());
	}
}
