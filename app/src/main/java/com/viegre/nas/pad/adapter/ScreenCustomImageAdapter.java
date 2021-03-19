package com.viegre.nas.pad.adapter;

import androidx.appcompat.widget.AppCompatImageView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.entity.ImageEntity;

import org.jetbrains.annotations.NotNull;

/**
 * Created by レインマン on 2021/03/14 8:54 PM with Android Studio.
 */
public class ScreenCustomImageAdapter extends BaseQuickAdapter<ImageEntity, BaseViewHolder> {

	public ScreenCustomImageAdapter() {
		super(R.layout.item_screen_custom_image);
	}

	@Override
	protected void convert(@NotNull BaseViewHolder baseViewHolder, ImageEntity imageEntity) {
		AppCompatImageView acivItemScreenCustomImageContent = baseViewHolder.getView(R.id.acivItemScreenCustomImageContent);
		Glide.with(getContext()).load(imageEntity.getPath()).into(acivItemScreenCustomImageContent);
		baseViewHolder.setImageResource(R.id.acivItemScreenCustomImageCheck,
		                                imageEntity.isCheck() ? R.mipmap.screen_custom_checked : 0);
	}
}
