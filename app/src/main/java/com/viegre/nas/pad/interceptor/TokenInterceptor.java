package com.viegre.nas.pad.interceptor;

import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.viegre.nas.pad.config.NasConfig;

import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

// token拦截器
public class TokenInterceptor implements Interceptor {

	private static final String TAG = "TokenInterceptor";

	@Override
	public Response intercept(Chain chain) throws IOException {
		Request request = chain.request();
		Response originalResponse = chain.proceed(request);
		MediaType mediaType = originalResponse.body().contentType();
		String content = originalResponse.body().string();
		JSONObject jsonObject = JSONObject.parseObject(content);
		String code = String.valueOf(jsonObject.get("code"));
		if (code.equals(NasConfig.TOKEN_FAILED)) {
			Log.e(TAG, "intercept: " + "token失效");

		}
		return originalResponse.newBuilder().body(ResponseBody.Companion.create(content, mediaType)).build();
	}


}
