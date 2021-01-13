package com.viegre.nas.pad.util;

import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.view.Gravity;

import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.viegre.nas.pad.R;

import java.util.HashMap;

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

	/**
	 * 获取本地视频总时长
	 *
	 * @param path
	 * @return
	 */
	public static long getVideoDuration(String path) {
		long duration;
		String d = "5";
		MediaMetadataRetriever mmr = new MediaMetadataRetriever();
		try {
			if (null != path) {
				HashMap<String, String> headers = new HashMap<>();
				headers.put("User-Agent",
				            "Mozilla/5.0 (Linux; U; Android 4.4.2; zh-CN; MW-KW-001 Build/JRO03C) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 UCBrowser/1.0.0.001 U4/0.8.0 Mobile Safari/533.1");
				mmr.setDataSource(path, headers);
			}
			d = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			mmr.release();
		}
		duration = Long.parseLong(d);
		return duration;
	}
}
