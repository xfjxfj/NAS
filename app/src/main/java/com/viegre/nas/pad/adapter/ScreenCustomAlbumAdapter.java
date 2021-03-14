package com.viegre.nas.pad.adapter;

import com.blankj.utilcode.util.StringUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.entity.ImageAlbumEntity;

import org.jetbrains.annotations.NotNull;

/**
 * Created by レインマン on 2021/03/12 11:16 with Android Studio.
 */
public class ScreenCustomAlbumAdapter extends BaseQuickAdapter<ImageAlbumEntity, BaseViewHolder> {

	public ScreenCustomAlbumAdapter() {
		super(R.layout.item_screen_custom_album);
	}

	@Override
	protected void convert(@NotNull BaseViewHolder baseViewHolder, ImageAlbumEntity imageAlbumEntity) {
		baseViewHolder.setText(R.id.actvItemScreenCustomAlbumName, imageAlbumEntity.getName())
		              .setText(R.id.actvItemScreenCustomAlbumCount,
		                       StringUtils.getString(R.string.screen_custom_album_count, imageAlbumEntity.getCount()))
		              .setImageResource(R.id.acivItemScreenCustomAlbumCheck,
		                                imageAlbumEntity.isCheck() ? R.mipmap.screen_custom_checked : R.mipmap.screen_custom_unchecked);
	}
}
