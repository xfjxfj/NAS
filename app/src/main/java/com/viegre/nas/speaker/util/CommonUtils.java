package com.viegre.nas.speaker.util;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;

import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.BasePopupView;
import com.viegre.nas.speaker.R;

/**
 * Created by Djangoogle on 2021/01/08 10:54 with Android Studio.
 */
public class CommonUtils {

	/**
	 * 标记手机号中间4位为*
	 *
	 * @param phoneNumber
	 * @return
	 */
	public static String getMarkedPhoneNumber(String phoneNumber) {
		String start = phoneNumber.substring(0, 3);
		String end = phoneNumber.substring(phoneNumber.length() - 4);
		return start + "****" + end;
	}

	/**
	 * 弹出失败Toast
	 *
	 * @param msg
	 */
	public static void showErrorToast(String msg) {
		ToastUtils.make()
		          .setGravity(Gravity.CENTER, 0, 0)
		          .setBgResource(R.drawable.login_error_toast_bg)
		          .setTextColor(Color.WHITE)
		          .setTextSize(30)
		          .show(msg);
	}

	/**
	 * 弹出失败Toast
	 *
	 * @param id
	 */
	public static void showErrorToast(int id) {
		showErrorToast(StringUtils.getString(id));
	}

	/**
	 * 显示自定义弹窗
	 *
	 * @param context
	 * @param popupView
	 */
	public static void showCustomXPopup(Context context, BasePopupView popupView) {
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
