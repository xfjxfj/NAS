package com.viegre.nas.pad.activity.video;

import android.content.Intent;
import android.database.Cursor;
import android.provider.MediaStore;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.djangoogle.framework.activity.BaseActivity;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.adapter.VideoListAdapter;
import com.viegre.nas.pad.databinding.ActivityVideoBinding;
import com.viegre.nas.pad.entity.VideoEntity;
import com.viegre.nas.pad.manager.TextStyleManager;
import com.viegre.nas.pad.widget.GridSpaceItemDecoration;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.SimpleItemAnimator;

/**
 * Created by レインマン on 2021/01/18 16:36 with Android Studio.
 */
public class VideoActivity extends BaseActivity<ActivityVideoBinding> {

	private volatile boolean mIsPublic = true;
	private VideoListAdapter mVideoListAdapter;

	@Override
	protected void initialize() {
		mViewBinding.iVideoTitle.actvFileManagerTitle.setText(R.string.video);
		mViewBinding.iVideoTitle.llcFileManagerTitleBack.setOnClickListener(view -> finish());
		initRadioGroup();
		initList();
	}

	private void initRadioGroup() {
		mViewBinding.rgVideoTag.setOnCheckedChangeListener((radioGroup, i) -> {
			if (R.id.acrbVideoTagPrivate == i) {
				mIsPublic = false;
			} else if (R.id.acrbVideoTagPublic == i) {
				mIsPublic = true;
			}
		});
		TextStyleManager.INSTANCE.setFileManagerTag(mViewBinding.acrbVideoTagPrivate, mViewBinding.acrbVideoTagPublic);
	}

	private void initList() {
		mVideoListAdapter = new VideoListAdapter();
		mVideoListAdapter.setOnItemClickListener((adapter, view, position) -> {
			Intent intent = new Intent(this, VideoPlayerActivity.class);
			intent.putExtra("video", mVideoListAdapter.getData().get(position));
			ActivityUtils.startActivity(intent);
		});
		mViewBinding.rvVideoList.setLayoutManager(new GridLayoutManager(this, 3));
		mViewBinding.rvVideoList.addItemDecoration(new GridSpaceItemDecoration(3, 12, 12));
		mViewBinding.rvVideoList.setAdapter(mVideoListAdapter);
		//禁用动画以防止更新列表时产生闪烁
		SimpleItemAnimator simpleItemAnimator = (SimpleItemAnimator) mViewBinding.rvVideoList.getItemAnimator();
		if (null != simpleItemAnimator) {
			simpleItemAnimator.setSupportsChangeAnimations(false);
		}
		mViewBinding.srlVideoRefresh.setColorSchemeResources(R.color.settings_menu_selected_bg);
		mViewBinding.srlVideoRefresh.setProgressBackgroundColorSchemeResource(R.color.file_manager_tag_unpressed);
		mViewBinding.srlVideoRefresh.setOnRefreshListener(this::getVideoList);
		mViewBinding.srlVideoRefresh.setRefreshing(true);
		getVideoList();
	}

	private void getVideoList() {
		ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<List<VideoEntity>>() {
			@Override
			public List<VideoEntity> doInBackground() {
				List<VideoEntity> videoList = new ArrayList<>();
				Cursor cursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
				                                           new String[]{MediaStore.Video.VideoColumns.DATA, MediaStore.Video.VideoColumns.DISPLAY_NAME, MediaStore.Video.VideoColumns.DURATION, MediaStore.Video.VideoColumns.DATE_ADDED},
				                                           null,
				                                           null,
				                                           MediaStore.Video.VideoColumns.DISPLAY_NAME);

				if (null != cursor) {
					while (cursor.moveToNext()) {
						String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
						if (null == path) {
							continue;
						}
						String name;
						String suffix;
						String displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME));
						if (null == displayName) {
							name = suffix = StringUtils.getString(R.string.unknown);
						} else {
							name = FileUtils.getFileNameNoExtension(displayName);
							suffix = FileUtils.getFileExtension(displayName);
						}
						videoList.add(new VideoEntity(name, suffix, path));
					}
					cursor.close();
				}
				return videoList;
			}

			@Override
			public void onSuccess(List<VideoEntity> result) {
				mVideoListAdapter.setList(result);
				mViewBinding.srlVideoRefresh.setRefreshing(false);
			}
		});
	}
}
