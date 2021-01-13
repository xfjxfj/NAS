package com.viegre.nas.pad.util;

import com.blankj.utilcode.util.SPUtils;
import com.viegre.nas.pad.config.SPConfig;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 从流获取图片
 * Created by レインマン on 2021/01/05 09:25 with Android Studio.
 */
public class ImageStreamUtils {

	/**
	 * 从流获取图片
	 *
	 * @param imageUrl
	 * @return
	 * @throws Exception
	 */
	public static byte[] getImageFromStream(String imageUrl) throws Exception {
		URL url = new URL(imageUrl);
		HttpURLConnection httpURLconnection = (HttpURLConnection) url.openConnection();
		httpURLconnection.setRequestMethod("POST");
		httpURLconnection.setReadTimeout(10 * 1000);
		//示例: "Set-Cookie, [JSESSIONID=E4240F41433CED789A71E7A40ABBA235; Path=/; HttpOnly]"
		String sessionId = httpURLconnection.getHeaderField("Set-Cookie").split(";")[0].replaceAll(" ", "");
		SPUtils.getInstance().put(SPConfig.LOGIN_CODE_SESSION_ID, sessionId);
		InputStream in;
		if (200 == httpURLconnection.getResponseCode()) {
			in = httpURLconnection.getInputStream();
			byte[] result = readStream(in);
			in.close();
			return result;
		}
		return null;
	}

	/**
	 * 读取流
	 *
	 * @param in
	 * @return
	 * @throws Exception
	 */
	private static byte[] readStream(InputStream in) throws Exception {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len;
		while ((len = in.read(buffer)) != -1) {
			outputStream.write(buffer, 0, len);
		}
		outputStream.close();
		in.close();
		return outputStream.toByteArray();
	}
}
