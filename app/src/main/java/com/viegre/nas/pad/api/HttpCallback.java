package com.viegre.nas.pad.api;

import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/8/16.
 */
public abstract class HttpCallback {

	/**
	 * 请求失败调用（网络问题）
	 *
	 * @param request
	 * @param e
	 */
	public abstract void onFailure(Request request, Exception e);

	/**
	 * 请求成功而且没有错误的时候调用
	 *
	 * @param response
	 * @param jsonStr
	 */
	public abstract void onSuccess(Response response, String jsonStr);

	/**
	 * 请求成功但是有错误的时候调用，解析错误等
	 *
	 * @param response
	 * @param errorCode
	 * @param e
	 */
	public abstract void onError(Response response, int errorCode, Exception e);
}
