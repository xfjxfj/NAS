package com.viegre.nas.pad.activity;

import android.media.MediaMetadataRetriever;
import android.net.Uri;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.PathUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.djangoogle.framework.activity.BaseActivity;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.adapter.AudioListAdapter;
import com.viegre.nas.pad.databinding.ActivityAudioBinding;
import com.viegre.nas.pad.entity.AudioEntity;
import com.viegre.nas.pad.filter.AudioFilter;
import com.viegre.nas.pad.manager.TextStyleManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.recyclerview.widget.LinearLayoutManager;

/**
 * 音频管理页
 * Created by レインマン on 2021/01/18 16:36 with Android Studio.
 */
public class AudioActivity extends BaseActivity<ActivityAudioBinding> {

	private final GetAudioListTask mGetAudioListTask = new GetAudioListTask();
	private AudioListAdapter mAudioListAdapter;
	private boolean mIsPublic = true;

	@Override
	protected void initialize() {
		mViewBinding.iAudioTitle.actvFileManagerTitle.setText(R.string.audio);
		mViewBinding.iAudioTitle.llcFileManagerTitleBack.setOnClickListener(view -> finish());
		initRadioGroup();
		initList();
		mViewBinding.srlAudioRefresh.setRefreshing(true);
		initData();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		ThreadUtils.cancel(mGetAudioListTask);
	}

	private void initRadioGroup() {
		mViewBinding.rgAudioTag.setOnCheckedChangeListener((radioGroup, i) -> {
			if (R.id.acrbAudioTagPrivate == i) {
				mIsPublic = false;
			} else if (R.id.acrbAudioTagPublic == i) {
				mIsPublic = true;
			}
			mViewBinding.srlAudioRefresh.setRefreshing(true);
			initData();
		});
		TextStyleManager.INSTANCE.setFileManagerTag(mViewBinding.acrbAudioTagPrivate, mViewBinding.acrbAudioTagPublic);
	}

	private void initList() {
		mAudioListAdapter = new AudioListAdapter();
		mViewBinding.rvAudioList.setLayoutManager(new LinearLayoutManager(this));
		mViewBinding.rvAudioList.setAdapter(mAudioListAdapter);
		mViewBinding.srlAudioRefresh.setColorSchemeResources(R.color.settings_menu_selected_bg);
		mViewBinding.srlAudioRefresh.setProgressBackgroundColorSchemeResource(R.color.file_manager_tag_unpressed);
		mViewBinding.srlAudioRefresh.setOnRefreshListener(this::initData);
	}

	private void initData() {
		mViewBinding.rgAudioTag.setEnabled(false);
		mViewBinding.rvAudioList.setEnabled(false);
		ThreadUtils.executeByCached(mGetAudioListTask);
	}

	private class GetAudioListTask extends ThreadUtils.SimpleTask<List<AudioEntity>> {
		@Override
		public List<AudioEntity> doInBackground() {
			List<File> audioFileList = FileUtils.listFilesInDirWithFilter(mIsPublic ? PathUtils.getExternalStoragePath() : PathUtils.getExternalAppFilesPath(),
			                                                              new AudioFilter(mIsPublic),
			                                                              true);
			List<AudioEntity> audioList = new ArrayList<>();
			if (!audioFileList.isEmpty()) {
				for (int i = 0; i < audioFileList.size(); i++) {
					File audio = audioFileList.get(i);
					MediaMetadataRetriever mmr = new MediaMetadataRetriever();
					Uri uri = Uri.fromFile(audio);
					mmr.setDataSource(mActivity, uri);
					mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
					//获得时长
					String duration = TimeUtils.millis2String(Long.parseLong(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)),
					                                          new SimpleDateFormat("mm:ss", Locale.getDefault()));
					audioList.add(new AudioEntity(String.valueOf(i + 1),
					                              audio.getName(),
					                              mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST),
					                              mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM),
					                              duration));
				}
			}
			return audioList;
		}

		@Override
		public void onSuccess(List<AudioEntity> result) {
			mAudioListAdapter.setList(result);
			mViewBinding.srlAudioRefresh.setRefreshing(false);
			mViewBinding.rgAudioTag.setEnabled(true);
			mViewBinding.rvAudioList.setEnabled(true);
		}
	}
}
