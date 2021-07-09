package com.viegre.nas.pad.fragment.settings;

import com.blankj.utilcode.util.SPUtils;
import com.djangoogle.framework.fragment.BaseFragment;
import com.github.iielse.switchbutton.SwitchView;
import com.viegre.nas.pad.config.SPConfig;
import com.viegre.nas.pad.databinding.FragmentIntelligentVoiceBinding;
import com.viegre.nas.pad.manager.PopupManager;
import com.viegre.nas.pad.popup.WakeupKeywordsSelectPopup;

/**
 * Created by レインマン on 2020/12/17 17:37 with Android Studio.
 */
public class IntelligentVoiceFragment extends BaseFragment<FragmentIntelligentVoiceBinding> {

	@Override
	protected void initialize() {
		mViewBinding.rlIntelligentVoiceWakeupKeywords.setOnClickListener(view -> PopupManager.INSTANCE.showCustomXPopup(mContext,
		                                                                                                                new WakeupKeywordsSelectPopup(
				                                                                                                                mContext)));
		mViewBinding.svIntelligentVoiceFeedbackSwitch.setOnStateChangedListener(new SwitchView.OnStateChangedListener() {
			@Override
			public void toggleToOn(SwitchView view) {
				SPUtils.getInstance().put(SPConfig.INTELLIGENT_VOICE_FEEDBACK, true);
				view.toggleSwitch(true);
			}

			@Override
			public void toggleToOff(SwitchView view) {
				SPUtils.getInstance().put(SPConfig.INTELLIGENT_VOICE_FEEDBACK, false);
				view.toggleSwitch(false);
			}
		});
	}

	public static IntelligentVoiceFragment newInstance() {
		return new IntelligentVoiceFragment();
	}
}
