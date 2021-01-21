package com.viegre.nas.pad.activity;

import com.djangoogle.framework.activity.BaseActivity;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.databinding.ActivityImageBinding;
import com.viegre.nas.pad.manager.TextStyleManager;

/**
 * Created by レインマン on 2021/01/18 16:36 with Android Studio.
 */
public class ImageActivity extends BaseActivity<ActivityImageBinding> {

	private volatile boolean mIsPublic = true;

	@Override
	protected void initialize() {
		mViewBinding.iImageTitle.actvFileManagerTitle.setText(R.string.image);
		mViewBinding.iImageTitle.llcFileManagerTitleBack.setOnClickListener(view -> finish());
		initRadioGroup();
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
}
