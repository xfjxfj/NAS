package com.viegre.nas.pad.fragment.settings.screen;

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;

import com.blankj.utilcode.util.BusUtils;
import com.djangoogle.framework.fragment.BaseFragment;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.config.BusConfig;
import com.viegre.nas.pad.databinding.FragmentScreenCustomImageBinding;
import com.viegre.nas.pad.entity.ImageAlbumEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by レインマン on 2021/03/11 11:45 with Android Studio.
 */
public class ScreenCustomImageFragment extends BaseFragment<FragmentScreenCustomImageBinding> {

	@Override
	protected void initialize() {
		getAllPhotoInfo();
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

	}

	private void initImage() {

	}

	/**
	 * 读取手机中所有图片信息
	 */
	private void getAllPhotoInfo() {
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
					list.get(ids.indexOf(imageAlbumEntity.get_id())).count++;
				}
			}
			cursor.close();
		}
	}
}
