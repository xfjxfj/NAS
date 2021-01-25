package com.viegre.nas.pad.adapter;

import com.blankj.utilcode.util.TimeUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.entity.AudioEntity;
import com.viegre.nas.pad.manager.TextStyleManager;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import androidx.appcompat.widget.AppCompatTextView;

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
		AppCompatTextView actvItemAudioListNumber = baseViewHolder.getView(R.id.actvItemAudioListNumber);
		AppCompatTextView actvItemAudioListDisplayName = baseViewHolder.getView(R.id.actvItemAudioListDisplayName);
		AppCompatTextView actvItemAudioListArtist = baseViewHolder.getView(R.id.actvItemAudioListArtist);
		AppCompatTextView actvItemAudioListAlbum = baseViewHolder.getView(R.id.actvItemAudioListAlbum);
		AppCompatTextView actvItemAudioListDuration = baseViewHolder.getView(R.id.actvItemAudioListDuration);
		actvItemAudioListNumber.setText(mDecimalFormat.format(baseViewHolder.getAdapterPosition() + 1));
		actvItemAudioListDisplayName.setText(audioEntity.getName());
		actvItemAudioListArtist.setText(audioEntity.getArtist());
		actvItemAudioListAlbum.setText(audioEntity.getAlbumName());
		actvItemAudioListDuration.setText(TimeUtils.millis2String(audioEntity.getDuration(), new SimpleDateFormat("mm:ss", Locale.getDefault())));
		TextStyleManager.INSTANCE.setAudioPlayListCheck(audioEntity.isChecked(),
		                                                actvItemAudioListNumber,
		                                                actvItemAudioListDisplayName,
		                                                actvItemAudioListArtist,
		                                                actvItemAudioListAlbum,
		                                                actvItemAudioListDuration);
	}
}
