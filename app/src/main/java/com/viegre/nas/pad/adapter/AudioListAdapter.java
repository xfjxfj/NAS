package com.viegre.nas.pad.adapter;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.entity.AudioEntity;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by レインマン on 2021/01/19 17:27 with Android Studio.
 */
public class AudioListAdapter extends BaseQuickAdapter<AudioEntity, BaseViewHolder> {

	private final DecimalFormat mDecimalFormat;

	public AudioListAdapter() {
		super(R.layout.item_audio_list);
		mDecimalFormat = new DecimalFormat("00");
	}

	@Override
	protected void convert(@NotNull BaseViewHolder baseViewHolder, AudioEntity audioEntity) {
		baseViewHolder.setText(R.id.actvItemAudioListNumber, mDecimalFormat.format(baseViewHolder.getAdapterPosition() + 1))
		              .setText(R.id.actvItemAudioListDisplayName, FileUtils.getFileNameNoExtension(audioEntity.getDisplayName()))
		              .setText(R.id.actvItemAudioListArtist, audioEntity.getArtist())
		              .setText(R.id.actvItemAudioListAlbum, audioEntity.getAlbum())
		              .setText(R.id.actvItemAudioListDuration,
		                       TimeUtils.millis2String(audioEntity.getDuration(), new SimpleDateFormat("mm:ss", Locale.getDefault())));
	}
}
