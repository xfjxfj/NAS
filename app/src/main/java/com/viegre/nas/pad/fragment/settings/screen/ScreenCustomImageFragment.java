package com.viegre.nas.pad.fragment.settings.screen;

import android.view.View;

import com.blankj.utilcode.util.BusUtils;
import com.djangoogle.framework.fragment.BaseFragment;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.config.BusConfig;
import com.viegre.nas.pad.databinding.FragmentScreenCustomImageBinding;

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
}
