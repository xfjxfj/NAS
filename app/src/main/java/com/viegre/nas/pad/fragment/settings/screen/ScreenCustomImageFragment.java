package com.viegre.nas.pad.fragment.settings.screen;

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;

import com.blankj.utilcode.util.ColorUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.djangoogle.framework.fragment.BaseFragment;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.adapter.ScreenCustomAlbumAdapter;
import com.viegre.nas.pad.adapter.ScreenCustomImageAdapter;
import com.viegre.nas.pad.config.BusConfig;
import com.viegre.nas.pad.config.SPConfig;
import com.viegre.nas.pad.databinding.FragmentScreenCustomImageBinding;
import com.viegre.nas.pad.entity.ImageAlbumEntity;
import com.viegre.nas.pad.entity.ImageEntity;
import com.viegre.nas.pad.task.VoidTask;
import com.viegre.nas.pad.widget.GridSpaceItemDecoration;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.SimpleItemAnimator;

/**
 * Created by レインマン on 2021/03/11 11:45 with Android Studio.
 */
public class ScreenCustomImageFragment extends BaseFragment<FragmentScreenCustomImageBinding> {

	//0、相册 1、图片
	private int mode = 0;
	private ScreenCustomAlbumAdapter mScreenCustomAlbumAdapter;
	private ScreenCustomImageAdapter mScreenCustomImageAdapter;

	private final Set<String> mScreenCustomSet = new TreeSet<>();

	@Override
	protected void initialize() {
		mViewBinding.acivScreenCustomBack.setOnClickListener(view -> ThreadUtils.executeByCached(new VoidTask() {
			@Override
			public Void doInBackground() {
				mScreenCustomSet.clear();
				if (0 == mode) {
					for (ImageAlbumEntity album : mScreenCustomAlbumAdapter.getData()) {
						if (!album.getImageSet().isEmpty()) {
							mScreenCustomSet.addAll(album.getImageSet());
						}
					}
				} else {
					for (ImageEntity image : mScreenCustomImageAdapter.getData()) {
						mScreenCustomSet.add(image.getPath());
					}
				}
				if (!mScreenCustomSet.isEmpty()) {
					SPUtils.getInstance().put(SPConfig.SCREEN_SAVER_CUSTOM_IMAGES, mScreenCustomSet);
				} else {
					SPUtils.getInstance().remove(SPConfig.SCREEN_SAVER_CUSTOM_IMAGES);
				}
				EventBus.getDefault().post(BusConfig.SCREEN_CUSTOM_HIDE);
				return null;
			}
		}));
		mViewBinding.acivScreenCustomList.setOnClickListener(view -> {
			mode = 0;
			mViewBinding.acivScreenCustomList.setImageResource(R.mipmap.screen_custom_list_checked);
			mViewBinding.acivScreenCustomTiled.setImageResource(R.mipmap.screen_custom_tiled_unchecked);
			mViewBinding.rvScreenCustomAlbumList.setVisibility(View.VISIBLE);
			mViewBinding.rvScreenCustomImageList.setVisibility(View.GONE);
		});
		mViewBinding.acivScreenCustomTiled.setOnClickListener(view -> {
			mode = 1;
			mViewBinding.acivScreenCustomList.setImageResource(R.mipmap.screen_custom_list_unchecked);
			mViewBinding.acivScreenCustomTiled.setImageResource(R.mipmap.screen_custom_tiled_checked);
			mViewBinding.rvScreenCustomAlbumList.setVisibility(View.GONE);
			mViewBinding.rvScreenCustomImageList.setVisibility(View.VISIBLE);
		});

		initAlbum();
		initImage();
	}

	public static ScreenCustomImageFragment newInstance() {
		return new ScreenCustomImageFragment();
	}

	private void initAlbum() {
		getAlbumList();
	}

	private void initImage() {
		mScreenCustomImageAdapter = new ScreenCustomImageAdapter();
		mViewBinding.rvScreenCustomImageList.setLayoutManager(new GridLayoutManager(mActivity, 4));
		mViewBinding.rvScreenCustomImageList.addItemDecoration(new GridSpaceItemDecoration(4, 12, 12));
		mScreenCustomImageAdapter.setOnItemClickListener((adapter, view, position) -> {
			mScreenCustomImageAdapter.getData().get(position).setCheck(!mScreenCustomImageAdapter.getData().get(position).isCheck());
			mScreenCustomImageAdapter.notifyItemChanged(position);
		});
		mViewBinding.rvScreenCustomImageList.setAdapter(mScreenCustomImageAdapter);
		//禁用动画以防止更新列表时产生闪烁
		SimpleItemAnimator simpleItemAnimator = (SimpleItemAnimator) mViewBinding.rvScreenCustomImageList.getItemAnimator();
		if (null != simpleItemAnimator) {
			simpleItemAnimator.setSupportsChangeAnimations(false);
		}
		getImageList();
	}

