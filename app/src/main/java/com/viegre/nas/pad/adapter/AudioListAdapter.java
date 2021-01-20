package com.viegre.nas.pad.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.entity.AudioEntity;

import org.jetbrains.annotations.NotNull;

/**
 * Created by レインマン on 2021/01/19 17:27 with Android Studio.
 */
public class AudioListAdapter extends BaseQuickAdapter<AudioEntity, BaseViewHolder> {

	public AudioListAdapter() {
		super(R.layout.item_audio_list);
	}

	@Override
	protected void convert(@NotNull BaseViewHolder baseViewHolder, AudioEntity audioEntity) {
		baseViewHolder.setText(R.id.actvItemAudioListNumber, String.valueOf(baseViewHolder.getAdapterPosition() + 1))
		              .setText(R.id.actvItemAudioListName, audioEntity.getTitle())
		              .setText(R.id.actvItemAudioListArtist, audioEntity.getArtist())
		              .setText(R.id.actvItemAudioListAlbum, audioEntity.getAlbum())
		              .setText(R.id.actvItemAudioListDuration, audioEntity.getDuration());
	}
}
