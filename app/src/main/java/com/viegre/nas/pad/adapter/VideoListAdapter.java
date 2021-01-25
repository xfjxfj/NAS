package com.viegre.nas.pad.adapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.entity.VideoEntity;

import org.jetbrains.annotations.NotNull;

import androidx.appcompat.widget.AppCompatImageView;

/**
 * Created by レインマン on 2021/01/25 1:18 AM with Android Studio.
 */
public class VideoListAdapter extends BaseQuickAdapter<VideoEntity, BaseViewHolder> {

	public VideoListAdapter() {
		super(R.layout.item_video_list);
	}

	@Override
	protected void convert(@NotNull BaseViewHolder baseViewHolder, VideoEntity videoEntity) {
		AppCompatImageView acivItemVideoThumbnail = baseViewHolder.getView(R.id.acivItemVideoThumbnail);
		Glide.with(getContext())
		     .setDefaultRequestOptions(new RequestOptions().frame(1000000).centerCrop())
		     .load(videoEntity.getPath())
		     .into(acivItemVideoThumbnail);
		baseViewHolder.setText(R.id.actvItemVideoName, videoEntity.getName());
	}
}
