package com.viegre.nas.pad.fragment.settings.screen;

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;

import com.blankj.utilcode.util.BusUtils;
import com.blankj.utilcode.util.ColorUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.djangoogle.framework.fragment.BaseFragment;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.adapter.ScreenCustomAlbumAdapter;
import com.viegre.nas.pad.config.BusConfig;
import com.viegre.nas.pad.databinding.FragmentScreenCustomImageBinding;
import com.viegre.nas.pad.entity.ImageAlbumEntity;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;

/**
 * Created by レインマン on 2021/03/11 11:45 with Android Studio.
 */
public class ScreenCustomImageFragment extends BaseFragment<FragmentScreenCustomImageBinding> {

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
				mViewBinding.rvScreenCustomAlbumList.addItemDecoration(new HorizontalDividerItemDecoration.Builder(mActivity).color(ColorUtils.getColor(
						R.color.screen_custom_album_list_divider)).size(1).margin(25, 25).build());
				mViewBinding.rvScreenCustomAlbumList.setAdapter(screenCustomAlbumAdapter);
				screenCustomAlbumAdapter.setList(result);
			}
		});
	}
}
