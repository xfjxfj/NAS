package com.viegre.nas.pad.activity;

import com.djangoogle.framework.activity.BaseActivity;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.databinding.ActivityAudioBinding;
import com.viegre.nas.pad.manager.RadioButtonManager;

/**
 * 音频管理页
 * Created by レインマン on 2021/01/18 16:36 with Android Studio.
 */
public class AudioActivity extends BaseActivity<ActivityAudioBinding> {

	@Override
	protected void initialize() {
		initRadioGroup();
	}

	private void initRadioGroup() {
		mViewBinding.rgAudioTag.setOnCheckedChangeListener((radioGroup, i) -> {
			if (R.id.acrbAudioTagPrivate == i) {

			} else if (R.id.acrbAudioTagPublic == i) {

			}
		});
		RadioButtonManager.INSTANCE.setFileManagerTag(mViewBinding.acrbAudioTagPrivate);
		RadioButtonManager.INSTANCE.setFileManagerTag(mViewBinding.acrbAudioTagPublic);
	}
}
