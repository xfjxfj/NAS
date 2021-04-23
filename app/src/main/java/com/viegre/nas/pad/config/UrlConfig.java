package com.viegre.nas.pad.config;

/**
 * Created by レインマン on 2021/01/04 17:44 with Android Studio.
 */
public class UrlConfig {

	//服务器地址
	public static final String APP_SERVER = "http://39.108.98.92:8708/";
//	public static final String APP_SERVER = "http://192.168.10.11:8080/";

	public static final String MQTT_SERVER = "tcp://39.108.236.191:1883";

	/**
	 * 用户相关接口
	 */
	public static class User {
		public static final String USER = APP_SERVER + "user/";
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
		public static final String DEVICE = APP_SERVER + "device/";
		//获取天气
		public static final String GET_WEATHER = DEVICE + "getWeather";
		//获取资源配置
		public static final String GET_RESOURCE = DEVICE + "getResource";
		//获取设备所有的绑定用户
		public static final String GET_GETALLFOLLOWS = DEVICE + "getAllFollows";
	}

	public static class App {
		//获取设备型号列表
		public static final String GET_DEVICETYPELIST = APP_SERVER + "app/getDeviceTypeList";
//		public static final String GET_DEVICETYPELIST = SERVER + "app/getDeviceTypeList";
	}
}


