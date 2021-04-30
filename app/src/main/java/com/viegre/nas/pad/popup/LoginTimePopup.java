package com.viegre.nas.pad.popup;

import android.content.Context;
import android.widget.SeekBar;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.SPUtils;
import com.lxj.xpopup.core.CenterPopupView;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.activity.LoginActivity;
import com.viegre.nas.pad.activity.MainActivity;
import com.viegre.nas.pad.config.SPConfig;
import com.viegre.nas.pad.config.UrlConfig;
import com.viegre.nas.pad.databinding.PopupLoginTimeBinding;
import com.viegre.nas.pad.entity.LoginEntity;
import com.viegre.nas.pad.manager.TextStyleManager;
import com.viegre.nas.pad.util.CommonUtils;

import androidx.annotation.NonNull;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import rxhttp.RxHttp;

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
		popupLoginTimeBinding.actvPopupLoginTimeConfirm.setOnClickListener(view -> refreshToken());
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
		TextStyleManager.INSTANCE.setBold(popupLoginTimeBinding.acrbPopupLoginTime2hours,
		                                  popupLoginTimeBinding.acrbPopupLoginTime24hours,
		                                  popupLoginTimeBinding.acrbPopupLoginTimeAWeek,
		                                  popupLoginTimeBinding.acrbPopupLoginTimePermanent);
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

	private void refreshToken() {
		RxHttp.postForm(UrlConfig.User.REFRESH_TOKEN)
		      .add("hour", mHour)
		      .asResponse(LoginEntity.class)
		      .observeOn(AndroidSchedulers.mainThread())
		      .subscribe(new Observer<LoginEntity>() {
			      @Override
			      public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {}

			      @Override
			      public void onNext(@io.reactivex.rxjava3.annotations.NonNull LoginEntity loginEntity) {
				      RxHttp.setOnParamAssembly(param -> param.setHeader("token", loginEntity.getToken()));
				      ActivityUtils.startActivity(MainActivity.class);
				      ActivityUtils.finishActivity(LoginActivity.class);
			      }

			      @Override
			      public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
				      CommonUtils.showErrorToast(e.getMessage());
				      SPUtils.getInstance().remove(SPConfig.PHONE);
			      }

			      @Override
			      public void onComplete() {
				      dismiss();
			      }
		      });
	}
}