	private void getAlbumList() {
		ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<List<ImageAlbumEntity>>() {
			@Override
			public List<ImageAlbumEntity> doInBackground() {
				Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				String[] projection = {MediaStore.Images.Media._ID, MediaStore.Images.Media.BUCKET_ID, MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.ImageColumns.DATA};

				Cursor cursor = mActivity.getContentResolver().query(uri, projection, null, null, null);

				List<String> bucketIdList = new ArrayList<>();
				List<ImageAlbumEntity> imageAlbumList = new ArrayList<>();
				if (null != cursor) {
					while (cursor.moveToNext()) {
						ImageAlbumEntity imageAlbumEntity = new ImageAlbumEntity();

						String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA));
						if (null == path) {
							continue;
						}

						int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID);
						imageAlbumEntity.setBucketId(cursor.getString(columnIndex));

						if (!bucketIdList.contains(imageAlbumEntity.getBucketId())) {
							columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
							imageAlbumEntity.setName(cursor.getString(columnIndex));

							columnIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
							imageAlbumEntity.setCoverID(cursor.getLong(columnIndex));

							imageAlbumEntity.getImageSet().add(path);

							imageAlbumList.add(imageAlbumEntity);
							bucketIdList.add(imageAlbumEntity.getBucketId());
						} else {
							imageAlbumList.get(bucketIdList.indexOf(imageAlbumEntity.getBucketId())).getImageSet().add(path);
						}
					}
					cursor.close();
				}
				return imageAlbumList;
			}

			@Override
			public void onSuccess(List<ImageAlbumEntity> result) {
				mScreenCustomAlbumAdapter = new ScreenCustomAlbumAdapter();
				mViewBinding.rvScreenCustomAlbumList.setLayoutManager(new LinearLayoutManager(mActivity));
				mViewBinding.rvScreenCustomAlbumList.addItemDecoration(new HorizontalDividerItemDecoration.Builder(mActivity).color(ColorUtils.getColor(
						R.color.screen_custom_album_list_divider)).size(1).margin(25, 25).build());
				mViewBinding.rvScreenCustomAlbumList.setAdapter(mScreenCustomAlbumAdapter);
				//禁用动画以防止更新列表时产生闪烁
				SimpleItemAnimator simpleItemAnimator = (SimpleItemAnimator) mViewBinding.rvScreenCustomAlbumList.getItemAnimator();
				if (null != simpleItemAnimator) {
					simpleItemAnimator.setSupportsChangeAnimations(false);
				}
				Iterator<ImageAlbumEntity> iterator = result.iterator();
				while (iterator.hasNext()) {
					if (iterator.next().getImageSet().isEmpty()) {
						iterator.remove();
					}
				}
				mScreenCustomAlbumAdapter.setOnItemClickListener((adapter, view, position) -> {
					result.get(position).setCheck(!result.get(position).isCheck());
					mScreenCustomAlbumAdapter.notifyItemChanged(position);
				});
				mScreenCustomAlbumAdapter.setList(result);
			}
		});
	}

	private void getImageList() {
		ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<List<ImageEntity>>() {
			@Override
			public List<ImageEntity> doInBackground() {
				List<ImageEntity> imageList = new ArrayList<>();
				Cursor cursor = mActivity.getContentResolver()
				                         .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
				                                new String[]{MediaStore.Images.ImageColumns.DATA},
				                                null,
				                                null,
				                                null);
				if (null != cursor) {
					while (cursor.moveToNext()) {
						String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA));
						if (null == path || !FileUtils.isFileExists(path)) {
							continue;
						}
						imageList.add(new ImageEntity(path));
					}
					cursor.close();
				}
				return imageList;
			}

			@Override
			public void onSuccess(List<ImageEntity> result) {
				mScreenCustomImageAdapter.setList(result);
			}
		});
	}
}
