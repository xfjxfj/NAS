package com.viegre.nas.speaker.util;

import android.graphics.Color;
import android.view.Gravity;

import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.viegre.nas.speaker.R;

/**
 * Created by レインマン on 2021/01/08 10:54 with Android Studio.
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
}
