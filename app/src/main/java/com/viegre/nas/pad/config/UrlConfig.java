package com.viegre.nas.pad.config;

/**
 * Created by レインマン on 2021/01/04 17:44 with Android Studio.
 */
public class UrlConfig {

	//服务器地址
	public static final String SERVER = "http://39.108.98.92:8708/";

	/**
	 * 用户相关接口
	 */
	public static class User {
		public static final String USER = SERVER + "user/";
		//获取图片验证码
		public static final String GET_IMAGE_CODE = USER + "getImageCode";
		//账号密码登录
		public static final String LOGIN = USER + "login";
		//登出接口
		public static final String LOGOUT = USER + "logout";
		//刷新Token，限制有效时间
		public static final String REFRESH_TOKEN = USER + "refreshToken";
	}

	/**
	 * 设备相关接口
	 */
	public static class Device {
		public static final String DEVICE = SERVER + "device/";
		//获取天气
		public static final String GET_WEATHER = DEVICE + "getWeather";
		//获取资源配置
		public static final String GET_RESOURCE = DEVICE + "getResource";
	}
}
