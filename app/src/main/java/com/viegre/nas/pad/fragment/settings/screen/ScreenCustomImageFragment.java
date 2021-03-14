package com.viegre.nas.pad.fragment.settings.screen;

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.blankj.utilcode.util.BusUtils;
import com.blankj.utilcode.util.ColorUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.djangoogle.framework.fragment.BaseFragment;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.adapter.ScreenCustomAlbumAdapter;
import com.viegre.nas.pad.adapter.ScreenCustomImageAdapter;
import com.viegre.nas.pad.config.BusConfig;
import com.viegre.nas.pad.databinding.FragmentScreenCustomImageBinding;
import com.viegre.nas.pad.entity.ImageAlbumEntity;
import com.viegre.nas.pad.entity.ImageEntity;
import com.viegre.nas.pad.widget.GridSpaceItemDecoration;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by レインマン on 2021/03/11 11:45 with Android Studio.
 */
public class ScreenCustomImageFragment extends BaseFragment<FragmentScreenCustomImageBinding> {

	private ScreenCustomImageAdapter mScreenCustomImageAdapter;

	@Override
	protected void initialize() {
		mViewBinding.acivScreenCustomBack.setOnClickListener(view -> BusUtils.post(BusConfig.SCREEN_CUSTOM_HIDE));
		mViewBinding.acivScreenCustomList.setOnClickListener(view -> {
			mViewBinding.acivScreenCustomList.setImageResource(R.mipmap.screen_custom_list_checked);
			mViewBinding.acivScreenCustomTiled.setImageResource(R.mipmap.screen_custom_tiled_unchecked);
			mViewBinding.rvScreenCustomAlbumList.setVisibility(View.VISIBLE);
			mViewBinding.rvScreenCustomImageList.setVisibility(View.GONE);
		});
		mViewBinding.acivScreenCustomTiled.setOnClickListener(view -> {
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
			mScreenCustomImageAdapter.getData()
			                         .get(position)
			                         .setCheck(!mScreenCustomImageAdapter.getData().get(position).isCheck());
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
				String[] projection = {MediaStore.Images.Media._ID, MediaStore.Images.Media.BUCKET_ID, MediaStore.Images.Media.BUCKET_DISPLAY_NAME};

				Cursor cursor = mActivity.getContentResolver().query(uri, projection, null, null, null);

				List<String> ids = new ArrayList<>();
				List<ImageAlbumEntity> list = new ArrayList<>();
				if (null != cursor) {
					while (cursor.moveToNext()) {
						ImageAlbumEntity imageAlbumEntity = new ImageAlbumEntity();

						int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID);
						imageAlbumEntity.set_id(cursor.getString(columnIndex));

						if (!ids.contains(imageAlbumEntity.get_id())) {
							columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
							imageAlbumEntity.setName(cursor.getString(columnIndex));

							columnIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
							imageAlbumEntity.setCoverID(cursor.getLong(columnIndex));

							list.add(imageAlbumEntity);
							ids.add(imageAlbumEntity.get_id());
						} else {
							int count = list.get(ids.indexOf(imageAlbumEntity.get_id())).getCount();
							count++;
							list.get(ids.indexOf(imageAlbumEntity.get_id())).setCount(count);
						}
					}
					cursor.close();
				}
				return list;
			}

			@Override
			public void onSuccess(List<ImageAlbumEntity> result) {
				ScreenCustomAlbumAdapter screenCustomAlbumAdapter = new ScreenCustomAlbumAdapter();
				mViewBinding.rvScreenCustomAlbumList.setLayoutManager(new LinearLayoutManager(mActivity));
				mViewBinding.rvScreenCustomAlbumList.addItemDecoration(new HorizontalDividerItemDecoration.Builder(mActivity).color(
						ColorUtils.getColor(R.color.screen_custom_album_list_divider)).size(1).margin(25, 25).build());
				mViewBinding.rvScreenCustomAlbumList.setAdapter(screenCustomAlbumAdapter);
				//禁用动画以防止更新列表时产生闪烁
				SimpleItemAnimator simpleItemAnimator = (SimpleItemAnimator) mViewBinding.rvScreenCustomAlbumList.getItemAnimator();
				if (null != simpleItemAnimator) {
					simpleItemAnimator.setSupportsChangeAnimations(false);
				}
				Iterator<ImageAlbumEntity> iterator = result.iterator();
				while (iterator.hasNext()) {
					if (iterator.next().getCount() < 1) {
						iterator.remove();
					}
				}
				screenCustomAlbumAdapter.setOnItemClickListener((adapter, view, position) -> {
					result.get(position).setCheck(!result.get(position).isCheck());
					screenCustomAlbumAdapter.notifyItemChanged(position);
				});
				screenCustomAlbumAdapter.setList(result);
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
				mScreenCustomImageAdapter.setList(result);
			}
		});
	}
}
