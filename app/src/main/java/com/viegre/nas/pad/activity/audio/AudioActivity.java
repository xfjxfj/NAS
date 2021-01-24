package com.viegre.nas.pad.activity.audio;

import android.content.Intent;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.BusUtils;
import com.blankj.utilcode.util.PathUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.Utils;
import com.djangoogle.framework.activity.BaseActivity;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.adapter.AudioListAdapter;
import com.viegre.nas.pad.config.BusConfig;
import com.viegre.nas.pad.config.FileConfig;
import com.viegre.nas.pad.config.PathConfig;
import com.viegre.nas.pad.databinding.ActivityAudioBinding;
import com.viegre.nas.pad.entity.AudioEntity;
import com.viegre.nas.pad.manager.TextStyleManager;

import org.litepal.LitePal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 音频管理页
 * Created by レインマン on 2021/01/18 16:36 with Android Studio.
 */
public class AudioActivity extends BaseActivity<ActivityAudioBinding> {

	private final QueryAudioByCursorTask mQueryAudioByCursorTask = new QueryAudioByCursorTask();
	private final QueryAudioByLitepalTask mQueryAudioByLitepalTask = new QueryAudioByLitepalTask();
	private AudioListAdapter mAudioListAdapter;
	private volatile boolean mIsPublic = true;

	@Override
	protected void initialize() {
		mViewBinding.iAudioTitle.actvFileManagerTitle.setText(R.string.audio);
		mViewBinding.iAudioTitle.llcFileManagerTitleBack.setOnClickListener(view -> finish());
		initRadioGroup();
		initList();
		mViewBinding.srlAudioRefresh.setRefreshing(true);
		queryAudioByLitepal();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		ThreadUtils.cancel(mQueryAudioByCursorTask);
		ThreadUtils.cancel(mQueryAudioByLitepalTask);
	}

	private void initRadioGroup() {
		mViewBinding.rgAudioTag.setOnCheckedChangeListener((radioGroup, i) -> {
			if (R.id.acrbAudioTagPrivate == i) {
				mIsPublic = false;
			} else if (R.id.acrbAudioTagPublic == i) {
				mIsPublic = true;
			}
			mViewBinding.srlAudioRefresh.setRefreshing(true);
			queryAudioByLitepal();
		});
		TextStyleManager.INSTANCE.setFileManagerTag(mViewBinding.acrbAudioTagPrivate, mViewBinding.acrbAudioTagPublic);
	}

	private void initList() {
		mAudioListAdapter = new AudioListAdapter();
		mAudioListAdapter.setOnItemClickListener((adapter, view, position) -> {
			AudioEntity audioEntity = mAudioListAdapter.getItem(position);
			Intent intent = new Intent(this, AudioPlayerActivity.class);
			intent.putExtra("audioPath", audioEntity.getPath());
			ActivityUtils.startActivity(intent);
		});
		mViewBinding.rvAudioList.setLayoutManager(new LinearLayoutManager(this));
		mViewBinding.rvAudioList.setAdapter(mAudioListAdapter);
		//禁用动画以防止更新列表时产生闪烁
		SimpleItemAnimator simpleItemAnimator = (SimpleItemAnimator) mViewBinding.rvAudioList.getItemAnimator();
		if (null != simpleItemAnimator) {
			simpleItemAnimator.setSupportsChangeAnimations(false);
		}
		mViewBinding.srlAudioRefresh.setColorSchemeResources(R.color.settings_menu_selected_bg);
		mViewBinding.srlAudioRefresh.setProgressBackgroundColorSchemeResource(R.color.file_manager_tag_unpressed);
		mViewBinding.srlAudioRefresh.setOnRefreshListener(this::queryAudioByCursor);
	}

	private void queryAudioByCursor() {
		mViewBinding.acrbAudioTagPrivate.setEnabled(false);
		mViewBinding.acrbAudioTagPublic.setEnabled(false);
		mViewBinding.rvAudioList.setEnabled(false);
		scanMediaFile();
	}

