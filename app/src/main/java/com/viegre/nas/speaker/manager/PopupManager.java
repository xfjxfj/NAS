package com.viegre.nas.speaker.manager;

import android.content.Context;

import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.BasePopupView;

/**
 * Created by レインマン on 2021/01/08 11:15 PM with Android Studio.
 */
public enum PopupManager {

	INSTANCE;

	/**
	 * 显示自定义弹窗
	 *
	 * @param context
	 * @param popupView
	 */
	public void showCustomXPopup(Context context, BasePopupView popupView) {
		new XPopup.Builder(context).hasShadowBg(false)//是否有半透明的背景，默认为true
		                           .hasBlurBg(true)//是否有高斯模糊的背景，默认为false
		                           .dismissOnBackPressed(false)//按返回键是否关闭弹窗，默认为true
		                           .dismissOnTouchOutside(false)//点击外部是否关闭弹窗，默认为true
		                           .hasStatusBar(false)//是否显示状态栏，默认显示
		                           .hasNavigationBar(false)//是否显示导航栏，默认显示
		                           .asCustom(popupView)//设置自定义弹窗
		                           .show();
	}
}
