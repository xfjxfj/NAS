package com.viegre.nas.speaker.popup;

import android.content.Context;

import com.lxj.xpopup.core.CenterPopupView;
import com.viegre.nas.speaker.R;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;

/**
 * Created by Djangoogle on 2020/11/30 10:06 with Android Studio.
 */
public class WLANPasswordPopup extends CenterPopupView {

	private String mSSID;

	public WLANPasswordPopup(@NonNull Context context) {
		super(context);
	}

	public WLANPasswordPopup(@NonNull Context context, String SSID) {
		super(context);
		mSSID = SSID;
	}

	@Override
	protected int getImplLayoutId() {
		return R.layout.popup_wlan_password;
	}

	@Override
	protected void onCreate() {
		super.onCreate();
		((AppCompatTextView) findViewById(R.id.actvPopupWLANName)).setText(mSSID);
		findViewById(R.id.actvPopupWLANConfirm).setOnClickListener(view -> {
			onConfirmClick();
		});
		findViewById(R.id.actvPopupWLANCancel).setOnClickListener(view -> dismiss());
	}

	private void onConfirmClick() {

	}
}
