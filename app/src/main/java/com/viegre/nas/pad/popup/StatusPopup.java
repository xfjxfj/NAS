package com.viegre.nas.pad.popup;

import android.content.Context;

import com.lxj.xpopup.impl.FullScreenPopupView;
import com.viegre.nas.pad.R;

import org.jetbrains.annotations.NotNull;

import androidx.annotation.NonNull;

/**
 * Created by レインマン on 2021/07/29 14:13 with Android Studio.
 */
public class StatusPopup extends FullScreenPopupView {

	public StatusPopup(@NonNull @NotNull Context context) {
		super(context);
	}

	@Override
	protected int getImplLayoutId() {
		return R.layout.popup_status;
	}
}
