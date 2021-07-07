package com.viegre.nas.pad.popup;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;
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

import org.json.JSONException;
import org.json.JSONObject;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import rxhttp.RxHttp;
import rxhttp.RxHttpPlugins;

/**
 * Created by レインマン on 2021/01/08 16:03 with Android Studio.
 */
public class LoginTimePopup extends CenterPopupView implements View.OnClickListener {

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
//        popupLoginTimeBinding.actvPopupLoginTimeConfirm.setOnClickListener(view -> refreshToken());
        popupLoginTimeBinding.actvPopupLoginTimeConfirm.setOnClickListener(this);
//		popupLoginTimeBinding.rgPopupLoginTime.setOnCheckedChangeListener((radioGroup, i) -> {
//			if (R.id.acrbPopupLoginTime2hours == i) {
//				mHour = 2;
//			} else if (R.id.acrbPopupLoginTime24hours == i) {
//				mHour = 24;
//			} else if (R.id.acrbPopupLoginTimeAWeek == i) {
//				mHour = 24 * 7;
//			} else if (R.id.acrbPopupLoginTimePermanent == i) {
//				mHour = 24 * 365 * 99;
//			} else if (R.id.acrbPopupLoginTimeCustom == i) {
//				mHour = popupLoginTimeBinding.acsbPopupLoginTimeCustom.getProgress() + 2;
//			}
//		});
        try {
            JSONObject js = new JSONObject();
            js.put("phone", SPUtils.getInstance().getString(SPConfig.PHONE));
            js.put(SPConfig.TOKEN_START_TIME, System.currentTimeMillis());
            switch (SPUtils.getInstance().getString(SPConfig.TOKEN_TYPE)) {
                case "1":
                    mHour = 2;
                    js.put(SPConfig.TOKEN_HOUR_TIME, mHour);

                    SPUtils.getInstance().put(SPConfig.TOKEN_TYPE, "1");

                    setPopupView(popupLoginTimeBinding, false);

                    popupLoginTimeBinding.acrbPopupLoginTime2hours.setChecked(true);
                    break;
                case "2":
                    mHour = 24;
                    js.put(SPConfig.TOKEN_HOUR_TIME, mHour);

                    SPUtils.getInstance().put(SPConfig.TOKEN_TYPE, "2");
                    setPopupView(popupLoginTimeBinding, false);
                    popupLoginTimeBinding.acrbPopupLoginTime24hours.setChecked(true);
                    break;
                case "3":
                    SPUtils.getInstance().put(SPConfig.TOKEN_TYPE, "3");
                    mHour = 24 * 7;
                    js.put(SPConfig.TOKEN_HOUR_TIME, mHour);
                    setPopupView(popupLoginTimeBinding, false);
                    popupLoginTimeBinding.acrbPopupLoginTimeAWeek.setChecked(true);
                    break;
                case "4":
                    SPUtils.getInstance().put(SPConfig.TOKEN_TYPE, "4");
                    mHour = 24 * 365 * 99;
                    js.put(SPConfig.TOKEN_HOUR_TIME, mHour);
                    setPopupView(popupLoginTimeBinding, false);
                    popupLoginTimeBinding.acrbPopupLoginTimePermanent.setChecked(true);
                    break;
                case "":
                case "5":
                    SPUtils.getInstance().put(SPConfig.TOKEN_TYPE, "5");
                    mHour = popupLoginTimeBinding.acsbPopupLoginTimeCustom.getProgress() + 2;
                    js.put(SPConfig.TOKEN_HOUR_TIME, mHour);
                    setPopupView(popupLoginTimeBinding, true);
                    popupLoginTimeBinding.acrbPopupLoginTimeCustom.setChecked(true);
                    break;
            }
            SPUtils.getInstance().put(SPConfig.TOKEN_TIME, js.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(CommonUtils.getFileName()+CommonUtils.getLineNumber(),e.toString());
        }
        popupLoginTimeBinding.rgPopupLoginTime.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                /**
                 * 1为2个小时的按钮
                 * 2为24小时的按钮
                 * 3为一周的按钮
                 * 4为永久的按钮
                 * 5为自定义的按钮
                 * */
                try {
                    JSONObject jstime = new JSONObject();
                    jstime.put("phone", SPUtils.getInstance().getString(SPConfig.PHONE));
                    jstime.put(SPConfig.TOKEN_START_TIME, System.currentTimeMillis());
                    if (R.id.acrbPopupLoginTime2hours == checkedId) {
                        SPUtils.getInstance().put(SPConfig.TOKEN_TYPE, "1");
                        mHour = 2;
                        jstime.put(SPConfig.TOKEN_HOUR_TIME, mHour);
                        setPopupView(popupLoginTimeBinding, false);
                    } else if (R.id.acrbPopupLoginTime24hours == checkedId) {
                        SPUtils.getInstance().put(SPConfig.TOKEN_TYPE, "2");
                        mHour = 24;
                        jstime.put(SPConfig.TOKEN_HOUR_TIME, mHour);
                        setPopupView(popupLoginTimeBinding, false);
                    } else if (R.id.acrbPopupLoginTimeAWeek == checkedId) {
                        SPUtils.getInstance().put(SPConfig.TOKEN_TYPE, "3");
                        mHour = 24 * 7;
                        jstime.put(SPConfig.TOKEN_HOUR_TIME, mHour);
                        setPopupView(popupLoginTimeBinding, false);
                    } else if (R.id.acrbPopupLoginTimePermanent == checkedId) {
                        SPUtils.getInstance().put(SPConfig.TOKEN_TYPE, "4");
                        mHour = 24 * 365 * 99;
                        jstime.put(SPConfig.TOKEN_HOUR_TIME, mHour);
                        setPopupView(popupLoginTimeBinding, false);
                    } else if (R.id.acrbPopupLoginTimeCustom == checkedId) {
                        SPUtils.getInstance().put(SPConfig.TOKEN_TYPE, "5");
                        mHour = popupLoginTimeBinding.acsbPopupLoginTimeCustom.getProgress() + 2;
                        jstime.put(SPConfig.TOKEN_HOUR_TIME, mHour);
                        setPopupView(popupLoginTimeBinding, true);
                    }
                    SPUtils.getInstance().put(SPConfig.TOKEN_TIME, jstime.toString());
                } catch (Exception e) {
                    Log.e(CommonUtils.getFileName()+CommonUtils.getLineNumber(),e.toString());
                }
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
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void setPopupView(PopupLoginTimeBinding popupLoginTimeBinding, boolean b) {
        if (b) {
            popupLoginTimeBinding.actvPopupLoginTimeCustomTitle.setVisibility(VISIBLE);
            popupLoginTimeBinding.actvPopupLoginTimeCustomHour.setVisibility(VISIBLE);
            popupLoginTimeBinding.actvPopupLoginTimeCustomHourNumber.setVisibility(VISIBLE);
            popupLoginTimeBinding.acsbPopupLoginTimeCustom.setVisibility(VISIBLE);
            popupLoginTimeBinding.actvPopupLoginTimeCustom2HoursPrompt.setVisibility(VISIBLE);
            popupLoginTimeBinding.actvPopupLoginTimeCustom24HoursPrompt.setVisibility(VISIBLE);
        } else {
            popupLoginTimeBinding.actvPopupLoginTimeCustomTitle.setVisibility(GONE);
            popupLoginTimeBinding.actvPopupLoginTimeCustomHour.setVisibility(GONE);
            popupLoginTimeBinding.actvPopupLoginTimeCustomHourNumber.setVisibility(GONE);
            popupLoginTimeBinding.acsbPopupLoginTimeCustom.setVisibility(GONE);
            popupLoginTimeBinding.actvPopupLoginTimeCustom2HoursPrompt.setVisibility(GONE);
            popupLoginTimeBinding.actvPopupLoginTimeCustom24HoursPrompt.setVisibility(GONE);
        }

    }

    private void refreshToken() {
        RxHttp.postForm(UrlConfig.User.REFRESH_TOKEN)
                .add("hour", mHour)

                .asResponse(LoginEntity.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<LoginEntity>() {
                    @Override
                    public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
                    }

                    @Override
                    public void onNext(@io.reactivex.rxjava3.annotations.NonNull LoginEntity loginEntity) {
                        RxHttpPlugins.init(RxHttpPlugins.getOkHttpClient())
                                .setOnParamAssembly(param -> param.addHeader("token", loginEntity.getToken()));
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

    @Override
    public void onClick(View v) {
        ActivityUtils.startActivity(MainActivity.class);
        ActivityUtils.finishActivity(LoginActivity.class);
    }
}
