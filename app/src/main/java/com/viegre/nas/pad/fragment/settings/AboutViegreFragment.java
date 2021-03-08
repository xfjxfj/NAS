package com.viegre.nas.pad.fragment.settings;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.StringUtils;
import com.djangoogle.framework.fragment.BaseFragment;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.databinding.FragmentAboutViegreBinding;

/**
 * Created by レインマン on 2020/12/17 17:39 with Android Studio.
 */
public class AboutViegreFragment extends BaseFragment<FragmentAboutViegreBinding> {

	@Override
	protected void initialize() {
		mViewBinding.actvAboutViegreVersion.setText(StringUtils.getString(R.string.about_viegre_version) + AppUtils.getAppVersionName());
	}

	public static AboutViegreFragment newInstance() {
		return new AboutViegreFragment();
	}
}
