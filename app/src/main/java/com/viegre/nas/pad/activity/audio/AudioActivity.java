package com.viegre.nas.pad.activity.audio;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.djangoogle.framework.activity.BaseActivity;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.adapter.AudioListAdapter;
import com.viegre.nas.pad.config.BusConfig;
import com.viegre.nas.pad.config.PathConfig;
import com.viegre.nas.pad.databinding.ActivityAudioBinding;
import com.viegre.nas.pad.entity.AudioEntity;
import com.viegre.nas.pad.manager.AudioPlayListManager;
import com.viegre.nas.pad.manager.MediaScannerManager;
import com.viegre.nas.pad.manager.TextStyleManager;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.SimpleItemAnimator;
import nl.changer.audiowife.AudioWife;

/**
 * 音频管理页
 * Created by レインマン on 2021/01/18 16:36 with Android Studio.
 */
public class  	AudioActivity extends BaseActivity<ActivityAudioBinding> {

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
	protected void onResume() {
		super.onResume();
		onPlayListUpdate(BusConfig.UPDATE_AUDIO_PLAY_LIST);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		ThreadUtils.cancel(ThreadUtils.getSinglePool());
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
		TextStyleManager.INSTANCE.setFileManagerTagOnCheckedChange(mViewBinding.acrbAudioTagPrivate, mViewBinding.acrbAudioTagPublic);
	}

	private void initList() {
		mAudioListAdapter = new AudioListAdapter();
		mAudioListAdapter.setOnItemClickListener((adapter, view, position) -> {
			if (AudioPlayListManager.INSTANCE.getPosition() < 0) {
				AudioPlayListManager.INSTANCE.setPosition(position);
			}
			Intent intent = new Intent(this, AudioPlayerActivity.class);
			intent.putExtra("position", position);
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
		scanMedia();
	}

	private void queryAudioByLitepal() {
		mViewBinding.acrbAudioTagPrivate.setEnabled(false);
		mViewBinding.acrbAudioTagPublic.setEnabled(false);
		mViewBinding.rvAudioList.setEnabled(false);
		ThreadUtils.executeBySingle(new ThreadUtils.SimpleTask<List<AudioEntity>>() {
			@Override
			public List<AudioEntity> doInBackground() {
				return LitePal.where("path like ?", (mIsPublic ? PathConfig.PUBLIC : PathConfig.PRIVATE) + "%")
				              .order("name desc")
				              .find(AudioEntity.class);
			}

			@Override
			public void onSuccess(List<AudioEntity> result) {
				if (result.isEmpty()) {
					scanMedia();
				} else {
					queryCompleted(result);
				}
			}
		});
	}

	private void queryCompleted(List<AudioEntity> audioList) {
		AudioWife.getInstance().release();
		AudioPlayListManager.INSTANCE.getList().clear();
		AudioPlayListManager.INSTANCE.getList().addAll(audioList);
		mAudioListAdapter.setList(audioList);
		mViewBinding.srlAudioRefresh.setRefreshing(false);
		mViewBinding.acrbAudioTagPrivate.setEnabled(true);
		mViewBinding.acrbAudioTagPublic.setEnabled(true);
		mViewBinding.rvAudioList.setEnabled(true);
	}

	private void scanMedia() {
		MediaScannerManager.INSTANCE.scan(this::scanAndSaveAudioList, mIsPublic ? PathConfig.PUBLIC : PathConfig.PRIVATE);
	}

	private void scanAndSaveAudioList() {
		ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<List<AudioEntity>>() {
			@Override
			public List<AudioEntity> doInBackground() {
				List<AudioEntity> audioList = new ArrayList<>();
//				Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
//				                                           new String[]{MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media._ID, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.DURATION},
//				                                           MediaStore.Audio.Media.DATA + " like ?",
//				                                           new String[]{(mIsPublic ? PathConfig.PUBLIC : PathConfig.PRIVATE) + "%"},
//				                                           MediaStore.Audio.Media.DISPLAY_NAME + " desc");

				Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
				if (null != cursor) {
					while (cursor.moveToNext()) {
						String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
						if (null == path) {
							continue;
						}
//				if (mIsPublic && path.startsWith(PathConfig.AUDIO.PUB)) {
//					continue;
//				}
//				if (!mIsPublic && !path.startsWith(PathConfig.AUDIO.PRI)) {
//					continue;
//				}
						String name;
						String suffix;
						String displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));
						if (null == displayName) {
							name = suffix = StringUtils.getString(R.string.unknown);
						} else {
							name = FileUtils.getFileNameNoExtension(displayName);
							suffix = FileUtils.getFileExtension(displayName);
						}
						int _id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
						String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
						if (null == artist) {
							artist = StringUtils.getString(R.string.unknown);
						}
						String albumName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
						if (null == albumName) {
							albumName = StringUtils.getString(R.string.unknown);
						}
						int albumId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ID));
						String albumImage = getAlbumImage(albumId);
						int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
						if (duration < 0) {
							duration = 0;
						}
						audioList.add(new AudioEntity(_id, name, suffix, artist, albumName, albumImage, duration, path, false));
					}
					cursor.close();
				}
				LitePal.deleteAll(AudioEntity.class);
				if (!audioList.isEmpty()) {
					LitePal.saveAll(audioList);
				}
				return audioList;
			}

			@Override
			public void onSuccess(List<AudioEntity> result) {
				queryCompleted(result);
			}
		});
	}

	private String getAlbumImage(int albumId) {
		String uriAlbums = "content://media/external/audio/albums/" + albumId;
		String[] projection = new String[]{"album_art"};
		Cursor cursor = getContentResolver().query(Uri.parse(uriAlbums), projection, null, null, null);
		String albumImage = null;
		if (null != cursor && cursor.getCount() > 0 && cursor.getColumnCount() > 0) {
			cursor.moveToNext();
			albumImage = cursor.getString(0);
			cursor.close();
		}
		return albumImage;
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onPlayListUpdate(String event) {
		if (!BusConfig.UPDATE_AUDIO_PLAY_LIST.equals(event)) {
			return;
		}
		if (null != mAudioListAdapter) {
			int position = AudioPlayListManager.INSTANCE.getPosition();
			int previousPosition = AudioPlayListManager.INSTANCE.getPreviousPosition();
			if (position < 0) {
				return;
			}
			if (mAudioListAdapter.getData().isEmpty()) {
				return;
			}
			if (position == previousPosition) {
				mAudioListAdapter.getData().get(position).setChecked(true);
				mAudioListAdapter.notifyItemChanged(position);
				return;
			}
			if (position >= 0) {
				mAudioListAdapter.getData().get(position).setChecked(true);
				mAudioListAdapter.notifyItemChanged(position);
			}
			if (previousPosition >= 0) {
				mAudioListAdapter.getData().get(previousPosition).setChecked(false);
				mAudioListAdapter.notifyItemChanged(previousPosition);
			}
		}
	}
}
