package com.viegre.nas.speaker.popup;

import android.content.Context;

import androidx.annotation.NonNull;

import com.lxj.xpopup.core.CenterPopupView;
import com.viegre.nas.speaker.R;
import com.viegre.nas.speaker.databinding.PopupNetworkConnectionFailedBinding;

/**
 * Created by レインマン on 2020/11/30 10:06 with Android Studio.
 */
public class NetworkConnectionFailedPopup extends CenterPopupView {

	public NetworkConnectionFailedPopup(@NonNull Context context) {
		super(context);
	}

	@Override
	protected int getImplLayoutId() {
		return R.layout.popup_network_connection_failed;
	}

	@Override
	protected void onCreate() {
		super.onCreate();
		PopupNetworkConnectionFailedBinding popupNetworkConnectionFailedBinding = PopupNetworkConnectionFailedBinding.bind(getPopupImplView());
		popupNetworkConnectionFailedBinding.actvPopupNetworkConnectionFailedConfirm.setOnClickListener(view -> dismiss());
	}
}
