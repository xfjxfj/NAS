package com.viegre.nas.speaker.popup;

import android.content.Context;

import com.lxj.xpopup.core.CenterPopupView;
import com.viegre.nas.speaker.R;
import com.viegre.nas.speaker.databinding.PopupLoginTimeBinding;
import com.viegre.nas.speaker.impl.PopupClickListener;

import androidx.annotation.NonNull;

/**
 * Created by Djangoogle on 2021/01/08 16:03 with Android Studio.
 */
public class LoginTimePopup extends CenterPopupView {

	private PopupClickListener mPopupClickListener;

	public LoginTimePopup(@NonNull Context context) {
		super(context);
	}

	public LoginTimePopup(@NonNull Context context, PopupClickListener popupClickListener) {
		super(context);
		mPopupClickListener = popupClickListener;
	}

	@Override
	protected int getImplLayoutId() {
		return R.layout.popup_login_time;
	}

	@Override
	protected void onCreate() {
		super.onCreate();
		PopupLoginTimeBinding popupLoginTimeBinding = PopupLoginTimeBinding.bind(getPopupImplView());
		popupLoginTimeBinding.actvPopupLoginTimeCancel.setOnClickListener(view -> {
			mPopupClickListener.onCancel();
			dismiss();
		});
		popupLoginTimeBinding.actvPopupLoginTimeConfirm.setOnClickListener(view -> {
			mPopupClickListener.onConfirm();
			dismiss();
		});
	}
}
