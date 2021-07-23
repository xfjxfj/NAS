package com.viegre.nas.pad.activity.video;

import android.content.Intent;
import android.database.Cursor;
import android.provider.MediaStore;
import android.view.View;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.Utils;
import com.djangoogle.framework.activity.BaseActivity;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.activity.LoginActivity;
import com.viegre.nas.pad.adapter.VideoListAdapter;
import com.viegre.nas.pad.config.PathConfig;
import com.viegre.nas.pad.config.SPConfig;
import com.viegre.nas.pad.databinding.ActivityVideoBinding;
import com.viegre.nas.pad.entity.VideoEntity;
import com.viegre.nas.pad.manager.TextStyleManager;
import com.viegre.nas.pad.util.CommonUtils;
import com.viegre.nas.pad.util.MediaScanner;
import com.viegre.nas.pad.widget.GridSpaceItemDecoration;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.StringDef;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.SimpleItemAnimator;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Created by レインマン on 2021/01/18 16:36 with Android Studio.
 */
public class VideoActivity extends BaseActivity<ActivityVideoBinding> {

	private volatile boolean mIsPublic = true;
	private VideoListAdapter mVideoListAdapter;
	private final String mKeywords = "";
	private final String mType = Type.PUBLIC;
	private final String mTime = TIME.ALL;

	@Override
	protected void initialize() {
		mViewBinding.iVideoTitle.actvFileManagerTitle.setText(R.string.video);
		mViewBinding.iVideoTitle.acivFileManagerTitleBack.setOnClickListener(view -> finish());
		initRadioGroup();
		initList();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		login();
	}

	private void initRadioGroup() {
		mViewBinding.rgVideoTag.setOnCheckedChangeListener((radioGroup, i) -> {
			if (R.id.acrbVideoTagPrivate == i) {
				mIsPublic = false;
				loginStatus();
			} else if (R.id.acrbVideoTagPublic == i) {
				mIsPublic = true;
				loginStatus();
			}
			scanMedia();
		});
		mViewBinding.imageConst.setOnClickListener(v -> startActivity(new Intent(mActivity, LoginActivity.class)));
		TextStyleManager.INSTANCE.setFileManagerTagOnCheckedChange(mViewBinding.acrbVideoTagPrivate, mViewBinding.acrbVideoTagPublic);
	}

	private void loginStatus() {
		if (mIsPublic) {
			mViewBinding.srlVideoRefresh.setVisibility(View.VISIBLE);
			mViewBinding.rvVideoList.setVisibility(View.VISIBLE);
			mViewBinding.imageConst.setVisibility(View.GONE);
		} else {
			login();
		}
	}

	private void login() {
		if (SPUtils.getInstance().contains(SPConfig.PHONE)) {
			mViewBinding.srlVideoRefresh.setVisibility(View.VISIBLE);
			mViewBinding.rvVideoList.setVisibility(View.VISIBLE);
			mViewBinding.imageConst.setVisibility(View.GONE);
		} else {
			mViewBinding.srlVideoRefresh.setVisibility(View.GONE);
			mViewBinding.rvVideoList.setVisibility(View.GONE);
			mViewBinding.imageConst.setVisibility(View.VISIBLE);
		}
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
		mViewBinding.srlVideoRefresh.setOnRefreshListener(this::scanMedia);
		mViewBinding.srlVideoRefresh.setRefreshing(true);
		scanMedia();
	}

	private void scanMedia() {
		MediaScanner mediaScanner = new MediaScanner(this::queryVideo);
		mediaScanner.scanFile(new File(mIsPublic ? PathConfig.PUBLIC : PathConfig.PRIVATE));
	}

	private void queryVideo() {
		ThreadUtils.executeBySingle(new ThreadUtils.SimpleTask<List<VideoEntity>>() {
			@Override
			public List<VideoEntity> doInBackground() {
				List<VideoEntity> videoList = new ArrayList<>();
				StringBuilder selection = new StringBuilder();
				List<String> selectionArgList = new ArrayList<>();
				switch (mType) {
					case Type.PUBLIC:
						selection.append(MediaStore.Video.VideoColumns.DATA + " like ? escape '/'");
						selectionArgList.add(CommonUtils.sqliteEscape(PathConfig.PUBLIC) + "%");
						break;

					case Type.PRIVATE:
						selection.append(MediaStore.Video.VideoColumns.DATA + " like ? escape '/'");
						selectionArgList.add(CommonUtils.sqliteEscape(PathConfig.PRIVATE + SPUtils.getInstance()
						                                                                          .getString(SPConfig.PHONE) + File.separator) + "%");
						break;

					default:
						selection.append(MediaStore.Video.VideoColumns.DATA + " like ? escape '/' and " + MediaStore.Video.VideoColumns.DATA + " like ? escape '/'");
						selectionArgList.add(CommonUtils.sqliteEscape(PathConfig.PUBLIC) + "%");
						selectionArgList.add(CommonUtils.sqliteEscape(PathConfig.PRIVATE + SPUtils.getInstance()
						                                                                          .getString(SPConfig.PHONE) + File.separator) + "%");
						break;
				}
				if (!mKeywords.isEmpty()) {
					selection.append(" and " + MediaStore.Video.VideoColumns.DISPLAY_NAME + " like ? escape '/'");
					selectionArgList.add(CommonUtils.sqliteEscape(mKeywords) + "%");
				}
				selection.append(" and " + MediaStore.Video.VideoColumns.DATE_MODIFIED + " > ?");
				long nowTime = System.currentTimeMillis() / 1000 / 60 / 60 / 24;
				switch (mTime) {
					case TIME.DAY_1:
						selectionArgList.add(String.valueOf(nowTime - 1));
						break;

					case TIME.DAY_3:
						selectionArgList.add(String.valueOf(nowTime - 3));
						break;

					case TIME.DAY_7:
						selectionArgList.add(String.valueOf(nowTime - 7));
						break;

					case TIME.MONTH_1:
						selectionArgList.add(String.valueOf(nowTime - 30));
						break;

					case TIME.MONTH_3:
						selectionArgList.add(String.valueOf(nowTime - 90));
						break;

					default:
						selectionArgList.add(String.valueOf(0));
						break;
				}
				Cursor cursor = Utils.getApp()
				                     .getContentResolver()
				                     .query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
				                            new String[]{MediaStore.Video.VideoColumns.DATA, MediaStore.Video.VideoColumns.DISPLAY_NAME, MediaStore.Video.VideoColumns.DATE_MODIFIED},
				                            selection.toString(),
				                            selectionArgList.toArray(new String[0]),
				                            null);
				if (null != cursor) {
					while (cursor.moveToNext()) {
						String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA));
						if (null == path) {
							continue;
						}
						String name = FileUtils.getFileNameNoExtension(path);
						String suffix = FileUtils.getFileExtension(path);
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

	@Retention(SOURCE)
	@Target({PARAMETER})
	@StringDef(value = {Type.ALL, Type.PUBLIC, Type.PRIVATE})
	private @interface Type {
		String ALL = "all";
		String PUBLIC = "public";
		String PRIVATE = "private";
	}

	@Retention(SOURCE)
	@Target({PARAMETER})
	@StringDef(value = {TIME.ALL, TIME.DAY_1, TIME.DAY_3, TIME.DAY_7, TIME.MONTH_1, TIME.MONTH_3})
	private @interface TIME {
		String ALL = "all";
		String DAY_1 = "day1";
		String DAY_3 = "day3";
		String DAY_7 = "day7";
		String MONTH_1 = "month1";
		String MONTH_3 = "month3";
	}
}
