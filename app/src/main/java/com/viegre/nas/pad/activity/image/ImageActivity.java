package com.viegre.nas.pad.activity.image;

import android.database.Cursor;
import android.provider.MediaStore;

import com.blankj.utilcode.util.ThreadUtils;
import com.djangoogle.framework.activity.BaseActivity;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.adapter.ImageListAdapter;
import com.viegre.nas.pad.databinding.ActivityImageBinding;
import com.viegre.nas.pad.entity.ImageEntity;
import com.viegre.nas.pad.manager.TextStyleManager;
import com.viegre.nas.pad.widget.GridSpaceItemDecoration;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.SimpleItemAnimator;

/**
 * Created by レインマン on 2021/01/18 16:36 with Android Studio.
 */
public class ImageActivity extends BaseActivity<ActivityImageBinding> {

	private volatile boolean mIsPublic = true;
	private ImageListAdapter mImageListAdapter;

	@Override
	protected void initialize() {
		mViewBinding.iImageTitle.actvFileManagerTitle.setText(R.string.image);
		mViewBinding.iImageTitle.llcFileManagerTitleBack.setOnClickListener(view -> finish());
		initRadioGroup();
		initList();
	}

	private void initRadioGroup() {
		mViewBinding.rgImageTag.setOnCheckedChangeListener((radioGroup, i) -> {
			if (R.id.acrbImageTagPrivate == i) {
				mIsPublic = false;
			} else if (R.id.acrbImageTagPublic == i) {
				mIsPublic = true;
			}
		});
		TextStyleManager.INSTANCE.setFileManagerTag(mViewBinding.acrbImageTagPrivate, mViewBinding.acrbImageTagPublic);
	}

	private void initList() {
		mImageListAdapter = new ImageListAdapter();
		mViewBinding.rvImageList.setLayoutManager(new GridLayoutManager(this, 4));
		mViewBinding.rvImageList.addItemDecoration(new GridSpaceItemDecoration(4, 12, 12));
		mViewBinding.rvImageList.setAdapter(mImageListAdapter);
		//禁用动画以防止更新列表时产生闪烁
		SimpleItemAnimator simpleItemAnimator = (SimpleItemAnimator) mViewBinding.rvImageList.getItemAnimator();
		if (null != simpleItemAnimator) {
			simpleItemAnimator.setSupportsChangeAnimations(false);
		}
		mViewBinding.srlImageRefresh.setColorSchemeResources(R.color.settings_menu_selected_bg);
		mViewBinding.srlImageRefresh.setProgressBackgroundColorSchemeResource(R.color.file_manager_tag_unpressed);
		mViewBinding.srlImageRefresh.setOnRefreshListener(this::getImageList);
		mViewBinding.srlImageRefresh.setRefreshing(true);
		getImageList();
	}

	private void getImageList() {
		ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<List<ImageEntity>>() {
			@Override
			public List<ImageEntity> doInBackground() {
				List<ImageEntity> imageList = new ArrayList<>();
				Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
				                                           new String[]{MediaStore.Images.ImageColumns.DATA, MediaStore.Images.ImageColumns.DISPLAY_NAME, MediaStore.Images.ImageColumns.DATE_ADDED},
				                                           null,
				                                           null,
				                                           MediaStore.Images.Media.DATE_MODIFIED + " desc");

				if (null != cursor) {
					while (cursor.moveToNext()) {
						String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA));
						if (null == path) {
							continue;
						}
//						String fileName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
//						String size = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE));
//						String date = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED));
						imageList.add(new ImageEntity(path));
					}
					cursor.close();
				}
				return imageList;
			}

			@Override
			public void onSuccess(List<ImageEntity> result) {
				mImageListAdapter.setList(result);
				mViewBinding.srlImageRefresh.setRefreshing(false);
			}
		});
	}
}
