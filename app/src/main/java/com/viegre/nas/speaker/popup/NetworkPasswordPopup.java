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
import com.viegre.nas.speaker.databinding.PopupNetworkPasswordBinding;

import androidx.annotation.NonNull;

/**
 * Created by Djangoogle on 2020/11/30 10:06 with Android Studio.
 */
public class NetworkPasswordPopup extends CenterPopupView {

	private PopupNetworkPasswordBinding mPopupNetworkPasswordBinding;
	private String mSSID;

	public NetworkPasswordPopup(@NonNull Context context) {
		super(context);
	}

	public NetworkPasswordPopup(@NonNull Context context, String SSID) {
		super(context);
		mSSID = SSID;
	}

	@Override
	protected int getImplLayoutId() {
		return R.layout.popup_network_password;
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	protected void onCreate() {
		super.onCreate();
		mPopupNetworkPasswordBinding = PopupNetworkPasswordBinding.bind(getPopupImplView());
		mPopupNetworkPasswordBinding.actvPopupNetworkPasswordName.setText(mSSID);
		mPopupNetworkPasswordBinding.actvPopupNetworkPasswordConfirm.setOnClickListener(view -> onConfirmClick());
		mPopupNetworkPasswordBinding.actvPopupNetworkPasswordCancel.setOnClickListener(view -> dismiss());
		Drawable drawableShow = ResourceUtils.getDrawable(R.mipmap.network_wifi_password_input_show);
		drawableShow.setBounds(0, 0, drawableShow.getMinimumWidth(), drawableShow.getMinimumHeight());
		Drawable drawableHide = ResourceUtils.getDrawable(R.mipmap.network_wifi_password_input_hide);
		drawableHide.setBounds(0, 0, drawableHide.getMinimumWidth(), drawableHide.getMinimumHeight());
		mPopupNetworkPasswordBinding.acetPopupNetworkPasswordInput.setCompoundDrawables(null, null, drawableHide, null);
		mPopupNetworkPasswordBinding.acetPopupNetworkPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
		mPopupNetworkPasswordBinding.acetPopupNetworkPasswordInput.setOnTouchListener((view, motionEvent) -> {
			//如果不是按下事件，不再处理
			if (motionEvent.getAction() != MotionEvent.ACTION_UP) {
				return false;
			}
			Drawable drawable = mPopupNetworkPasswordBinding.acetPopupNetworkPasswordInput.getCompoundDrawables()[2];
			if (motionEvent.getX() > mPopupNetworkPasswordBinding.acetPopupNetworkPasswordInput.getWidth() - mPopupNetworkPasswordBinding.acetPopupNetworkPasswordInput
					.getPaddingRight() - drawable.getIntrinsicWidth()) {
				if (drawableShow == drawable) {
					mPopupNetworkPasswordBinding.acetPopupNetworkPasswordInput.setCompoundDrawables(null, null, drawableHide, null);
					mPopupNetworkPasswordBinding.acetPopupNetworkPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
				} else {
					mPopupNetworkPasswordBinding.acetPopupNetworkPasswordInput.setCompoundDrawables(null, null, drawableShow, null);
					mPopupNetworkPasswordBinding.acetPopupNetworkPasswordInput.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
				}
			}
			return false;
		});
	}

	private void onConfirmClick() {
		String password = String.valueOf(mPopupNetworkPasswordBinding.acetPopupNetworkPasswordInput.getText());
		if (TextUtils.isEmpty(password)) {
			ToastUtils.showShort(R.string.network_please_enter_password);
			return;
		}

		if (password.length() < 8) {
			ToastUtils.showShort(R.string.network_password_must_be_greater_than_8_digits);
			return;
		}

		dismissWith(() -> BusUtils.post(BusConfig.BUS_NETWORK_PASSWORD, password));
	}
}