	private void scanMediaFile() {
//		ShellUtils.execCmdAsync("find /mnt/sdcard/Music/ -exec am broadcast \\\n" + "    -a android.intent.action.MEDIA_SCANNER_SCAN_FILE \\\n" + "    -d file://{} \\\\",
//		                        true,
//		                        commandResult -> {
//			                        LogUtils.iTag("ShellUtils", commandResult.toString());
//			                        BusUtils.post(BusConfig.MEDIA_MOUNTED);
//		                        });
//		sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + PathUtils.getExternalStoragePath())));
		MediaScannerConnection.scanFile(this, null, null, new MediaScannerConnection.OnScanCompletedListener() {
			@Override
			public void onScanCompleted(String s, Uri uri) {
				BusUtils.post(BusConfig.MEDIA_MOUNTED);
			}
		});
	}

	private void queryAudioByLitepal() {
		mViewBinding.acrbAudioTagPrivate.setEnabled(false);
		mViewBinding.acrbAudioTagPublic.setEnabled(false);
		mViewBinding.rvAudioList.setEnabled(false);
		ThreadUtils.executeByCached(mQueryAudioByLitepalTask);
	}

	private class QueryAudioByCursorTask extends ThreadUtils.SimpleTask<List<AudioEntity>> {
		@Override
		public List<AudioEntity> doInBackground() {
			return scanAndSaveAudioList();
		}

		@Override
		public void onSuccess(List<AudioEntity> result) {
			queryCompleted(result);
		}
	}

	private class QueryAudioByLitepalTask extends ThreadUtils.SimpleTask<List<AudioEntity>> {
		@Override
		public List<AudioEntity> doInBackground() {
			return LitePal.findAll(AudioEntity.class);
		}

		@Override
		public void onSuccess(List<AudioEntity> result) {
			if (result.isEmpty()) {
				scanMediaFile();
			} else {
				queryCompleted(result);
			}
		}
	}

	private void queryCompleted(List<AudioEntity> audioList) {
		mAudioListAdapter.setList(audioList);
		mViewBinding.srlAudioRefresh.setRefreshing(false);
		mViewBinding.acrbAudioTagPrivate.setEnabled(true);
		mViewBinding.acrbAudioTagPublic.setEnabled(true);
		mViewBinding.rvAudioList.setEnabled(true);
	}

	private List<AudioEntity> scanAndSaveAudioList() {
		List<AudioEntity> audioList = new ArrayList<>();
		Cursor cursor = Utils.getApp()
		                     .getContentResolver()
		                     .query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
		                            new String[]{MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media._ID, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.DURATION},
		                            MediaStore.Audio.Media.MIME_TYPE + "=? or " + MediaStore.Audio.Media.MIME_TYPE + "=? or " + MediaStore.Audio.Media.MIME_TYPE + "=? or " + MediaStore.Audio.Media.MIME_TYPE + "=? or " + MediaStore.Audio.Media.MIME_TYPE + "=?",
		                            FileConfig.AUDIO_TYPES,
		                            MediaStore.Audio.Media.DISPLAY_NAME);
		if (null != cursor) {
			while (cursor.moveToNext()) {
				String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
				if (null == path) {
					continue;
				}
				if (mIsPublic && path.startsWith(PathUtils.getExternalAppFilesPath() + File.separator + PathConfig.AUDIO)) {
					continue;
				}
				if (!mIsPublic && !path.startsWith(PathUtils.getExternalAppFilesPath() + File.separator + PathConfig.AUDIO)) {
					continue;
				}
				String displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));
				if (null == displayName) {
					displayName = StringUtils.getString(R.string.unknown);
				}
				int _id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
				String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
				if (null == artist) {
					artist = StringUtils.getString(R.string.unknown);
				}
				String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
				if (null == album) {
					album = StringUtils.getString(R.string.unknown);
				}
				int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
				if (duration < 0) {
					duration = 0;
				}
				audioList.add(new AudioEntity(_id, displayName, artist, album, duration, path));
			}
			cursor.close();
		}
		LitePal.deleteAll(AudioEntity.class);
		if (!audioList.isEmpty()) {
			LitePal.saveAll(audioList);
		}
		return audioList;
	}

	@BusUtils.Bus(tag = BusConfig.MEDIA_MOUNTED, threadMode = BusUtils.ThreadMode.MAIN)
	public void onMediaMounted() {
		ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<List<AudioEntity>>() {
			@Override
			public List<AudioEntity> doInBackground() {
				return scanAndSaveAudioList();
			}

			@Override
			public void onSuccess(List<AudioEntity> result) {
				queryCompleted(result);
			}
		});
	}
}