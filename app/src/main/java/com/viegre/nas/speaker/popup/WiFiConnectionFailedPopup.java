package com.viegre.nas.speaker.popup;

import android.content.Context;

import com.lxj.xpopup.core.CenterPopupView;
import com.viegre.nas.speaker.R;

import androidx.annotation.NonNull;

/**
 * Created by Djangoogle on 2020/11/30 10:06 with Android Studio.
 */
public class WiFiConnectionFailedPopup extends CenterPopupView {

	public WiFiConnectionFailedPopup(@NonNull Context context) {
		super(context);
	}

	@Override
	protected int getImplLayoutId() {
		return R.layout.popup_wlan_connection_failed;
	}

	@Override
	protected void onCreate() {
		super.onCreate();
		findViewById(R.id.actvPopupWLANConnectionFailedConfirm).setOnClickListener(view -> dismiss());
	}
}
