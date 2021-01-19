package com.viegre.nas.pad.manager;

import android.util.TypedValue;

import com.blankj.utilcode.util.ColorUtils;
import com.viegre.nas.pad.R;

import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.appcompat.widget.AppCompatTextView;

/**
 * Created by レインマン on 2021/01/19 16:31 with Android Studio.
 */
public enum TextStyleManager {

	INSTANCE;

	/**
	 * 设置文字是否加粗
	 *
	 * @param isChecked
	 * @param actvList
	 */
	public void setAudioListCheck(boolean isChecked, AppCompatTextView... actvList) {
		for (AppCompatTextView actv : actvList) {
			actv.setTextColor(isChecked ? ColorUtils.getColor(R.color.settings_menu_selected_bg) : ColorUtils.getColor(R.color.audio_list_unchecked));
			actv.getPaint().setFakeBoldText(isChecked);
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
	 * 设置文件管理器标签字号和粗细
	 *
	 * @param acrbList
	 */
	public void setFileManagerTag(AppCompatRadioButton... acrbList) {
		for (AppCompatRadioButton acrb : acrbList) {
			acrb.setOnCheckedChangeListener((compoundButton, b) -> {
				acrb.setTextSize(TypedValue.COMPLEX_UNIT_PX, b ? 30F : 20F);
				acrb.getPaint().setFakeBoldText(b);
			});
		}
	}
}
