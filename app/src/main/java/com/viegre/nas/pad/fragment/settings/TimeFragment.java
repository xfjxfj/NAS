package com.viegre.nas.pad.fragment.settings;

import android.graphics.Color;
import android.os.SystemClock;

import androidx.appcompat.widget.AppCompatTextView;

import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.view.TimePickerView;
import com.blankj.utilcode.util.ColorUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.djangoogle.framework.fragment.BaseFragment;
import com.github.iielse.switchbutton.SwitchView;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.config.SPConfig;
import com.viegre.nas.pad.databinding.FragmentTimeBinding;
import com.viegre.nas.pad.util.SntpClient;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by レインマン on 2020/12/17 17:38 with Android Studio.
 */
public class TimeFragment extends BaseFragment<FragmentTimeBinding> {

	private SimpleDateFormat mDateFormat, mTimeFormat;
	private TimePickerView mDatePickerView, mTimePickerView;

	@Override
	protected void initialize() {
		boolean isSync = SPUtils.getInstance().getBoolean(SPConfig.TIME_SYNC, true);
		mViewBinding.svTimeSyncSwitch.setOpened(SPUtils.getInstance().getBoolean(SPConfig.TIME_SYNC, true));
		if (isSync) {
			timeSyncOn();
		} else {
			timeSyncOff();
		}
		mViewBinding.svTimeSyncSwitch.setOnStateChangedListener(new SwitchView.OnStateChangedListener() {
			@Override
			public void toggleToOn(SwitchView view) {
				timeSyncOn();
			}

			@Override
			public void toggleToOff(SwitchView view) {
				timeSyncOff();
			}
		});
	}

	public static TimeFragment newInstance() {
		return new TimeFragment();
	}

	private void timeSyncOn() {
		mViewBinding.rlTimeDate.setOnClickListener(null);
		mViewBinding.rlTime.setOnClickListener(null);
		ThreadUtils.executeBySingle(new ThreadUtils.SimpleTask<Date>() {
			@Override
			public Date doInBackground() {
				SntpClient sntpClient = new SntpClient();
				if (sntpClient.requestTime("cn.pool.ntp.org", 30000)) {
					long now = sntpClient.getNtpTime() + SystemClock.elapsedRealtime() - sntpClient.getNtpTimeReference();
					return new Date(now);
				}
				return null;
			}

			@Override
			public void onSuccess(Date result) {
				SPUtils.getInstance().put(SPConfig.TIME_SYNC, true);
				mViewBinding.svTimeSyncSwitch.toggleSwitch(true);
			}
		});
	}

	private void timeSyncOff() {
		//设置日期
		mViewBinding.rlTimeDate.setOnClickListener(view -> {
			mDatePickerView = new TimePickerBuilder(mActivity, (date, v) -> {

			}).setLayoutRes(R.layout.picker_view_time, v -> {
				AppCompatTextView actvPickerViewTimeTitle = v.findViewById(R.id.actvPickerViewTimeTitle);
				AppCompatTextView actvPickerViewTimeConfirm = v.findViewById(R.id.actvPickerViewTimeConfirm);
				AppCompatTextView actvPickerViewTimeCancel = v.findViewById(R.id.actvPickerViewTimeCancel);
				actvPickerViewTimeTitle.setText(StringUtils.getString(R.string.time_pick_date));
				actvPickerViewTimeConfirm.setOnClickListener(view1 -> {
					mDatePickerView.returnData();
					mDatePickerView.dismiss();
				});
				actvPickerViewTimeCancel.setOnClickListener(view12 -> mDatePickerView.dismiss());
			})
			  .setType(new boolean[]{true, true, true, false, false, false})// 默认全部显示
			  .setContentTextSize(40)//滚轮文字大小
			  .setOutSideCancelable(false)//点击屏幕，点在控件外部范围时，是否取消显示
			  .isCyclic(true)//是否循环滚动
			  .setBgColor(Color.TRANSPARENT)//滚轮背景颜色
			  .setTextColorOut(ColorUtils.getColor(R.color.pickerview_wheelview_textcolor_out))
			  .setTextColorCenter(ColorUtils.getColor(R.color.pickerview_wheelview_textcolor_center))
			  .setDividerColor(ColorUtils.getColor(R.color.pickerview_wheelview_textcolor_divider))
			  .setLabel("", "", "", "", "", "")
			  .isCenterLabel(false)//是否只显示中间选中项的label文字，false则每项item全部都带有label。
			  .isDialog(true)//是否显示为对话框样式
			  .build();
			mDatePickerView.show();
		});
		//设置时间
		mViewBinding.rlTime.setOnClickListener(view -> {
			mTimePickerView = new TimePickerBuilder(mActivity, (date, v) -> {

			}).setLayoutRes(R.layout.picker_view_time, v -> {
				AppCompatTextView actvPickerViewTimeTitle = v.findViewById(R.id.actvPickerViewTimeTitle);
				AppCompatTextView actvPickerViewTimeConfirm = v.findViewById(R.id.actvPickerViewTimeConfirm);
				AppCompatTextView actvPickerViewTimeCancel = v.findViewById(R.id.actvPickerViewTimeCancel);
				actvPickerViewTimeTitle.setText(StringUtils.getString(R.string.time_pick_time));
				actvPickerViewTimeConfirm.setOnClickListener(view1 -> {
					mTimePickerView.returnData();
					mTimePickerView.dismiss();
				});
				actvPickerViewTimeCancel.setOnClickListener(view12 -> mTimePickerView.dismiss());
			})
			  .setType(new boolean[]{false, false, false, true, true, false})// 默认全部显示
			  .setContentTextSize(40)//滚轮文字大小
			  .setOutSideCancelable(false)//点击屏幕，点在控件外部范围时，是否取消显示
			  .isCyclic(true)//是否循环滚动
			  .setBgColor(Color.TRANSPARENT)//滚轮背景颜色
			  .setTextColorOut(ColorUtils.getColor(R.color.pickerview_wheelview_textcolor_out))
			  .setTextColorCenter(ColorUtils.getColor(R.color.pickerview_wheelview_textcolor_center))
			  .setDividerColor(ColorUtils.getColor(R.color.pickerview_wheelview_textcolor_divider))
			  .setLabel("", "", "", "", "", "")
			  .isCenterLabel(false)//是否只显示中间选中项的label文字，false则每项item全部都带有label。
			  .isDialog(true)//是否显示为对话框样式
			  .build();
			mTimePickerView.show();
		});
		SPUtils.getInstance().put(SPConfig.TIME_SYNC, false);
		mViewBinding.svTimeSyncSwitch.toggleSwitch(false);
	}
}
