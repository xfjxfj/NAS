package com.viegre.nas.pad.popup;

import android.content.Context;
import android.graphics.Color;

import com.blankj.utilcode.util.SPUtils;
import com.lxj.xpopup.core.CenterPopupView;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.config.SPConfig;
import com.viegre.nas.pad.databinding.PopupWakeupKeywordsSelectBinding;

import org.jetbrains.annotations.NotNull;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

/**
 * Created by レインマン on 2021/07/08 16:18 with Android Studio.
 */
public class WakeupKeywordsSelectPopup extends CenterPopupView {

	private PopupWakeupKeywordsSelectBinding mPopupWakeupKeywordsSelectBinding;
	private final Context mContext;
	private int mIndex = SPUtils.getInstance().getInt(SPConfig.WAKEUP_KEYWORDS_INDEX, 0);

	public WakeupKeywordsSelectPopup(@NonNull @NotNull Context context) {
		super(context);
		mContext = context;
	}

	@Override
	protected int getImplLayoutId() {
		return R.layout.popup_wakeup_keywords_select;
	}

	@Override
	protected void onCreate() {
		super.onCreate();
		mPopupWakeupKeywordsSelectBinding = PopupWakeupKeywordsSelectBinding.bind(getPopupImplView());
		mPopupWakeupKeywordsSelectBinding.actvPopupIntelligentVoiceKeyword1.setOnClickListener(view -> {
			mIndex = 0;
			selectKeywords();
		});
		mPopupWakeupKeywordsSelectBinding.actvPopupIntelligentVoiceKeyword2.setOnClickListener(view -> {
			mIndex = 1;
			selectKeywords();
		});
		mPopupWakeupKeywordsSelectBinding.actvPopupIntelligentVoiceKeyword3.setOnClickListener(view -> {
			mIndex = 2;
			selectKeywords();
		});
		mPopupWakeupKeywordsSelectBinding.actvPopupIntelligentVoiceCancel.setOnClickListener(view -> dismiss());
		mPopupWakeupKeywordsSelectBinding.actvPopupIntelligentVoiceConfirm.setOnClickListener(view -> dismissWith(() -> SPUtils.getInstance()
		                                                                                                                       .put(SPConfig.WAKEUP_KEYWORDS_INDEX,
		                                                                                                                            mIndex)));
		selectKeywords();
	}

	private void selectKeywords() {
		switch (mIndex) {
			case 0:
				mPopupWakeupKeywordsSelectBinding.actvPopupIntelligentVoiceKeyword1.setBackgroundColor(ContextCompat.getColor(mContext,
				                                                                                                              R.color.popup_intelligent_voice_keyword_bg));
				mPopupWakeupKeywordsSelectBinding.actvPopupIntelligentVoiceKeyword2.setBackgroundColor(Color.TRANSPARENT);
				mPopupWakeupKeywordsSelectBinding.actvPopupIntelligentVoiceKeyword3.setBackgroundColor(Color.TRANSPARENT);
				break;

			case 1:
				mPopupWakeupKeywordsSelectBinding.actvPopupIntelligentVoiceKeyword1.setBackgroundColor(Color.TRANSPARENT);
				mPopupWakeupKeywordsSelectBinding.actvPopupIntelligentVoiceKeyword2.setBackgroundColor(ContextCompat.getColor(mContext,
				                                                                                                              R.color.popup_intelligent_voice_keyword_bg));
				mPopupWakeupKeywordsSelectBinding.actvPopupIntelligentVoiceKeyword3.setBackgroundColor(Color.TRANSPARENT);
				break;

			case 2:
				mPopupWakeupKeywordsSelectBinding.actvPopupIntelligentVoiceKeyword1.setBackgroundColor(Color.TRANSPARENT);
				mPopupWakeupKeywordsSelectBinding.actvPopupIntelligentVoiceKeyword2.setBackgroundColor(Color.TRANSPARENT);
				mPopupWakeupKeywordsSelectBinding.actvPopupIntelligentVoiceKeyword3.setBackgroundColor(ContextCompat.getColor(mContext,
				                                                                                                              R.color.popup_intelligent_voice_keyword_bg));
				break;

			default:
				break;
		}
	}
}
