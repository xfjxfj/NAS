package com.viegre.nas.pad.adapter;

import androidx.appcompat.widget.AppCompatImageView;

import com.blankj.utilcode.util.StringUtils;
import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.entity.ImageAlbumEntity;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

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
		                       StringUtils.getString(R.string.screen_custom_album_count, imageAlbumEntity.getImageSet().size()))
		              .setImageResource(R.id.acivItemScreenCustomAlbumCheck,
		                                imageAlbumEntity.isCheck() ? R.mipmap.screen_custom_checked : R.mipmap.screen_custom_unchecked);
		AppCompatImageView acivItemScreenCustomAlbumThumbnail1 = baseViewHolder.getView(R.id.acivItemScreenCustomAlbumThumbnail1);
		AppCompatImageView acivItemScreenCustomAlbumThumbnail2 = baseViewHolder.getView(R.id.acivItemScreenCustomAlbumThumbnail2);
		AppCompatImageView acivItemScreenCustomAlbumThumbnail3 = baseViewHolder.getView(R.id.acivItemScreenCustomAlbumThumbnail3);
		AppCompatImageView acivItemScreenCustomAlbumThumbnail4 = baseViewHolder.getView(R.id.acivItemScreenCustomAlbumThumbnail4);
		List<String> list = new ArrayList<>(imageAlbumEntity.getImageSet());
		if (imageAlbumEntity.getImageSet().size() < 1) {
			acivItemScreenCustomAlbumThumbnail1.setImageResource(0);
			acivItemScreenCustomAlbumThumbnail2.setImageResource(0);
			acivItemScreenCustomAlbumThumbnail3.setImageResource(0);
			acivItemScreenCustomAlbumThumbnail4.setImageResource(0);
		} else if (imageAlbumEntity.getImageSet().size() == 1) {
			Glide.with(getContext()).load(list.get(0)).centerCrop().into(acivItemScreenCustomAlbumThumbnail1);
			acivItemScreenCustomAlbumThumbnail2.setImageResource(0);
			acivItemScreenCustomAlbumThumbnail3.setImageResource(0);
			acivItemScreenCustomAlbumThumbnail4.setImageResource(0);
		} else if (imageAlbumEntity.getImageSet().size() == 2) {
			Glide.with(getContext()).load(list.get(0)).centerCrop().into(acivItemScreenCustomAlbumThumbnail1);
			Glide.with(getContext()).load(list.get(1)).centerCrop().into(acivItemScreenCustomAlbumThumbnail2);
			acivItemScreenCustomAlbumThumbnail3.setImageResource(0);
			acivItemScreenCustomAlbumThumbnail4.setImageResource(0);
		} else if (imageAlbumEntity.getImageSet().size() == 3) {
			Glide.with(getContext()).load(list.get(0)).centerCrop().into(acivItemScreenCustomAlbumThumbnail1);
			Glide.with(getContext()).load(list.get(1)).centerCrop().into(acivItemScreenCustomAlbumThumbnail2);
			Glide.with(getContext()).load(list.get(2)).centerCrop().into(acivItemScreenCustomAlbumThumbnail3);
			acivItemScreenCustomAlbumThumbnail4.setImageResource(0);
		} else {
			Glide.with(getContext()).load(list.get(0)).centerCrop().into(acivItemScreenCustomAlbumThumbnail1);
			Glide.with(getContext()).load(list.get(1)).centerCrop().into(acivItemScreenCustomAlbumThumbnail2);
			Glide.with(getContext()).load(list.get(2)).centerCrop().into(acivItemScreenCustomAlbumThumbnail3);
			Glide.with(getContext()).load(list.get(3)).centerCrop().into(acivItemScreenCustomAlbumThumbnail4);
		}
	}
}
