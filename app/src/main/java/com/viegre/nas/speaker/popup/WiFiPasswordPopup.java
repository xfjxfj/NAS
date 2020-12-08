package com.viegre.nas.speaker.popup;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.InputType;
import android.text.TextUtils;
import android.view.MotionEvent;

import com.blankj.utilcode.util.BusUtils;
import com.blankj.utilcode.util.ResourceUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.lxj.xpopup.core.CenterPopupView;
import com.viegre.nas.speaker.R;
import com.viegre.nas.speaker.config.BusConfig;
import com.viegre.nas.speaker.databinding.PopupWlanPasswordBinding;

import androidx.annotation.NonNull;

/**
 * Created by Djangoogle on 2020/11/30 10:06 with Android Studio.
 */
public class WiFiPasswordPopup extends CenterPopupView {

	private PopupWlanPasswordBinding mPopupWlanPasswordBinding;
	private String mSSID;

	public WiFiPasswordPopup(@NonNull Context context) {
		super(context);
	}

	public WiFiPasswordPopup(@NonNull Context context, String SSID) {
		super(context);
		mSSID = SSID;
	}

	@Override
	protected int getImplLayoutId() {
		return R.layout.popup_wlan_password;
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	protected void onCreate() {
		super.onCreate();
		mPopupWlanPasswordBinding = PopupWlanPasswordBinding.bind(getPopupImplView());
		mPopupWlanPasswordBinding.actvPopupWLANName.setText(mSSID);
		mPopupWlanPasswordBinding.actvPopupWLANConfirm.setOnClickListener(view -> onConfirmClick());
		mPopupWlanPasswordBinding.actvPopupWLANCancel.setOnClickListener(view -> dismiss());
		Drawable drawableShow = ResourceUtils.getDrawable(R.mipmap.wifi_password_input_show);
		drawableShow.setBounds(0, 0, drawableShow.getMinimumWidth(), drawableShow.getMinimumHeight());
		Drawable drawableHide = ResourceUtils.getDrawable(R.mipmap.wifi_password_input_hide);
		drawableHide.setBounds(0, 0, drawableHide.getMinimumWidth(), drawableHide.getMinimumHeight());
		mPopupWlanPasswordBinding.acetPopupWLANInput.setCompoundDrawables(null, null, drawableHide, null);
		mPopupWlanPasswordBinding.acetPopupWLANInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
		mPopupWlanPasswordBinding.acetPopupWLANInput.setOnTouchListener((view, motionEvent) -> {
			//如果不是按下事件，不再处理
			if (motionEvent.getAction() != MotionEvent.ACTION_UP) {
				return false;
			}
			Drawable drawable = mPopupWlanPasswordBinding.acetPopupWLANInput.getCompoundDrawables()[2];
			if (motionEvent.getX() > mPopupWlanPasswordBinding.acetPopupWLANInput.getWidth() - mPopupWlanPasswordBinding.acetPopupWLANInput.getPaddingRight() - drawable.getIntrinsicWidth()) {
				if (drawableShow == drawable) {
					mPopupWlanPasswordBinding.acetPopupWLANInput.setCompoundDrawables(null, null, drawableHide, null);
					mPopupWlanPasswordBinding.acetPopupWLANInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
				} else {
					mPopupWlanPasswordBinding.acetPopupWLANInput.setCompoundDrawables(null, null, drawableShow, null);
					mPopupWlanPasswordBinding.acetPopupWLANInput.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
				}
			}
			return false;
		});
	}

	private void onConfirmClick() {
		String password = String.valueOf(mPopupWlanPasswordBinding.acetPopupWLANInput.getText());
		if (TextUtils.isEmpty(password)) {
			ToastUtils.showShort(R.string.please_enter_password);
			return;
		}

		if (password.length() < 8) {
			ToastUtils.showShort(R.string.password_must_be_greater_than_8_digits);
			return;
		}

		dismissWith(() -> BusUtils.post(BusConfig.BUS_WLAN_PASSWORD, password));
	}
}
