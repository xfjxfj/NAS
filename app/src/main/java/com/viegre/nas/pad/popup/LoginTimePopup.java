package com.viegre.nas.pad.popup;

import android.content.Context;
import android.widget.SeekBar;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.lxj.xpopup.core.CenterPopupView;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.activity.LoginActivity;
import com.viegre.nas.pad.activity.MainActivity;
import com.viegre.nas.pad.config.UrlConfig;
import com.viegre.nas.pad.databinding.PopupLoginTimeBinding;
import com.viegre.nas.pad.entity.LoginEntity;
import com.viegre.nas.pad.entity.LoginInfoEntity;
import com.viegre.nas.pad.manager.RadioButtonManager;
import com.viegre.nas.pad.util.CommonUtils;
import com.yanzhenjie.kalle.Kalle;
import com.yanzhenjie.kalle.simple.SimpleCallback;
import com.yanzhenjie.kalle.simple.SimpleResponse;

import org.litepal.LitePal;

import androidx.annotation.NonNull;

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
		popupLoginTimeBinding.actvPopupLoginTimeConfirm.setOnClickListener(view -> Kalle.post(UrlConfig.User.REFRESH_TOKEN)
		                                                                                .param("hour", mHour)
		                                                                                .perform(new SimpleCallback<LoginEntity>() {
			                                                                                @Override
			                                                                                public void onResponse(SimpleResponse<LoginEntity, String> response) {
				                                                                                dismiss();
				                                                                                if (!response.isSucceed()) {
					                                                                                CommonUtils.showErrorToast(response.failed());
					                                                                                LitePal.deleteAll(LoginInfoEntity.class);
				                                                                                } else {
					                                                                                String token = response.succeed().getToken();
					                                                                                ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<Void>() {
						                                                                                @Override
						                                                                                public Void doInBackground() {
							                                                                                LoginInfoEntity loginInfoEntity = LitePal.findFirst(
									                                                                                LoginInfoEntity.class);
							                                                                                loginInfoEntity.setToken(token);
							                                                                                loginInfoEntity.saveOrUpdate();
							                                                                                return null;
						                                                                                }

						                                                                                @Override
						                                                                                public void onSuccess(Void result) {
							                                                                                Kalle.getConfig()
							                                                                                     .getHeaders()
							                                                                                     .set("token", token);
							                                                                                Kalle.setConfig(Kalle.getConfig());
							                                                                                ActivityUtils.startActivity(MainActivity.class);
							                                                                                ActivityUtils.finishActivity(LoginActivity.class);
						                                                                                }
					                                                                                });
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
		RadioButtonManager.INSTANCE.setBold(popupLoginTimeBinding.acrbPopupLoginTime2hours);
		RadioButtonManager.INSTANCE.setBold(popupLoginTimeBinding.acrbPopupLoginTime24hours);
		RadioButtonManager.INSTANCE.setBold(popupLoginTimeBinding.acrbPopupLoginTimeAWeek);
		RadioButtonManager.INSTANCE.setBold(popupLoginTimeBinding.acrbPopupLoginTimePermanent);
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
}
