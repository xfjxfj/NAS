package com.viegre.nas.speaker.config;

/**
 * Created by レインマン on 2021/01/04 17:44 with Android Studio.
 */
public class UrlConfig {

	//服务器地址
	public static final String SERVER_URL = "http://39.108.98.92:8708/";

	//用户相关接口
	public static final String USER = "user/";
	//获取图片验证码
	public static final String GET_IMAGE_CODE = "getImageCode";
	//账号密码登录
	public static final String LOGIN = "login";
	//登出接口
	public static final String LOGOUT = "logout";
	//刷新Token，限制有效时间
	public static final String REFRESH_TOKEN = "refreshToken";
}
