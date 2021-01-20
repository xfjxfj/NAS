package com.viegre.nas.pad.activity;

import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.blankj.utilcode.util.BusUtils;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.PathUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.UriUtils;
import com.djangoogle.framework.activity.BaseActivity;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.adapter.AudioListAdapter;
import com.viegre.nas.pad.config.BusConfig;
import com.viegre.nas.pad.config.FileConfig;
import com.viegre.nas.pad.databinding.ActivityAudioBinding;
import com.viegre.nas.pad.entity.AudioEntity;
import com.viegre.nas.pad.manager.TextStyleManager;
import com.viegre.nas.pad.receiver.FileReceiver;

import java.util.ArrayList;
import java.util.List;

/**
 * 音频管理页
 * Created by レインマン on 2021/01/18 16:36 with Android Studio.
 */
public class AudioActivity extends BaseActivity<ActivityAudioBinding> {

	private final GetAudioListTask mGetAudioListTask = new GetAudioListTask();
	private AudioListAdapter mAudioListAdapter;
	private volatile boolean mIsPublic = true;
	private FileReceiver mFileReceiver;
	private static final String[] AUDIO_MIME_TYPES = new String[]{FileConfig.AUDIO_TYPE_MP3, FileConfig.AUDIO_TYPE_WMA, FileConfig.AUDIO_TYPE_FLAC, FileConfig.AUDIO_TYPE_APE, FileConfig.AUDIO_TYPE_M4A};

	@Override
	protected void initialize() {
		registerFileReceiver();
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
		if (null != mFileReceiver) {
			unregisterReceiver(mFileReceiver);
		}
	}

	private void registerFileReceiver() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
		intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
		intentFilter.addDataScheme("file");
		mFileReceiver = new FileReceiver();
		registerReceiver(mFileReceiver, intentFilter);
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
		scanFile();
	}

	private class GetAudioListTask extends ThreadUtils.SimpleTask<List<AudioEntity>> {
		@Override
		public List<AudioEntity> doInBackground() {
			return getAudioList();
		}

		@Override
		public void onSuccess(List<AudioEntity> result) {
			mAudioListAdapter.setList(result);
			mViewBinding.srlAudioRefresh.setRefreshing(false);
			mViewBinding.rgAudioTag.setEnabled(true);
			mViewBinding.rvAudioList.setEnabled(true);
		}
	}

	private List<AudioEntity> getAudioList() {
		List<AudioEntity> audioList = new ArrayList<>();
		Uri uri = UriUtils.file2Uri(FileUtils.getFileByPath(PathUtils.getExternalStoragePath()));
		Cursor cursor = getContentResolver().query(uri,
		                                           new String[]{MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.YEAR, MediaStore.Audio.Media.MIME_TYPE, MediaStore.Audio.Media.SIZE, MediaStore.Audio.Media.DATA},
		                                           MediaStore.Audio.Media.MIME_TYPE + " = ? or " + MediaStore.Audio.Media.MIME_TYPE + " = ? or " + MediaStore.Audio.Media.MIME_TYPE + " = ? or " + MediaStore.Audio.Media.MIME_TYPE + " = ? or " + MediaStore.Audio.Media.MIME_TYPE + " = ?",
		                                           AUDIO_MIME_TYPES,
		                                           null);

		if (cursor.moveToFirst()) {
			do {
				AudioEntity audio = new AudioEntity();
				//文件名
				audio.setFileName(cursor.getString(1));
				//歌曲名
				audio.setTitle(cursor.getString(2));
				//时长
				audio.setDuration(cursor.getInt(3));
				//歌手名
				audio.setArtist(cursor.getString(4));
				//专辑名
				audio.setAlbum(cursor.getString(5));
				//年代
				if (null != cursor.getString(6)) {
					audio.setYear(cursor.getString(6));
				} else {
					audio.setYear(StringUtils.getString(R.string.unknown));
				}
				//歌曲格式
				if (FileConfig.AUDIO_TYPE_MP3.equals(cursor.getString(7).trim())) {
					audio.setType("mp3");
				} else if (FileConfig.AUDIO_TYPE_WMA.equals(cursor.getString(7).trim())) {
					audio.setType("wma");
				} else if (FileConfig.AUDIO_TYPE_FLAC.equals(cursor.getString(7).trim())) {
					audio.setType("flac");
				} else if (FileConfig.AUDIO_TYPE_APE.equals(cursor.getString(7).trim())) {
					audio.setType("ape");
				} else if (FileConfig.AUDIO_TYPE_M4A.equals(cursor.getString(7).trim())) {
					audio.setType("m4a");
				}
				//文件大小
				if (null != cursor.getString(8)) {
					audio.setSize(ConvertUtils.byte2FitMemorySize(cursor.getInt(8), 2));
				} else {
					audio.setSize(StringUtils.getString(R.string.unknown));
				}
				//文件路径
				if (null != cursor.getString(9)) {
					audio.setFileUrl(cursor.getString(9));
				}
				audioList.add(audio);
			} while (cursor.moveToNext());
			cursor.close();
		}
		return audioList;
	}

	private void scanFile() {
		MediaScannerConnection.scanFile(this, new String[]{PathUtils.getExternalStoragePath()}, AUDIO_MIME_TYPES, (s, uri) -> {
			ThreadUtils.executeByCached(mGetAudioListTask);
//			Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//			intent.setData(uri);
//			sendBroadcast(intent);
		});
	}

	@BusUtils.Bus(tag = BusConfig.MEDIA_SCANNER_FINISHED, threadMode = BusUtils.ThreadMode.MAIN)
	public void queryAudio() {
		ThreadUtils.executeByCached(mGetAudioListTask);
	}
}
