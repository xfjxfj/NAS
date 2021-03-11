package com.viegre.nas.pad.fragment.settings.screen;

import android.view.View;

import com.blankj.utilcode.util.BusUtils;
import com.djangoogle.framework.fragment.BaseFragment;
import com.viegre.nas.pad.config.BusConfig;
import com.viegre.nas.pad.databinding.FragmentScreenCustomImageBinding;

/**
 * Created by レインマン on 2021/03/11 11:45 with Android Studio.
 */
public class ScreenCustomImageFragment extends BaseFragment<FragmentScreenCustomImageBinding> {

	@Override
	protected void initialize() {
		mViewBinding.acivScreenCustomBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				BusUtils.post(BusConfig.SCREEN_CUSTOM_HIDE);
			}
		});
	}

	public static ScreenCustomImageFragment newInstance() {
		return new ScreenCustomImageFragment();
	}
}
