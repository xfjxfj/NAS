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

import java.util.ArrayList;

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

		ArrayList<String> ids = new ArrayList<String>();
		mAlbumsList.clear();
		if (cursor != null) {
			while (cursor.moveToNext()) {
				Album album = new Album();

				int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID);
				album.id = cursor.getString(columnIndex);

				if (!ids.contains(album.id)) {
					columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
					album.name = cursor.getString(columnIndex);

					columnIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
					album.coverID = cursor.getLong(columnIndex);

					mAlbumsList.add(album);
					ids.add(album.id);
				} else {
					mAlbumsList.get(ids.indexOf(album.id)).count++;
				}
			}
			cursor.close();
		}
	}
