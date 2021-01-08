package com.viegre.nas.speaker.popup;

import android.content.Context;

import com.blankj.utilcode.util.StringUtils;
import com.lxj.xpopup.core.CenterPopupView;
import com.viegre.nas.speaker.R;
import com.viegre.nas.speaker.databinding.PopupPromptBinding;
import com.viegre.nas.speaker.impl.PopupClickListener;

import androidx.annotation.NonNull;

/**
 * Created by Djangoogle on 2021/01/08 16:03 with Android Studio.
 */
public class PromptPopup extends CenterPopupView {

	private String mTitle;
	private String mContent;
	private PopupClickListener mPopupClickListener;

	public PromptPopup(@NonNull Context context) {
		super(context);
	}

	public PromptPopup(@NonNull Context context, String title, String content, PopupClickListener popupClickListener) {
		super(context);
		mTitle = title;
		mContent = content;
		mPopupClickListener = popupClickListener;
	}

	public PromptPopup(@NonNull Context context, int titleRedId, int contentResId, PopupClickListener popupClickListener) {
		super(context);
		mTitle = StringUtils.getString(titleRedId);
		mContent = StringUtils.getString(contentResId);
		mPopupClickListener = popupClickListener;
	}

	@Override
	protected int getImplLayoutId() {
		return R.layout.popup_prompt;
	}

	@Override
	protected void onCreate() {
		super.onCreate();
		PopupPromptBinding popupPromptBinding = PopupPromptBinding.bind(getPopupImplView());
		popupPromptBinding.actvPopupPromptTitle.setText(mTitle);
		popupPromptBinding.actvPopupPromptContent.setText(mContent);
		popupPromptBinding.actvPopupPromptCancel.setOnClickListener(view -> {
			mPopupClickListener.onCancel();
			dismiss();
		});
		popupPromptBinding.actvPopupPromptConfirm.setOnClickListener(view -> {
			mPopupClickListener.onConfirm();
			dismiss();
		});
	}
}
