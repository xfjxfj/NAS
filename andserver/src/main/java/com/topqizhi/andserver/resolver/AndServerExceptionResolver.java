package com.topqizhi.andserver.resolver;

import com.topqizhi.andserver.util.JsonUtils;
import com.yanzhenjie.andserver.annotation.Resolver;
import com.yanzhenjie.andserver.error.HttpException;
import com.yanzhenjie.andserver.framework.ExceptionResolver;
import com.yanzhenjie.andserver.framework.body.JsonBody;
import com.yanzhenjie.andserver.http.HttpRequest;
import com.yanzhenjie.andserver.http.HttpResponse;
import com.yanzhenjie.andserver.http.StatusCode;

import androidx.annotation.NonNull;

/**
 * Created by レインマン on 2021/07/16 15:36 with Android Studio.
 */
@Resolver
public class AndServerExceptionResolver implements ExceptionResolver {

	@Override
	public void onResolve(@NonNull HttpRequest request, @NonNull HttpResponse response, @NonNull Throwable e) {
		e.printStackTrace();
		if (e instanceof HttpException) {
			HttpException exception = (HttpException) e;
			response.setStatus(exception.getStatusCode());
		} else {
			response.setStatus(StatusCode.SC_INTERNAL_SERVER_ERROR);
		}
		String body = JsonUtils.failedJson(response.getStatus(), e.getMessage());
		response.setBody(new JsonBody(body));
	}
}