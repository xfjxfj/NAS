package com.viegre.nas.pad.activity;

import android.media.MediaMetadataRetriever;
import android.net.Uri;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.PathUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.djangoogle.framework.activity.BaseActivity;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.adapter.AudioListAdapter;
import com.viegre.nas.pad.databinding.ActivityAudioBinding;
import com.viegre.nas.pad.entity.AudioEntity;
import com.viegre.nas.pad.filter.AudioFilter;
import com.viegre.nas.pad.manager.TextStyleManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 音频管理页
 * Created by レインマン on 2021/01/18 16:36 with Android Studio.
 */
public class AudioActivity extends BaseActivity<ActivityAudioBinding> {

	private AudioListAdapter mAudioListAdapter;
	private final boolean mIsPublic = true;

	@Override
	protected void initialize() {
		initRadioGroup();
		initList();
	}

	private void initRadioGroup() {
		mViewBinding.rgAudioTag.setOnCheckedChangeListener((radioGroup, i) -> {
			if (R.id.acrbAudioTagPrivate == i) {

			} else if (R.id.acrbAudioTagPublic == i) {

			}
		});
		TextStyleManager.INSTANCE.setFileManagerTag(mViewBinding.acrbAudioTagPrivate, mViewBinding.acrbAudioTagPublic);
	}

	private void initList() {
		mAudioListAdapter = new AudioListAdapter();
		ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<List<AudioEntity>>() {
			@Override
			public List<AudioEntity> doInBackground() {
				List<File> audioFileList = FileUtils.listFilesInDirWithFilter(PathUtils.getExternalAppAlarmsPath(), new AudioFilter(mIsPublic), true);
				List<AudioEntity> audioList = new ArrayList<>();
				for (File file : audioFileList) {
					MediaMetadataRetriever mmr = new MediaMetadataRetriever();
					Uri uri = Uri.fromFile(file);
					mmr.setDataSource(mActivity, uri);
					//获得时长
					mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
					//获得名称
					mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
				}
				return audioList;
			}

			@Override
			public void onSuccess(List<AudioEntity> result) {
				mAudioListAdapter.setList(result);
			}
		});
	}
}
