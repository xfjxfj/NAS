package com.topqizhi.andserver.component;

import android.util.Log;

import com.topqizhi.andserver.util.JsonUtils;
import com.yanzhenjie.andserver.annotation.Interceptor;
import com.yanzhenjie.andserver.framework.HandlerInterceptor;
import com.yanzhenjie.andserver.framework.handler.RequestHandler;
import com.yanzhenjie.andserver.http.HttpRequest;
import com.yanzhenjie.andserver.http.HttpResponse;

import androidx.annotation.NonNull;

/**
 * Created by Zhenjie Yan on 2018/9/11.
 */
@Interceptor
public class AndServerInterceptor implements HandlerInterceptor {
	@Override
	public boolean onIntercept(@NonNull HttpRequest request, @NonNull HttpResponse response, @NonNull RequestHandler handler) {
		Log.i("AndServerInterceptor",
		      "Path: " + request.getPath() + "\nMethod: " + request.getMethod() + "\nParam: " + JsonUtils.toJsonString(request.getParameter()));
		return false;
	}
}