package com.viegre.nas.pad.fragment.settings.screen;

import android.content.ContentResolver;
import android.graphics.Color;
import android.net.Uri;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;

import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.blankj.utilcode.util.ColorUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.Utils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.djangoogle.framework.fragment.BaseFragment;
import com.github.iielse.switchbutton.SwitchView;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.config.BusConfig;
import com.viegre.nas.pad.config.SPConfig;
import com.viegre.nas.pad.databinding.FragmentScreenBinding;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.widget.AppCompatTextView;

/**
 * Created by レインマン on 2020/12/17 17:37 with Android Studio.
 */
public class ScreenFragment extends BaseFragment<FragmentScreenBinding> {

	private OptionsPickerView<Integer> mTimeOptionsPickerView;
	private final List<Integer> mDurationList = new ArrayList<>();

	@Override
	protected void initialize() {
		initSaver();
		initBrightness();
	}

	public static ScreenFragment newInstance() {
		return new ScreenFragment();
	}

	private void initSaver() {
		boolean screenSaverSwitch = SPUtils.getInstance().getBoolean(SPConfig.SCREEN_SAVER_SWITCH, true);
		mViewBinding.svScreenStandbyModeSwitch.setOpened(screenSaverSwitch);
		mDurationList.add(5);
		mDurationList.add(10);
		mDurationList.add(30);
		mDurationList.add(60);
		if (screenSaverSwitch) {
			openSaver();
		} else {
			closeSaver();
		}
		mViewBinding.svScreenStandbyModeSwitch.setOnStateChangedListener(new SwitchView.OnStateChangedListener() {
			@Override
			public void toggleToOn(SwitchView view) {
				SPUtils.getInstance().put(SPConfig.SCREEN_SAVER_SWITCH, true);
				openSaver();
				view.toggleSwitch(true);
			}

			@Override
			public void toggleToOff(SwitchView view) {
				SPUtils.getInstance().put(SPConfig.SCREEN_SAVER_SWITCH, false);
				closeSaver();
				view.toggleSwitch(false);
			}
		});
	}

	private void openSaver() {
		initCustomImage();
		int screenSaverDelay = SPUtils.getInstance().getInt(SPConfig.SCREEN_SAVER_DELAY, 5);
		mViewBinding.acivScreenMisoperation.setText(StringUtils.getString(R.string.screen_misoperation, screenSaverDelay));
		int index = 0;
		for (int i = 0; i < mDurationList.size(); i++) {
			if (screenSaverDelay == mDurationList.get(i)) {
				index = i;
				break;
			}
		}
		int finalIndex = index;
		mViewBinding.rlScreenEnterStandbyTime.setOnClickListener(view -> {
			mTimeOptionsPickerView = new OptionsPickerBuilder(mActivity, (options1, options2, options3, v) -> {
				SPUtils.getInstance().put(SPConfig.SCREEN_SAVER_DELAY, mDurationList.get(options1));
				mViewBinding.acivScreenMisoperation.setText(StringUtils.getString(R.string.screen_misoperation, mDurationList.get(options1)));
				Settings.System.putInt(mActivity.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, mDurationList.get(options1) * 60 * 1000);
				openSaver();
			}).setLayoutRes(R.layout.picker_view_screen_saver, v -> {
				AppCompatTextView actvPickerViewScreenSaverTitle = v.findViewById(R.id.actvPickerViewScreenSaverTitle);
				AppCompatTextView actvPickerViewScreenSaverConfirm = v.findViewById(R.id.actvPickerViewScreenSaverConfirm);
				AppCompatTextView actvPickerViewScreenSaverCancel = v.findViewById(R.id.actvPickerViewScreenSaverCancel);
				actvPickerViewScreenSaverTitle.setText(StringUtils.getString(R.string.screen_enter_standby_time));
				actvPickerViewScreenSaverConfirm.setOnClickListener(view1 -> {
					mTimeOptionsPickerView.returnData();
					mTimeOptionsPickerView.dismiss();
				});
				actvPickerViewScreenSaverCancel.setOnClickListener(view2 -> mTimeOptionsPickerView.dismiss());
			})
			  .setContentTextSize(40)//滚轮文字大小
			  .setOutSideCancelable(false)//点击屏幕，点在控件外部范围时，是否取消显示
			  .setCyclic(true, true, true)//是否循环滚动
			  .setBgColor(Color.TRANSPARENT)//滚轮背景颜色
			  .setTextColorOut(ColorUtils.getColor(R.color.pickerview_wheelview_textcolor_out))
			  .setTextColorCenter(ColorUtils.getColor(R.color.pickerview_wheelview_textcolor_center))
			  .setDividerColor(ColorUtils.getColor(R.color.pickerview_wheelview_textcolor_divider))
			  .setLabels("分钟", "", "")
			  .setSelectOptions(finalIndex, 0, 0)//设置默认选中项
			  .isCenterLabel(false)//是否只显示中间选中项的label文字，false则每项item全部都带有label。
			  .isDialog(true)//是否显示为对话框样式
			  .build();

			mTimeOptionsPickerView.setPicker(mDurationList);
			mTimeOptionsPickerView.show();
		});
		mViewBinding.vScreenStandbyModeSwitchLine.setVisibility(View.VISIBLE);
		mViewBinding.rlScreenEnterStandbyTime.setVisibility(View.VISIBLE);
		mViewBinding.actvScreenStandbyImage.setVisibility(View.VISIBLE);
		mViewBinding.llcScreenStandbyImage.setVisibility(View.VISIBLE);
	}

