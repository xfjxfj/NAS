package com.viegre.nas.pad.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.entity.FileEntity;

import org.jetbrains.annotations.NotNull;

/**
 * Created by レインマン on 2021/04/02 10:18 with Android Studio.
 */
public class ExternalStorageListAdapter extends BaseQuickAdapter<FileEntity, BaseViewHolder> {

	public ExternalStorageListAdapter() {
		super(R.layout.item_external_storage);
	}

	@Override
	protected void convert(@NotNull BaseViewHolder baseViewHolder, FileEntity fileEntity) {
		baseViewHolder.setText(R.id.actvItemExternalStorageIcon, fileEntity.getName());
	}
}
