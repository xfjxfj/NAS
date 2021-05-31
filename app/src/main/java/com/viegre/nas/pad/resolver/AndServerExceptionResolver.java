package com.viegre.nas.pad.resolver;

import com.viegre.nas.pad.util.JsonUtils;
import com.yanzhenjie.andserver.annotation.Resolver;
import com.yanzhenjie.andserver.error.HttpException;
import com.yanzhenjie.andserver.framework.ExceptionResolver;
import com.yanzhenjie.andserver.framework.body.JsonBody;
import com.yanzhenjie.andserver.http.HttpRequest;
import com.yanzhenjie.andserver.http.HttpResponse;
import com.yanzhenjie.andserver.http.StatusCode;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import androidx.annotation.NonNull;

/**
 * Created by レインマン on 2021/05/28 16:23 with Android Studio.
 */
@Resolver
public class AndServerExceptionResolver implements ExceptionResolver {
	@Override
	public void onResolve(@NonNull @NotNull HttpRequest request, @NonNull @NotNull HttpResponse response, @NonNull @NotNull Throwable e) {
		e.printStackTrace();
		if (e instanceof HttpException) {
			HttpException exception = (HttpException) e;
			response.setStatus(exception.getStatusCode());
		} else if (e instanceof IOException) {
			response.setStatus(1000);
		} else {
			response.setStatus(StatusCode.SC_INTERNAL_SERVER_ERROR);
		}
		String body = JsonUtils.failedJson(response.getStatus(), e.getMessage());
		response.setBody(new JsonBody(body));
	}
}
