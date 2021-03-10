package com.viegre.nas.pad.fragment.settings;

import android.graphics.Color;
import android.provider.Settings;
import android.view.View;

import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.blankj.utilcode.util.ColorUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.StringUtils;
import com.djangoogle.framework.fragment.BaseFragment;
import com.github.iielse.switchbutton.SwitchView;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.config.SPConfig;
import com.viegre.nas.pad.databinding.FragmentScreenBinding;

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
				Settings.System.putInt(mActivity.getContentResolver(), android.provider.Settings.System.SCREEN_OFF_TIMEOUT, options1 * 60 * 1000);
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
			  .setSelectOptions(0, finalIndex, 0)//设置默认选中项
			  .isCenterLabel(false)//是否只显示中间选中项的label文字，false则每项item全部都带有label。
			  .isDialog(true)//是否显示为对话框样式
			  .build();

			mTimeOptionsPickerView.setPicker(mDurationList);
			mTimeOptionsPickerView.show();
		});
		mViewBinding.vScreenStandbyModeSwitchLine.setVisibility(View.VISIBLE);
		mViewBinding.rlScreenEnterStandbyTime.setVisibility(View.VISIBLE);
		mViewBinding.actvScreenStandbyPicture.setVisibility(View.VISIBLE);
		mViewBinding.llcScreenStandbyPicture.setVisibility(View.VISIBLE);
	}

	private void closeSaver() {
		mViewBinding.rlScreenEnterStandbyTime.setOnClickListener(null);
		mViewBinding.vScreenStandbyModeSwitchLine.setVisibility(View.GONE);
		mViewBinding.rlScreenEnterStandbyTime.setVisibility(View.GONE);
		mViewBinding.actvScreenStandbyPicture.setVisibility(View.GONE);
		mViewBinding.llcScreenStandbyPicture.setVisibility(View.GONE);
		Settings.System.putInt(mActivity.getContentResolver(), android.provider.Settings.System.SCREEN_OFF_TIMEOUT, Integer.MAX_VALUE);
	}
}