	private void closeSaver() {
		mViewBinding.rlScreenEnterStandbyTime.setOnClickListener(null);
		mViewBinding.vScreenStandbyModeSwitchLine.setVisibility(View.GONE);
		mViewBinding.rlScreenEnterStandbyTime.setVisibility(View.GONE);
		mViewBinding.actvScreenStandbyImage.setVisibility(View.GONE);
		mViewBinding.llcScreenStandbyImage.setVisibility(View.GONE);
		Settings.System.putInt(mActivity.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, Integer.MAX_VALUE);
	}

	private void initCustomImage() {
		boolean screenSaverCustomSwitch = SPUtils.getInstance().getBoolean(SPConfig.SCREEN_SAVER_CUSTOM_SWITCH, false);
		mViewBinding.svScreenCustomStandbyImageSwitch.setOpened(screenSaverCustomSwitch);
		if (screenSaverCustomSwitch) {
			openCustom();
		} else {
			closeCustom();
		}
		mViewBinding.svScreenCustomStandbyImageSwitch.setOnStateChangedListener(new SwitchView.OnStateChangedListener() {
			@Override
			public void toggleToOn(SwitchView view) {
				SPUtils.getInstance().put(SPConfig.SCREEN_SAVER_CUSTOM_SWITCH, true);
				openCustom();
				view.toggleSwitch(true);
			}

			@Override
			public void toggleToOff(SwitchView view) {
				SPUtils.getInstance().put(SPConfig.SCREEN_SAVER_CUSTOM_SWITCH, false);
				closeCustom();
				view.toggleSwitch(false);
			}
		});
	}

	private void openCustom() {
		mViewBinding.llcScreenStandbyImageDefault.setVisibility(View.GONE);
		mViewBinding.vScreenStandbyImageLine1.setVisibility(View.GONE);
		mViewBinding.vScreenStandbyImageLine2.setVisibility(View.VISIBLE);
		mViewBinding.rlScreenAssignImage.setVisibility(View.VISIBLE);
		mViewBinding.acivScreenStandbyImageDefault1.setImageResource(0);
		mViewBinding.acivScreenStandbyImageDefault2.setImageResource(0);
		mViewBinding.acivScreenStandbyImageDefault3.setImageResource(0);
		mViewBinding.acivScreenStandbyImageDefault4.setImageResource(0);
		mViewBinding.rlScreenAssignImage.setOnClickListener(view -> EventBus.getDefault().post(BusConfig.SCREEN_CUSTOM_SHOW));
	}

	private void closeCustom() {
		mViewBinding.rlScreenAssignImage.setOnClickListener(null);
		mViewBinding.llcScreenStandbyImageDefault.setVisibility(View.VISIBLE);
		mViewBinding.vScreenStandbyImageLine1.setVisibility(View.VISIBLE);
		mViewBinding.vScreenStandbyImageLine2.setVisibility(View.GONE);
		mViewBinding.rlScreenAssignImage.setVisibility(View.GONE);
		Glide.with(this)
		     .load(R.mipmap.screen_standby_image_default_1)
		     .apply(RequestOptions.bitmapTransform(new RoundedCorners(4)))
		     .into(mViewBinding.acivScreenStandbyImageDefault1);
		Glide.with(this)
		     .load(R.mipmap.screen_standby_image_default_2)
		     .apply(RequestOptions.bitmapTransform(new RoundedCorners(4)))
		     .into(mViewBinding.acivScreenStandbyImageDefault2);
		Glide.with(this)
		     .load(R.mipmap.screen_standby_image_default_3)
		     .apply(RequestOptions.bitmapTransform(new RoundedCorners(4)))
		     .into(mViewBinding.acivScreenStandbyImageDefault3);
		Glide.with(this)
		     .load(R.mipmap.screen_standby_image_default_4)
		     .apply(RequestOptions.bitmapTransform(new RoundedCorners(4)))
		     .into(mViewBinding.acivScreenStandbyImageDefault4);
		SPUtils.getInstance().remove(SPConfig.SCREEN_SAVER_CUSTOM_IMAGES);
	}

	private void initBrightness() {
		try {
			int brightness = Settings.System.getInt(Utils.getApp().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
			if (brightness >= 0) {
				mViewBinding.acsbScreenBrightnessControl.setProgress(brightness);
			}
		} catch (Settings.SettingNotFoundException e) {
			e.printStackTrace();
		}
		mViewBinding.acsbScreenBrightnessControl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
				setScreenBrightness(seekBar.getProgress());
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});
	}

	/**
	 * 设置屏幕的亮度
	 */
	private void setScreenBrightness(int process) {
		//设置当前窗口的亮度值
		WindowManager.LayoutParams localLayoutParams = mActivity.getWindow().getAttributes();
		localLayoutParams.screenBrightness = process / 255.0F;
		mActivity.getWindow().setAttributes(localLayoutParams);
		//修改系统的亮度值,以至于退出应用程序亮度保持
		saveBrightness(Utils.getApp().getContentResolver(), process);
	}

	private void saveBrightness(ContentResolver resolver, int brightness) {
		//改变系统的亮度值
		//设置为手动调节模式
		Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
		//保存到系统中
		Uri uri = android.provider.Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS);
		android.provider.Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, brightness);
		resolver.notifyChange(uri, null);
	}
}
