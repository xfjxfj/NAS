package com.viegre.nas.pad.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.view.Gravity;

import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.viegre.nas.pad.R;

/**
 * Created by レインマン on 2021/01/08 10:54 with Android Studio.
 */
public class CommonUtils {

	public static final long DEFAULT_SPLASH_GUIDE_DURATION = 5 * 1000L;

	public static void getOpenApp(Context context, PackageInfo packageName) {
		final Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName.packageName);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		context.startActivity(intent);
	}
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
	 * 弹出普通Toast
	 *
	 * @param msg
	 */
	public static void showToast(String msg) {
		ToastUtils.make().setTextColor(Color.BLACK).show(msg);
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
	 * 获取本地视频总时长
	 *
	 * @param path
	 * @return
	 */
	public static long getLocalVideoDuration(String path) {
		long duration = DEFAULT_SPLASH_GUIDE_DURATION;
		try {
			MediaMetadataRetriever mmr = new MediaMetadataRetriever();
			mmr.setDataSource(path);
			duration = Long.parseLong(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return duration;
	}
}
