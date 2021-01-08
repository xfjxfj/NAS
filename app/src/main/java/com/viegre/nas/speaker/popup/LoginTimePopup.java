package com.viegre.nas.speaker.popup;

import android.content.Context;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatRadioButton;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.SPUtils;
import com.lxj.xpopup.core.CenterPopupView;
import com.viegre.nas.speaker.R;
import com.viegre.nas.speaker.activity.LoginActivity;
import com.viegre.nas.speaker.activity.MainActivity;
import com.viegre.nas.speaker.config.SPConfig;
import com.viegre.nas.speaker.config.UrlConfig;
import com.viegre.nas.speaker.databinding.PopupLoginTimeBinding;
import com.viegre.nas.speaker.entity.LoginEntity;
import com.viegre.nas.speaker.util.CommonUtils;
import com.yanzhenjie.kalle.Kalle;
import com.yanzhenjie.kalle.simple.SimpleCallback;
import com.yanzhenjie.kalle.simple.SimpleResponse;

/**
 * Created by レインマン on 2021/01/08 16:03 with Android Studio.
 */
public class LoginTimePopup extends CenterPopupView {

	private int mHour = 8;

	public LoginTimePopup(@NonNull Context context) {
		super(context);
	}

	@Override
	protected int getImplLayoutId() {
		return R.layout.popup_login_time;
	}

	@Override
	protected void onCreate() {
		super.onCreate();
		PopupLoginTimeBinding popupLoginTimeBinding = PopupLoginTimeBinding.bind(getPopupImplView());
		popupLoginTimeBinding.actvPopupLoginTimeCancel.setVisibility(GONE);
		popupLoginTimeBinding.actvPopupLoginTimeConfirm.setOnClickListener(view -> Kalle.post(UrlConfig.SERVER_URL + UrlConfig.USER + UrlConfig.REFRESH_TOKEN)
		                                                                                .param("hour", mHour)
		                                                                                .perform(new SimpleCallback<LoginEntity>() {
			                                                                                @Override
			                                                                                public void onResponse(SimpleResponse<LoginEntity, String> response) {
				                                                                                dismiss();
				                                                                                if (!response.isSucceed()) {
					                                                                                CommonUtils.showErrorToast(
							                                                                                response.failed());
					                                                                                SPUtils.getInstance()
					                                                                                       .remove(SPConfig.SP_TOKEN);
					                                                                                SPUtils.getInstance()
					                                                                                       .remove(SPConfig.SP_PHONE_NUMBER);
				                                                                                } else {
					                                                                                String token = response.succeed()
					                                                                                                       .getToken();
					                                                                                SPUtils.getInstance()
					                                                                                       .put(SPConfig.SP_TOKEN,
					                                                                                            token);
					                                                                                Kalle.getConfig()
					                                                                                     .getHeaders()
					                                                                                     .set("token", token);
					                                                                                Kalle.setConfig(Kalle.getConfig());
					                                                                                ActivityUtils.startActivity(
							                                                                                MainActivity.class);
					                                                                                ActivityUtils.finishActivity(
							                                                                                LoginActivity.class);
				                                                                                }
			                                                                                }
		                                                                                }));
		popupLoginTimeBinding.rgPopupLoginTime.setOnCheckedChangeListener((radioGroup, i) -> {
			if (R.id.acrbPopupLoginTime2hours == i) {
				mHour = 2;
			} else if (R.id.acrbPopupLoginTime24hours == i) {
				mHour = 24;
			} else if (R.id.acrbPopupLoginTimeAWeek == i) {
				mHour = 24 * 7;
			} else if (R.id.acrbPopupLoginTimePermanent == i) {
				mHour = 24 * 365 * 99;
			} else if (R.id.acrbPopupLoginTimeCustom == i) {
				mHour = popupLoginTimeBinding.acsbPopupLoginTimeCustom.getProgress() + 2;
			}
		});
		setRadioButtonBold(popupLoginTimeBinding.acrbPopupLoginTime2hours);
		setRadioButtonBold(popupLoginTimeBinding.acrbPopupLoginTime24hours);
		setRadioButtonBold(popupLoginTimeBinding.acrbPopupLoginTimeAWeek);
		setRadioButtonBold(popupLoginTimeBinding.acrbPopupLoginTimePermanent);
		popupLoginTimeBinding.acrbPopupLoginTimeCustom.setOnCheckedChangeListener((compoundButton, b) -> {
			popupLoginTimeBinding.acrbPopupLoginTimeCustom.getPaint().setFakeBoldText(b);
			popupLoginTimeBinding.acsbPopupLoginTimeCustom.setEnabled(b);
		});
		popupLoginTimeBinding.acsbPopupLoginTimeCustom.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
				mHour = i + 2;
				popupLoginTimeBinding.actvPopupLoginTimeCustomHourNumber.setText(String.valueOf(mHour));
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});
	}

	private void setRadioButtonBold(AppCompatRadioButton appCompatRadioButton) {
		appCompatRadioButton.setOnCheckedChangeListener((compoundButton, b) -> appCompatRadioButton.getPaint()
		                                                                                           .setFakeBoldText(b));
	}
}
