package com.viegre.nas.pad.manager;

import android.util.TypedValue;

import androidx.appcompat.widget.AppCompatRadioButton;

/**
 * Created by レインマン on 2021/01/19 16:31 with Android Studio.
 */
public enum RadioButtonManager {

	INSTANCE;

	public void setBold(AppCompatRadioButton acrb) {
		acrb.setOnCheckedChangeListener((compoundButton, b) -> acrb.getPaint().setFakeBoldText(b));
	}

	public void setFileManagerTag(AppCompatRadioButton acrb) {
		acrb.setOnCheckedChangeListener((compoundButton, b) -> {
			acrb.setTextSize(TypedValue.COMPLEX_UNIT_PX, b ? 30F : 20F);
			acrb.getPaint().setFakeBoldText(b);
		});
	}
}
