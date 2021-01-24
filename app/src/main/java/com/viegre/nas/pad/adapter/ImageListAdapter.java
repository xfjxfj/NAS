package com.viegre.nas.pad.adapter;

import androidx.appcompat.widget.AppCompatImageView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.entity.ImageEntity;

import org.jetbrains.annotations.NotNull;

/**
 * Created by レインマン on 2021/01/24 11:34 PM with Android Studio.
 */
public class ImageListAdapter extends BaseQuickAdapter<ImageEntity, BaseViewHolder> {

	public ImageListAdapter() {
		super(R.layout.item_image_list);
	}

	@Override
	protected void convert(@NotNull BaseViewHolder baseViewHolder, ImageEntity imageEntity) {
		AppCompatImageView acivItemImageRoot = baseViewHolder.getView(R.id.acivItemImageRoot);
//		ViewGroup.LayoutParams params = acivItemImageRoot.getLayoutParams();
//		params.width = params.height = 168;
//		acivItemImageRoot.setLayoutParams(params);
		Glide.with(getContext()).load(imageEntity.getPath()).centerCrop().into(acivItemImageRoot);
	}
}
