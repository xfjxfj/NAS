package com.viegre.nas.pad.manager;

import android.util.TypedValue;

import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.appcompat.widget.AppCompatTextView;

import com.blankj.utilcode.util.ColorUtils;
import com.viegre.nas.pad.R;

/**
 * Created by レインマン on 2021/01/19 16:31 with Android Studio.
 */
public enum TextStyleManager {

	INSTANCE;

	/**
	 * 设置音频播放列表文字是否加粗
	 *
	 * @param isChecked
	 * @param actvList
	 */
	public void setAudioPlayListCheck(boolean isChecked, AppCompatTextView... actvList) {
		for (AppCompatTextView actv : actvList) {
			actv.setTextColor(isChecked ? ColorUtils.getColor(R.color.settings_menu_selected_bg) : ColorUtils.getColor(R.color.audio_list_unchecked));
			actv.getPaint().setFakeBoldText(isChecked);
			actv.setTextSize(TypedValue.COMPLEX_UNIT_PX, isChecked ? 20F : 16F);
		}
	}

	/**
	 * 设置RadioButton文字是否加粗
	 *
	 * @param acrbList
	 */
	public void setBold(AppCompatRadioButton... acrbList) {
		for (AppCompatRadioButton acrb : acrbList) {
			acrb.setOnCheckedChangeListener((compoundButton, b) -> acrb.getPaint().setFakeBoldText(b));
		}
	}

	/**
	 * 设置文件管理器标签字号和粗细（状态变化时）
	 *
	 * @param acrbList
	 */
	public void setFileManagerTagOnCheckedChange(AppCompatRadioButton... acrbList) {
		for (AppCompatRadioButton acrb : acrbList) {
			acrb.setOnCheckedChangeListener((compoundButton, b) -> setFileManagerTag(b, acrb));
			setFileManagerTag(acrb.isChecked(), acrb);
		}
	}

	/**
	 * 设置文件管理器标签字号和粗细
	 *
	 * @param b
	 * @param acrb
	 */
	public void setFileManagerTag(boolean b, AppCompatRadioButton acrb) {
		acrb.setTextSize(TypedValue.COMPLEX_UNIT_PX, b ? 30F : 20F);
		acrb.getPaint().setFakeBoldText(b);
	}
}
