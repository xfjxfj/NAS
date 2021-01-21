package com.viegre.nas.pad.activity;

import com.djangoogle.framework.activity.BaseActivity;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.databinding.ActivityVideoBinding;
import com.viegre.nas.pad.manager.TextStyleManager;

/**
 * Created by レインマン on 2021/01/18 16:36 with Android Studio.
 */
public class VideoActivity extends BaseActivity<ActivityVideoBinding> {

	private volatile boolean mIsPublic = true;

	@Override
	protected void initialize() {
		mViewBinding.iVideoTitle.actvFileManagerTitle.setText(R.string.video);
		mViewBinding.iVideoTitle.llcFileManagerTitleBack.setOnClickListener(view -> finish());
		initRadioGroup();
	}

	private void initRadioGroup() {
		mViewBinding.rgVideoTag.setOnCheckedChangeListener((radioGroup, i) -> {
			if (R.id.acrbVideoTagPrivate == i) {
				mIsPublic = false;
			} else if (R.id.acrbVideoTagPublic == i) {
				mIsPublic = true;
			}
		});
		TextStyleManager.INSTANCE.setFileManagerTag(mViewBinding.acrbVideoTagPrivate, mViewBinding.acrbVideoTagPublic);
	}
}
