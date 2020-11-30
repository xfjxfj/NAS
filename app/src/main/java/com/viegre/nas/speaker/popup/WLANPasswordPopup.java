package com.viegre.nas.speaker.popup;

import android.content.Context;
import android.text.TextUtils;

import com.blankj.utilcode.util.BusUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.lxj.xpopup.core.CenterPopupView;
import com.viegre.nas.speaker.R;
import com.viegre.nas.speaker.config.BusConfig;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;

/**
 * Created by Djangoogle on 2020/11/30 10:06 with Android Studio.
 */
public class WLANPasswordPopup extends CenterPopupView {

	private String mSSID;
	private AppCompatEditText mAcetPopupWLANInput;

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
		mAcetPopupWLANInput = findViewById(R.id.acetPopupWLANInput);
		((AppCompatTextView) findViewById(R.id.actvPopupWLANName)).setText(mSSID);
		findViewById(R.id.actvPopupWLANConfirm).setOnClickListener(view -> onConfirmClick());
		findViewById(R.id.actvPopupWLANCancel).setOnClickListener(view -> dismiss());
	}

	private void onConfirmClick() {
		String password = String.valueOf(mAcetPopupWLANInput.getText());
		if (TextUtils.isEmpty(password)) {
			ToastUtils.showShort(R.string.please_enter_password);
			return;
		}

		if (password.length() < 8) {
			ToastUtils.showShort(R.string.please_enter_password);
			return;
		}

		dismissWith(() -> BusUtils.post(BusConfig.BUS_WLAN_PASSWORD, password));
	}
}
